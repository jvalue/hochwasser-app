package de.bitdroid.flooding.ceps;


import com.google.common.base.Optional;

import org.jvalue.ceps.api.RegistrationApi;
import org.jvalue.ceps.api.notifications.Client;
import org.jvalue.ceps.api.notifications.ClientDescription;
import org.jvalue.ceps.api.notifications.GcmClientDescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.bitdroid.flooding.gcm.GcmManager;
import de.bitdroid.flooding.ods.OdsManager;
import de.bitdroid.flooding.ods.Station;
import retrofit.client.Response;
import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;

@Singleton
public class CepsManager {

	private static final String
			PEGEL_ALARM_ABOVE_LEVEL_ADAPTER_ID = "pegelAlarmAboveLevel",
			PEGEL_ALARM_BELOW_LEVEL_ADAPTER_ID = "pegelAlarmBelowLevel";
	private static final String
			ARGUMENT_UUID = "STATION_UUID",
			ARGUMENT_LEVEL = "LEVEL";

	private final RegistrationApi registrationApi;
	private final OdsManager odsManager;
	private final GcmManager gcmManager;

	private List<Alarm> alarmsCache = null;

	@Inject
	CepsManager(RegistrationApi registrationApi, OdsManager odsManager, GcmManager gcmManager) {
		this.registrationApi = registrationApi;
		this.odsManager = odsManager;
		this.gcmManager = gcmManager;
	}


	public Observable<List<Alarm>> getAlarms() {
		if (alarmsCache != null) {
			List<Alarm> alarms = new ArrayList<>(alarmsCache);
			return Observable.just(alarms);
			
		} else {
			return Observable
					.merge(
							registrationApi.getAllClients(PEGEL_ALARM_ABOVE_LEVEL_ADAPTER_ID)
									.flatMap(new Func1<List<Client>, Observable<Map.Entry<String, Alarm.Builder>>>() {
										@Override
										public Observable<Map.Entry<String, Alarm.Builder>> call(List<Client> clients) {
											return Observable.from(parseClients(clients, true).entrySet());
										}
									}),
							registrationApi.getAllClients(PEGEL_ALARM_BELOW_LEVEL_ADAPTER_ID)
									.flatMap(new Func1<List<Client>, Observable<Map.Entry<String, Alarm.Builder>>>() {
										@Override
										public Observable<Map.Entry<String, Alarm.Builder>> call(List<Client> clients) {
											return Observable.from(parseClients(clients, false).entrySet());
										}
									}))
					.flatMap(new Func1<Map.Entry<String, Alarm.Builder>, Observable<Alarm>>() {
						@Override
						public Observable<Alarm> call(final Map.Entry<String, Alarm.Builder> entry) {
							return odsManager.getStationByUuid(entry.getKey())
									.flatMap(new Func1<Optional<Station>, Observable<Alarm>>() {
										@Override
										public Observable<Alarm> call(Optional<Station> stationOptional) {
											if (!stationOptional.isPresent()) {
												Timber.e("failed to find station with uuid " + entry.getKey());
												return Observable.empty();
											}
											return Observable.just(entry.getValue().setStation(stationOptional.get()).build());
										}
									});
						}
					})
					.toList()
					.flatMap(new Func1<List<Alarm>, Observable<List<Alarm>>>() {
						@Override
						public Observable<List<Alarm>> call(List<Alarm> alarms) {
							CepsManager.this.alarmsCache = new ArrayList<>(alarms);
							return Observable.just(alarms);
						}
					});
		}
	}


	public Observable<Void> addAlarm(Alarm alarm) {
		Map<String, Object> args = new HashMap<>();
		args.put(ARGUMENT_UUID, alarm.getStation().getUuid());
		args.put(ARGUMENT_LEVEL, alarm.getLevel());
		ClientDescription clientDescription = new GcmClientDescription(gcmManager.getRegId(), args);
		String adapterId = alarm.isAlarmWhenAboveLevel() ? PEGEL_ALARM_ABOVE_LEVEL_ADAPTER_ID : PEGEL_ALARM_BELOW_LEVEL_ADAPTER_ID;
		return registrationApi
				.registerClient(adapterId, clientDescription)
				.flatMap(new Func1<Client, Observable<Void>>() {
					@Override
					public Observable<Void> call(Client client) {
						clearAlarmsCache();
						return Observable.just(null);
					}
				});
	}


	public Observable<Void> removeAlarm(Alarm alarm) {
		String adapterId = alarm.isAlarmWhenAboveLevel() ? PEGEL_ALARM_ABOVE_LEVEL_ADAPTER_ID : PEGEL_ALARM_BELOW_LEVEL_ADAPTER_ID;
		String clientId = alarm.getId();
		return registrationApi.unregisterClient(adapterId, clientId)
				.flatMap(new Func1<Response, Observable<Void>>() {
					@Override
					public Observable<Void> call(Response response) {
						clearAlarmsCache();
						return Observable.just(null);
					}
				});
	}


	public void clearAlarmsCache() {
		alarmsCache = null;
	}


	private Map<String, Alarm.Builder> parseClients(List<Client> clients, boolean alarmWhenAboveLevel) {
		Map<String, Alarm.Builder> builderMap = new HashMap<>(); // station uuid --> builder
		for (Client client : clients) {
			Map<String, Object> args = client.getEplArguments();
			Alarm.Builder builder = new Alarm.Builder()
					.setId(client.getId())
					.setLevel((double) args.get(ARGUMENT_LEVEL))
					.setAlarmWhenAboveLevel(alarmWhenAboveLevel);
			builderMap.put((String) args.get(ARGUMENT_UUID), builder);
		}
		return builderMap;
	}

}
