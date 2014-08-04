package de.bitdroid.flooding.levels;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.dataselection.Extras;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.flooding.utils.Log;
import de.bitdroid.flooding.utils.StringUtils;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_TIMESTAMP;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_TYPE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_VALUE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_NAME;

public class StationGraphActivity extends BaseActivity
	implements LoaderManager.LoaderCallbacks<Cursor>, Extras {
	
	private static final int LOADERID = 46;

	private CardListView listView;
	private CardArrayAdapter listAdapter;
	private String stationName;
	private String waterName;

	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.data_station);
		stationName = getIntent().getExtras().getString(EXTRA_STATION_NAME);
		waterName = getIntent().getExtras().getString(EXTRA_WATER_NAME);

		listView = (CardListView) findViewById(R.id.list);
		listAdapter = new CardArrayAdapter(this, new ArrayList<Card>());
		listView.setAdapter(listAdapter);

		getActionBar().setTitle(StringUtils.toProperCase(stationName));
		getActionBar().setSubtitle(StringUtils.toProperCase(waterName));

		// fetch new station data
		Intent intent = new Intent(this, StationIntentService.class);
		intent.putExtra(StationIntentService.EXTRA_STATION_NAME, stationName);
		startService(intent);
    }


	@Override
	public void onResume() {
		super.onResume();
		getLoaderManager().initLoader(LOADERID, null, this);
	}


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		return new CursorLoader(
				getApplicationContext(),
				PegelOnlineSource.INSTANCE.toUri(),
				new String[] {
						COLUMN_LEVEL_VALUE,
						COLUMN_LEVEL_UNIT,
						COLUMN_LEVEL_TIMESTAMP },
				COLUMN_STATION_NAME + "=? AND " + COLUMN_LEVEL_TYPE + "=?",
				new String[] { stationName, "W" },
				null);
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader.getId() != LOADERID) return;

		if (cursor.getCount() <= 0) return;

		int valueIdx = cursor.getColumnIndex(COLUMN_LEVEL_VALUE);
		int unitIdx = cursor.getColumnIndex(COLUMN_LEVEL_UNIT);

		cursor.moveToFirst();

		Card card = new LevelCard(
				getApplicationContext(),
				cursor.getString(2),
				cursor.getDouble(0),
				cursor.getString(1));

		listAdapter.clear();
		listAdapter.add(card);
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader) { }



	private static final class LevelCard extends Card {

		private static final DateFormat
				dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ"),
				dateFormat = new SimpleDateFormat("dd/MM/yy"),
				timeFormat = new SimpleDateFormat("HH:mm a");


		private final String timestamp;
		private final double value;
		private final String unit;

		public  LevelCard(Context context, String timestamp, double value, String unit) {
			super(context, R.layout.station_card_level);
			this.timestamp = timestamp;
			this.value = value;
			this.unit = unit;
		}


		@Override
		public void setupInnerViewElements(ViewGroup parent, View view) {
			TextView dateView = (TextView) view.findViewById(R.id.timestamp_date);
			TextView timeView = (TextView) view.findViewById(R.id.timestamp_time);
			TextView levelView = (TextView) view.findViewById(R.id.level);

			try {
				Date date = dateParser.parse(timestamp);
				dateView.setText(dateFormat.format(date));
				timeView.setText(timeFormat.format(date));
			} catch (ParseException pe) {
				Log.error("failed to parse timestamp", pe);
			}

			levelView.setText(value + " " + unit);
		}
	}

}
