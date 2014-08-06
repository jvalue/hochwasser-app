package de.bitdroid.flooding.levels;


import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.ods.data.OdsSource;
import de.bitdroid.flooding.ods.data.OdsSourceManager;
import de.bitdroid.utils.RestCall;
import de.bitdroid.utils.RestException;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.utils.Log;

public class StationIntentService extends IntentService {

	public static final String EXTRA_STATION_NAME = "EXTRA_STATION_NAME";


	public StationIntentService() {
		super(StationIntentService.class.getSimpleName());
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		String station = intent.getStringExtra(EXTRA_STATION_NAME);

		PegelOnlineSource source = PegelOnlineSource.INSTANCE;
		Cursor cursor = getContentResolver().query(
				source.toUri(),
				new String[] { OdsSource.COLUMN_TIMESTAMP, OdsSource.COLUMN_ID },
				PegelOnlineSource.COLUMN_STATION_NAME + "=? AND "
						+ PegelOnlineSource.COLUMN_LEVEL_TYPE + "=?",
				new String[] { station, "W" },
				null);

		cursor.moveToFirst();
		if (cursor.getCount() == 1) {
			Date lastMeasurement = new Date(cursor.getLong(0));
			Date currentDate = new Date();

			int maxAge = getResources().getInteger(R.integer.station_max_age_in_ms);
			long diff = currentDate.getTime() - lastMeasurement.getTime();

			if (diff > maxAge) synchronizeStation(station, true, cursor.getInt(1));


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
					.path(URLEncoder.encode(stationName, "UTF-8"))
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
		} catch (UnsupportedEncodingException uee) {
			Log.error("wrong encoding", uee);
		}
	}

}
