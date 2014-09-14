package de.bitdroid.flooding.alarms;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.dataselection.Extras;
import de.bitdroid.flooding.levels.StationCardFactory;
import de.bitdroid.flooding.levels.StationCharValuesCard;
import de.bitdroid.flooding.levels.StationIntentService;
import de.bitdroid.flooding.levels.StationMapCard;
import de.bitdroid.ods.cep.RuleManager;
import de.bitdroid.ods.cep.RuleManagerFactory;
import de.bitdroid.ods.gcm.GcmStatus;
import de.bitdroid.utils.StringUtils;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardView;


public final class SelectLevelFragment extends Fragment implements Extras, LoaderManager.LoaderCallbacks<Cursor> {

	private static final int LOADERID = 48;

	public static SelectLevelFragment newInstance(String riverName, String stationName) {
		SelectLevelFragment fragment = new SelectLevelFragment();
		Bundle extras = new Bundle();
		extras.putString(EXTRA_WATER_NAME, riverName);
		extras.putString(EXTRA_STATION_NAME, stationName);
		fragment.setArguments(extras);
		return fragment;
	}


	private String river, station;
	private CardView detailsCardView, levelView, charValuesView, mapView;
	private StationCardFactory cardFactory;
	private boolean showingStationCards = false;
	private Cursor stationData;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.river = getArguments().getString(EXTRA_WATER_NAME);
		this.station = getArguments().getString(EXTRA_STATION_NAME);
	}


	@Override
	public View onCreateView(
			LayoutInflater inflater, 
			ViewGroup container, 
			Bundle savedInstanceState) {

		getActivity().getActionBar().setTitle(getString(R.string.alarms_new_title_level));

		View view = inflater.inflate(R.layout.alarms_new, container, false);

		TextView stationView = (TextView) view.findViewById(R.id.station);

		final EditText levelEditText = (EditText) view.findViewById(R.id.level);
		final RadioGroup relationRadioGroup = (RadioGroup) view.findViewById(R.id.relation);

		stationView.setText(
				StringUtils.toProperCase(river) + " - " + StringUtils.toProperCase(station));

		final Button okButton = (Button) view.findViewById(R.id.ok);
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				double level = Double.valueOf(levelEditText.getText().toString());
				boolean whenAbove = relationRadioGroup.getCheckedRadioButtonId() == R.id.above;

				LevelAlarm alarm = new LevelAlarm(river, station, level, whenAbove);
				RuleManager manager = RuleManagerFactory.createRuleManager(getActivity().getApplicationContext());
				if (!manager.getRegistrationStatus(alarm.getRule()).equals(GcmStatus.UNREGISTERED)) {
					Toast.makeText(getActivity(), getString(R.string.alarms_new_already_added), Toast.LENGTH_SHORT).show();
					return;
				}

				manager.registerRule(alarm.getRule());

				SelectLevelFragment.this.getActivity().finish();
				Toast.makeText(
					SelectLevelFragment.this.getActivity(), 
					getString(R.string.alarms_new_created), 
					Toast.LENGTH_SHORT)
					.show();
			}
		});

		levelEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) { }
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() == 0) {
					okButton.setEnabled(false);
				} else {
					okButton.setEnabled(true);
				}
			}
		});

		// on card click show other cards
		this.detailsCardView = (CardView) view.findViewById(R.id.card_show);
		Card card = new Card(getActivity().getApplicationContext(), R.layout.station_card_more);
		card.setCheckable(true);
		card.setOnClickListener(new Card.OnCardClickListener() {
			@Override
			public void onClick(Card card, View view) {
				showingStationCards = true;
				if (stationData != null) showStationCards();
				detailsCardView.setVisibility(View.GONE);
			}
		});
		detailsCardView.setCard(card);

		// fetch new station data
		Intent intent = new Intent(getActivity(), StationIntentService.class);
		intent.putExtra(StationIntentService.EXTRA_STATION_NAME, station);
		getActivity().startService(intent);

		levelView = (CardView) view.findViewById(R.id.card_level);
		charValuesView = (CardView) view.findViewById(R.id.card_charvalues);
		mapView = (CardView) view.findViewById(R.id.card_map);

		cardFactory = new StationCardFactory(getActivity().getApplicationContext());
		getLoaderManager().initLoader(LOADERID, null, this);

		return view;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState == null) return;
		this.showingStationCards = savedInstanceState.getBoolean(KEY_SHOWING_DETAILS);
		if (showingStationCards) {
			if (stationData != null) showStationCards();
			detailsCardView.setVisibility(View.GONE);
		}
	}


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		return cardFactory.createCursorLoader(station);
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader.getId() != LOADERID) return;
		this.stationData = cursor;
		if (showingStationCards) showStationCards();
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader) { }


	private static final String KEY_SHOWING_DETAILS = "SHOWING_DETAILS";
	@Override
	public void onSaveInstanceState(Bundle state) {
		state.putBoolean(KEY_SHOWING_DETAILS, showingStationCards);
	}

	private void showStationCards() {
		if (stationData.getCount() <= 0) return;
		stationData.moveToFirst();

		Card levelCard = cardFactory.createStationLevelCard(stationData);
		if (levelView.getCard() == null) levelView.setCard(levelCard);
		else levelView.refreshCard(levelCard);
		levelView.setVisibility(View.VISIBLE);

		StationCharValuesCard charValuesCard = cardFactory.createStationCharValuesCard(stationData);
		if (charValuesCard.isEmpty()) charValuesView.setVisibility(View.GONE);
		else {
			if (charValuesView.getCard() == null) charValuesView.setCard(charValuesCard);
			else charValuesView.setCard(charValuesCard);
			charValuesView.setVisibility(View.VISIBLE);
		}

		StationMapCard mapCard = cardFactory.createStationMapCard(stationData, getActivity());
		if (mapCard.isEmpty()) mapView.setVisibility(View.GONE);
		else {
			if (mapView.getCard() == null) mapView.setCard(mapCard);
			else mapView.refreshCard(mapCard);
			mapView.setVisibility(View.VISIBLE);
		}
	}
}
