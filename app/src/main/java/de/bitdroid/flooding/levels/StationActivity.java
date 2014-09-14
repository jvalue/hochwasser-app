package de.bitdroid.flooding.levels;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.dataselection.Extras;
import de.bitdroid.flooding.utils.BaseActivity;
import de.bitdroid.utils.StringUtils;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardView;

public class StationActivity extends BaseActivity
	implements LoaderManager.LoaderCallbacks<Cursor>, Extras {
	
	private static final int LOADERID = 46;

	private String stationName;
	private String waterName;
	private CardView levelView, infoView, charValuesView, mapView;
	private StationCardFactory factory;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.data_station);
		stationName = getIntent().getExtras().getString(EXTRA_STATION_NAME);
		waterName = getIntent().getExtras().getString(EXTRA_WATER_NAME);

		levelView = (CardView) findViewById(R.id.level);
		infoView = (CardView) findViewById(R.id.info);
		charValuesView = (CardView) findViewById(R.id.charValues);
		mapView = (CardView) findViewById(R.id.map);

		getActionBar().setTitle(StringUtils.toProperCase(stationName));
		getActionBar().setSubtitle(StringUtils.toProperCase(waterName));

		// fetch new station data
		Intent intent = new Intent(this, StationIntentService.class);
		intent.putExtra(StationIntentService.EXTRA_STATION_NAME, stationName);
		startService(intent);

		factory = new StationCardFactory(getApplicationContext());
		getSupportLoaderManager().initLoader(LOADERID, null, this);

    }


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		return factory.createCursorLoader(stationName);
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader.getId() != LOADERID) return;
		if (cursor.getCount() <= 0) return;
		cursor.moveToFirst();

		Card levelCard = factory.createStationLevelCard(cursor);
		if (levelView.getCard() == null) levelView.setCard(levelCard);
		else levelView.refreshCard(levelCard);
		levelView.setVisibility(View.VISIBLE);

		Card infoCard = factory.createStationInfoCard(cursor);
		if (infoView.getCard() == null) infoView.setCard(infoCard);
		else infoView.refreshCard(infoCard);
		infoView.setVisibility(View.VISIBLE);

		StationCharValuesCard charValuesCard = factory.createStationCharValuesCard(cursor);
		if (charValuesCard.isEmpty()) charValuesView.setVisibility(View.GONE);
		else {
			if (charValuesView.getCard() == null) charValuesView.setCard(charValuesCard);
			else charValuesView.setCard(charValuesCard);
			charValuesView.setVisibility(View.VISIBLE);
		}

		StationMapCard mapCard = factory.createStationMapCard(cursor, this);
		if (mapCard.isEmpty()) mapView.setVisibility(View.GONE);
		else {
			if (mapView.getCard() == null) mapView.setCard(mapCard);
			else mapView.refreshCard(mapCard);
			mapView.setVisibility(View.VISIBLE);
		}
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader) { }


}
