package de.bitdroid.flooding.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.ceps.Alarm;
import de.bitdroid.flooding.ceps.CepsManager;
import de.bitdroid.flooding.network.AbstractErrorAction;
import de.bitdroid.flooding.network.NetworkUtils;
import de.bitdroid.flooding.ods.BodyOfWater;
import de.bitdroid.flooding.ods.Station;
import de.bitdroid.flooding.utils.StringUtils;
import roboguice.inject.InjectView;
import rx.functions.Action1;
import timber.log.Timber;

public class AlarmsFragment extends AbstractFragment {

	@Inject private NetworkUtils networkUtils;
	@Inject private CepsManager cepsManager;

	@InjectView(R.id.button_add) FloatingActionButton addButton;
	@InjectView(R.id.list) RecyclerView recyclerView;
	private AlarmsAdapter adapter;



	public AlarmsFragment() {
		super(R.layout.fragment_alarms);
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// setup list
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
		recyclerView.setLayoutManager(layoutManager);
		adapter = new AlarmsAdapter();
		recyclerView.setAdapter(adapter);

		// setup add button
		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), WaterSelectionActivity.class));
			}
		});

		// load items
		cepsManager.getAlarms()
				.compose(networkUtils.<List<Alarm>>getDefaultTransformer())
				.subscribe(new Action1<List<Alarm>>() {
					@Override
					public void call(List<Alarm> alarms) {
						adapter.setAlarms(alarms);
					}
				}, new AbstractErrorAction(AlarmsFragment.this) {
					@Override
					protected void doCall(Throwable throwable) {
						Timber.e(throwable, "failed to load data");
					}
				});
	}


	protected class AlarmsAdapter extends RecyclerView.Adapter<AlarmViewHolder> {

		private final List<Alarm> alarmList = new ArrayList<>();

		@Override
		public void onBindViewHolder(AlarmViewHolder holder, int position) {
			holder.setItem(alarmList.get(position));
		}

		@Override
		public int getItemCount() {
			return alarmList.size();
		}

		public void setAlarms(List<Alarm> alarmList) {
			this.alarmList.clear();
			this.alarmList.addAll(alarmList);
			notifyDataSetChanged();
		}

		@Override
		public AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_alarm, parent, false);
			return new AlarmViewHolder(view);
		}

	}


	protected class AlarmViewHolder extends RecyclerView.ViewHolder {

		private final TextView stationView;
		private final TextView descriptionView;

		public AlarmViewHolder(View itemView) {
			super(itemView);
			this.stationView = (TextView) itemView.findViewById(R.id.alarm_title);
			this.descriptionView = (TextView) itemView.findViewById(R.id.alarm_description);
		}

		public void setItem(Alarm alarm) {
			stationView.setText(StringUtils.toProperCase(alarm.getStation().getStationName()));
			descriptionView.setText("Level: " + alarm.getLevel());
		}

	}


	/**
	 * The glue that is necessary kick of the station selection process and show new {@link NewAlarmActivity}
	 * when a station has been selected.
	 */
	public static class WaterSelectionActivity extends AbstractWaterSelectionActivity {

		@Override
		protected void onDataSelected(BodyOfWater water) {
			startActivity(new StationSelection(water).toIntent(this, StationSelectionActivity.class));
		}

		@Override
		protected Class<? extends AbstractMapSelectionActivity> getMapSelectionClass() {
			return MapSelectionActivity.class;
		}

	}


	public static class StationSelectionActivity extends AbstractStationSelectionActivity {

		@Override
		protected void onDataSelected(Station station) {
			onStationSelected(this, station);
		}

		@Override
		protected Class<? extends AbstractMapSelectionActivity> getMapSelectionClass() {
			return MapSelectionActivity.class;
		}

	}


	public static class MapSelectionActivity extends AbstractMapSelectionActivity {

		@Override
		public void onStationClicked(Station station) {
			onStationSelected(this, station);
		}

	}


	private static void onStationSelected(Context context, Station station) {
		context.startActivity(new StationSelection(station.getBodyOfWater(), station).toIntent(context, NewAlarmActivity.class));
	}

}

