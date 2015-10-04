package de.bitdroid.flooding.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.ceps.Alarm;
import de.bitdroid.flooding.ceps.CepsManager;
import de.bitdroid.flooding.network.DefaultErrorAction;
import de.bitdroid.flooding.network.ErrorActionBuilder;
import de.bitdroid.flooding.network.HideSpinnerAction;
import de.bitdroid.flooding.ods.OdsManager;
import de.bitdroid.flooding.ods.Station;
import de.bitdroid.flooding.ods.StationMeasurements;
import de.bitdroid.flooding.utils.StringUtils;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.functions.Action1;


/**
 * Adds a new alarm.
 */
@ContentView(R.layout.activity_new_alarm)
public class NewAlarmActivity extends AbstractRestrictedActivity {

	// if true this activity will start the main activity once confirmed
	public static final String EXTRA_START_MAIN_ACTIVITY_ON_FINISH = "EXTRA_START_MAIN_ACTIVITY_ON_FINISH";

	@InjectView(R.id.text_station_name) TextView stationNameView;
	@InjectView(R.id.edit_level) TextView levelEditView;
	@InjectView(R.id.checkbox_above_level) CheckBox aboveLevelCheckBox;
	@InjectView(R.id.checkbox_below_level) CheckBox belowLevelCheckBox;
	@InjectView(R.id.button_confirm) Button confirmButton;

	@InjectView(R.id.card_levels) private CardView levelsCard;
	@InjectView(R.id.card_char_values) private CardView charValuesCard;
	@InjectView(R.id.card_metadata) private CardView metadataCard;
	@InjectView(R.id.card_map) private CardView mapCard;

	@Inject private OdsManager odsManager;
	@Inject private CepsManager cepsManager;
	@Inject private StationInfoUtils stationInfoUtils;

	private StationMeasurements measurements;

    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		analyticsUtils.onScreen("new alarm screen");

		// set title
		final Station station = new StationSelection(getIntent()).getStation();
		getSupportActionBar().setTitle(getString(R.string.title_new_alarm));
		stationNameView.setText(StringUtils.toProperCase(station.getStationName()));

		// select above level as default
		aboveLevelCheckBox.setChecked(true);

		// toggle confirm button when level check box change
		aboveLevelCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				onLevelCheckBoxChanged();
			}
		});
		belowLevelCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				onLevelCheckBoxChanged();
			}
		});

		// setup editing
		levelEditView.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (levelEditView.getText().length() == 0) confirmButton.setEnabled(false);
				else confirmButton.setEnabled(true);
			}
		});
		confirmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				analyticsUtils.onClick("create alarm");

				Alarm alarm = new Alarm.Builder()
						.setAlarmWhenAboveLevel(aboveLevelCheckBox.isChecked())
						.setAlarmWhenBelowLevel(belowLevelCheckBox.isChecked())
						.setLevel(Double.valueOf(levelEditView.getText().toString()))
						.setStation(station)
						.build();

				showSpinner();
				compositeSubscription.add(cepsManager
						.addAlarm(alarm)
						.compose(networkUtils.<Void>getDefaultTransformer())
						.subscribe(new Action1<Void>() {
							@Override
							public void call(Void aVoid) {

								Toast.makeText(NewAlarmActivity.this, getString(R.string.alarms_new_created), Toast.LENGTH_SHORT).show();
								boolean startMainActivity = getIntent().getBooleanExtra(EXTRA_START_MAIN_ACTIVITY_ON_FINISH, false);
								if (startMainActivity) {
									Intent intent = new Intent(NewAlarmActivity.this, MainDrawerActivity.class);
									intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
									startActivity(intent);
								}
								finish();
							}
						}, new ErrorActionBuilder()
								.add(new DefaultErrorAction(NewAlarmActivity.this, NewAlarmActivity.this, "failed to add alarm"))
								.add(new HideSpinnerAction(NewAlarmActivity.this))
								.build()));
			}
		});


		// load data
		compositeSubscription.add(odsManager.getMeasurements(station)
				.compose(networkUtils.<StationMeasurements>getDefaultTransformer())
				.subscribe(new Action1<StationMeasurements>() {
					@Override
					public void call(StationMeasurements stationMeasurements) {
						NewAlarmActivity.this.measurements = stationMeasurements;
						stationInfoUtils.setupStationCards(measurements, levelsCard, charValuesCard, metadataCard, mapCard);
					}
				}, new ErrorActionBuilder()
						.add(new DefaultErrorAction(NewAlarmActivity.this, NewAlarmActivity.this, "failed to download measurements"))
						.add(new HideSpinnerAction(NewAlarmActivity.this))
						.build()));
	}


	private void onLevelCheckBoxChanged() {
		boolean enableConfirm = false;
		if (aboveLevelCheckBox.isChecked() || belowLevelCheckBox.isChecked()) {
			enableConfirm = true;
		}
		confirmButton.setEnabled(enableConfirm);
	}

}
