package de.bitdroid.flooding.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.ceps.Alarm;
import de.bitdroid.flooding.ceps.CepsManager;
import de.bitdroid.flooding.network.NetworkUtils;
import de.bitdroid.flooding.ods.OdsManager;
import de.bitdroid.flooding.ods.Station;
import de.bitdroid.flooding.ods.StationMeasurements;
import de.bitdroid.flooding.utils.StringUtils;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.functions.Action1;
import timber.log.Timber;


/**
 * Adds a new alarm.
 */
@ContentView(R.layout.activity_new_alarm)
public class NewAlarmActivity extends AbstractActivity {

	@InjectView(R.id.text_station_name) TextView stationNameView;
	@InjectView(R.id.edit_level) TextView levelEditView;
	@InjectView(R.id.selection_relation) RadioGroup selectionGroup;
	@InjectView(R.id.button_confirm) Button confirmButton;

	@InjectView(R.id.card_levels) private CardView levelsCard;
	@InjectView(R.id.card_char_values) private CardView charValuesCard;
	@InjectView(R.id.card_metadata) private CardView metadataCard;
	@InjectView(R.id.card_map) private CardView mapCard;

	@Inject private NetworkUtils networkUtils;
	@Inject private OdsManager odsManager;
	@Inject private CepsManager cepsManager;
	@Inject private StationInfoUtils stationInfoUtils;

	private StationMeasurements measurements;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		// set title
		final Station station = new StationSelection(getIntent()).getStation();
		getSupportActionBar().setTitle(getString(R.string.title_new_alarm));
		stationNameView.setText(StringUtils.toProperCase(station.getStationName()));

		// select above level as default
		selectionGroup.check(R.id.button_above);

		// setup editing
		levelEditView.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {  }

			@Override
			public void afterTextChanged(Editable s) {
				if (levelEditView.getText().length() == 0) confirmButton.setEnabled(false);
				else confirmButton.setEnabled(true);
			}
		});
		confirmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean alarmWhenAboveLevel = selectionGroup.getCheckedRadioButtonId() == R.id.button_above;
				Alarm alarm = new Alarm.Builder()
						.setAlarmWhenAboveLevel(alarmWhenAboveLevel)
						.setLevel(Double.valueOf(levelEditView.getText().toString()))
						.setStation(station)
						.build();

				showSpinner();
				cepsManager
						.addAlarm(alarm)
						.compose(networkUtils.<Void>getDefaultTransformer())
						.subscribe(new Action1<Void>() {
							@Override
							public void call(Void aVoid) {
								hideSpinner();
								Toast.makeText(NewAlarmActivity.this, getString(R.string.alarms_new_created), Toast.LENGTH_SHORT).show();
								Intent intent = new Intent(NewAlarmActivity.this, MainDrawerActivity.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
								startActivity(intent);
								finish();
							}
						}, new Action1<Throwable>() {
							@Override
							public void call(Throwable throwable) {
								Toast.makeText(NewAlarmActivity.this, getString(R.string.alarms_new_not_created), Toast.LENGTH_SHORT).show();
								Timber.e(throwable, "failed to add alarm");
							}
						});
			}
		});


		// load data
		odsManager.getMeasurements(station)
				.compose(networkUtils.<StationMeasurements>getDefaultTransformer())
				.subscribe(new Action1<StationMeasurements>() {
					@Override
					public void call(StationMeasurements stationMeasurements) {
						NewAlarmActivity.this.measurements = stationMeasurements;
						stationInfoUtils.setupStationCards(measurements, levelsCard, charValuesCard, metadataCard, mapCard);
					}
				}, new Action1<Throwable>() {
					@Override
					public void call(Throwable throwable) {
						Timber.e(throwable, "failed to download measurements");
					}
				});
	}


}
