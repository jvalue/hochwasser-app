package de.bitdroid.flooding.levels;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.utils.Assert;

import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_HTHW_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_HTHW_VALUE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MHW_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MHW_VALUE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MNW_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MNW_VALUE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MTHW_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MTHW_VALUE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MTNW_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MTNW_VALUE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MW_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MW_VALUE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_NTNW_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_NTNW_VALUE;
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
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_WATER_NAME;

public final class StationCardFactory {

	private final Context context;

	public StationCardFactory(Context context) {
		this.context = context;
	}


	public Loader<Cursor> createCursorLoader(String stationName) {
		return new CursorLoader(
				context,
				PegelOnlineSource.INSTANCE.toUri(),
				new String[] {
						COLUMN_LEVEL_VALUE,
						COLUMN_LEVEL_UNIT,
						COLUMN_LEVEL_TIMESTAMP,
						COLUMN_WATER_NAME,
						COLUMN_STATION_NAME,
						COLUMN_STATION_KM,
						COLUMN_STATION_LAT,
						COLUMN_STATION_LONG,
						COLUMN_LEVEL_ZERO_VALUE,
						COLUMN_LEVEL_ZERO_UNIT,
						COLUMN_CHARVALUES_MHW_VALUE,
						COLUMN_CHARVALUES_MHW_UNIT,
						COLUMN_CHARVALUES_MW_VALUE,
						COLUMN_CHARVALUES_MW_UNIT,
						COLUMN_CHARVALUES_MNW_VALUE,
						COLUMN_CHARVALUES_MNW_UNIT,
						COLUMN_CHARVALUES_MTHW_VALUE,
						COLUMN_CHARVALUES_MTHW_UNIT,
						COLUMN_CHARVALUES_MTNW_VALUE,
						COLUMN_CHARVALUES_MTNW_UNIT,
						COLUMN_CHARVALUES_HTHW_VALUE,
						COLUMN_CHARVALUES_HTHW_UNIT,
						COLUMN_CHARVALUES_NTNW_VALUE,
						COLUMN_CHARVALUES_NTNW_UNIT
				},
				COLUMN_STATION_NAME + "=? AND " + COLUMN_LEVEL_TYPE + "=?",
				new String[] { stationName, "W" },
				null);
	}


	public StationLevelCard createStationLevelCard(Cursor cursor) {
		return new StationLevelCard(
				context,
				cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LEVEL_TIMESTAMP)),
				cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LEVEL_VALUE)),
				cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LEVEL_UNIT)));
	}


	public StationInfoCard createStationInfoCard(Cursor cursor) {
		NullCursorWrapper wrapper = new NullCursorWrapper(cursor);
		return new StationInfoCard.Builder(context)
				.station(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATION_NAME)))
				.river(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WATER_NAME)))
				.riverKm(wrapper.getDouble(cursor.getColumnIndexOrThrow(COLUMN_STATION_KM)))
				.lat(wrapper.getDouble(cursor.getColumnIndexOrThrow(COLUMN_STATION_LAT)))
				.lon(wrapper.getDouble(cursor.getColumnIndexOrThrow(COLUMN_STATION_LONG)))
				.zeroValue(wrapper.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LEVEL_ZERO_VALUE)))
				.zeroUnit(wrapper.getString(cursor.getColumnIndexOrThrow(COLUMN_LEVEL_ZERO_UNIT)))
				.build();
	}

	public StationCharValuesCard createStationCharValuesCard(Cursor cursor) {
		NullCursorWrapper wrapper = new NullCursorWrapper(cursor);
		return new StationCharValuesCard.Builder(context)
				.mhw(
						wrapper.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CHARVALUES_MHW_VALUE)),
						wrapper.getString(cursor.getColumnIndexOrThrow(COLUMN_CHARVALUES_MHW_UNIT)))
				.mw(
						wrapper.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CHARVALUES_MW_VALUE)),
						wrapper.getString(cursor.getColumnIndexOrThrow(COLUMN_CHARVALUES_MW_UNIT)))
				.mnw(
						wrapper.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CHARVALUES_MNW_VALUE)),
						wrapper.getString(cursor.getColumnIndexOrThrow(COLUMN_CHARVALUES_MNW_UNIT)))
				.mthw(
						wrapper.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CHARVALUES_MTHW_VALUE)),
						wrapper.getString(cursor.getColumnIndexOrThrow(COLUMN_CHARVALUES_MTHW_UNIT)))
				.mtnw(
						wrapper.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CHARVALUES_MTNW_VALUE)),
						wrapper.getString(cursor.getColumnIndexOrThrow(COLUMN_CHARVALUES_MTNW_UNIT)))
				.hthw(
						wrapper.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CHARVALUES_HTHW_VALUE)),
						wrapper.getString(cursor.getColumnIndexOrThrow(COLUMN_CHARVALUES_HTHW_UNIT)))
				.ntnw(
						wrapper.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CHARVALUES_NTNW_VALUE)),
						wrapper.getString(cursor.getColumnIndexOrThrow(COLUMN_CHARVALUES_NTNW_UNIT)))
				.build();
	}


	public StationMapCard createStationMapCard(Cursor cursor, Activity activity) {
		NullCursorWrapper wrapper = new NullCursorWrapper(cursor);
		return new StationMapCard(
				activity,
				cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATION_NAME)),
				cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WATER_NAME)),
				wrapper.getDouble(cursor.getColumnIndexOrThrow(COLUMN_STATION_LAT)),
				wrapper.getDouble(cursor.getColumnIndexOrThrow(COLUMN_STATION_LONG)));
	}


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
