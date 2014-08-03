package de.bitdroid.flooding.levels;


import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.ods.data.OdsSource;
import de.bitdroid.flooding.ods.data.OdsSourceManager;
import de.bitdroid.flooding.ods.utils.RestCall;
import de.bitdroid.flooding.ods.utils.RestException;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.flooding.utils.Log;

public class StationIntentService extends IntentService {


	public static final String EXTRA_STATION_NAME = "EXTRA_STATION_NAME";

	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ");


	public StationIntentService() {
		super(StationIntentService.class.getSimpleName());
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		String station = intent.getStringExtra(EXTRA_STATION_NAME);

		PegelOnlineSource source = PegelOnlineSource.INSTANCE;
		Cursor cursor = getContentResolver().query(
				source.toUri(),
				new String[] { PegelOnlineSource.COLUMN_LEVEL_TIMESTAMP, PegelOnlineSource.COLUMN_ID },
				PegelOnlineSource.COLUMN_STATION_NAME + "=? AND "
						+ PegelOnlineSource.COLUMN_LEVEL_TYPE + "=?",
				new String[] { station, "W" },
				null);

		cursor.moveToFirst();
		if (cursor.getCount() == 1) {
			String timestamp = cursor.getString(0);
			try {
				Date lastMeasurement = dateFormat.parse(timestamp);
				Date currentDate = new Date();

				int maxAge = getResources().getInteger(R.integer.station_max_age_in_ms);
				long diff = currentDate.getTime() - lastMeasurement.getTime();

				if (diff > maxAge) synchronizeStation(station, true, cursor.getInt(1));

			} catch (ParseException pe) {
				Log.error("failed to parse date", pe);
			}

		} else if (cursor.getCount() > 1) {
			Log.warning("found more than one timestamp!");
		} else {
			synchronizeStation(station, false, 0);
		}

	}


	private void synchronizeStation(String stationName, boolean oldValuePresent, int id) {
		try {
			PegelOnlineSource source = PegelOnlineSource.INSTANCE;
			String odsServerName = OdsSourceManager.getInstance(getApplicationContext()).getOdsServerName();
			String jsonResult = new RestCall.Builder(RestCall.RequestType.GET, odsServerName)
					.path(source.getSourceUrlPath())
					.path(stationName)
					.build()
					.execute();

			JSONObject json = new JSONObject(jsonResult);

			ContentValues contentValues = source.saveData(json, System.currentTimeMillis());

			if (oldValuePresent) {
				// update
				contentValues.put(OdsSource.COLUMN_ID, id);
				getContentResolver().update(
						source.toUri(),
						contentValues,
						OdsSource.COLUMN_ID + "=?",
						new String[] { String.valueOf(id) });

			} else {
				// insert
				getContentResolver().insert(source.toUri(), contentValues);
			}

		} catch(RestException re) {
			Log.error("failed to synchronize station", re);
		} catch(JSONException je) {
			Log.error("failed to read json", je);
		}
	}

}
