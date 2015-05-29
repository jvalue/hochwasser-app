package de.bitdroid.flooding.ceps;


import com.google.common.base.Optional;

import org.jvalue.ceps.api.RegistrationApi;
import org.jvalue.ceps.api.notifications.Client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.bitdroid.flooding.ods.OdsManager;
import de.bitdroid.flooding.ods.Station;
import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;

@Singleton
public class CepsManager {

	private static final String PEGEL_ALARM_ADAPTER_ID = "pegelAlarm";
	private static final String
			ARGUMENT_UUID = "UUID",
			ARGUMENT_LEVEL = "LEVEL",
			ARGUMENT_ALARM_WHEN_ABOVE_LEVEL = "above";

	private final RegistrationApi registrationApi;
	private final OdsManager odsManager;

	private List<Alarm> alarmsCache = null;

	@Inject
	CepsManager(RegistrationApi registrationApi, OdsManager odsManager) {
		this.registrationApi = registrationApi;
		this.odsManager = odsManager;
	}


	public Observable<List<Alarm>> getAlarms() {
		return registrationApi.getAllClients(PEGEL_ALARM_ADAPTER_ID)
				.flatMap(new Func1<List<Client>, Observable<Map.Entry<String, Alarm.Builder>>>() {
					@Override
					public Observable<Map.Entry<String, Alarm.Builder>> call(List<Client> clients) {
						Map<String, Alarm.Builder> builderMap = new HashMap<>(); // station name --> builder
						for (Client client : clients) {
							Map<String, Object> args = client.getEplArguments();
							Alarm.Builder builder = new Alarm.Builder()
									.setId(client.getId())
									.setLevel((double) args.get(ARGUMENT_LEVEL))
									// .setAlarmWhenAboveLevel((boolean) args.get(ARGUMENT_ALARM_WHEN_ABOVE_LEVEL));
									.setAlarmWhenAboveLevel(true);
							// 	TODO configure second adapter for below
							builderMap.put((String) args.get(ARGUMENT_UUID), builder);
						}
						return Observable.from(builderMap.entrySet());
					}
				})
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
				.toList();
	}

}
