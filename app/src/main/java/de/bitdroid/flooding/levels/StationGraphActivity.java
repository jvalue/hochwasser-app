package de.bitdroid.flooding.levels;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.dataselection.Extras;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.flooding.utils.Assert;
import de.bitdroid.flooding.utils.StringUtils;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardView;

import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_TIMESTAMP;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_TYPE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_VALUE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_ZERO_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_ZERO_VALUE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_KM;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_LAT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_LONG;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_NAME;

public class StationGraphActivity extends BaseActivity
	implements LoaderManager.LoaderCallbacks<Cursor>, Extras {
	
	private static final int LOADERID = 46;

	private CardView levelView, infoView;
	private String stationName;
	private String waterName;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.data_station);
		stationName = getIntent().getExtras().getString(EXTRA_STATION_NAME);
		waterName = getIntent().getExtras().getString(EXTRA_WATER_NAME);

		levelView = (CardView) findViewById(R.id.level);
		infoView = (CardView) findViewById(R.id.info);

		getActionBar().setTitle(StringUtils.toProperCase(stationName));
		getActionBar().setSubtitle(StringUtils.toProperCase(waterName));

		// fetch new station data
		Intent intent = new Intent(this, StationIntentService.class);
		intent.putExtra(StationIntentService.EXTRA_STATION_NAME, stationName);
		startService(intent);

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
						COLUMN_LEVEL_TIMESTAMP,
						COLUMN_STATION_KM,
						COLUMN_STATION_LAT,
						COLUMN_STATION_LONG,
						COLUMN_LEVEL_ZERO_VALUE,
						COLUMN_LEVEL_ZERO_UNIT
				},
				COLUMN_STATION_NAME + "=? AND " + COLUMN_LEVEL_TYPE + "=?",
				new String[] { stationName, "W" },
				null);
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader.getId() != LOADERID) return;
		if (cursor.getCount() <= 0) return;
		cursor.moveToFirst();
		NullCursorWrapper wrapper = new NullCursorWrapper(cursor);

		Card levelCard = new StationLevelCard(
				getApplicationContext(),
				cursor.getString(2),
				cursor.getDouble(0),
				cursor.getString(1));

		levelView.setCard(levelCard);

		Card infoCard = new StationInfoCard.Builder(getApplicationContext())
				.station(stationName)
				.river(waterName)
				.riverKm(wrapper.getDouble(3))
				.lat(wrapper.getDouble(4))
				.lon(wrapper.getDouble(5))
				.zeroValue(wrapper.getDouble(6))
				.zeroUnit(wrapper.getString(7))
				.build();

		infoView.setCard(infoCard);
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader) { }


	private static final class NullCursorWrapper {

		private final Cursor cursor;

		public NullCursorWrapper(Cursor cursor) {
			Assert.assertNotNull(cursor);
			this.cursor = cursor;
		}

		public Long getLong(int idx) {
			if (isNull(idx)) return null;
			return cursor.getLong(idx);
		}

		public Double getDouble(int idx) {
			if (isNull(idx)) return null;
			return cursor.getDouble(idx);
		}

		public Integer getInt(int idx) {
			if (isNull(idx)) return null;
			return cursor.getInt(idx);
		}

		public String getString(int idx) {
			if (isNull(idx)) return null;
			return cursor.getString(idx);
		}

		public Float getFloat(int idx) {
			if (isNull(idx)) return null;
			return cursor.getFloat(idx);
		}

		private boolean isNull(int idx) {
			return cursor.isNull(idx);
		}
	}

}
