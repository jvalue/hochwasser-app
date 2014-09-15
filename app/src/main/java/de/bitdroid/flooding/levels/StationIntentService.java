package de.bitdroid.flooding.levels;


import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.ods.data.OdsSource;
import de.bitdroid.ods.data.OdsSourceManager;
import de.bitdroid.utils.RestCall;
import de.bitdroid.utils.RestException;
import timber.log.Timber;

public class StationIntentService extends IntentService {

	public static final String
			EXTRA_FORCE_SYNC = "de.bitdroid.flooding.levels.EXTRA_FORCE_SYNC",
			EXTRA_STATION_NAME = "de.bitdroid.flooding.levels.EXTRA_STATION_NAME",
			EXTRA_SYNC_STATUS_RECEIVER = "de.bitdroid.flooding.levels.EXTRA_SYNC_STATUS_RECEIVER";


	public StationIntentService() {
		super(StationIntentService.class.getSimpleName());
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		boolean forceSync = intent.getBooleanExtra(EXTRA_FORCE_SYNC, false);
		String[] stations = intent.getStringArrayExtra(EXTRA_STATION_NAME);
		ResultReceiver syncStatusReceiver = intent.getParcelableExtra(EXTRA_SYNC_STATUS_RECEIVER);

		String odsServerName = OdsSourceManager.getInstance(getApplicationContext()).getOdsServerName();
		long timestamp = System.currentTimeMillis();

		for (String station : stations) {
			Cursor cursor = getContentResolver().query(
					PegelOnlineSource.INSTANCE.toUri(),
					new String[]{OdsSource.COLUMN_TIMESTAMP, OdsSource.COLUMN_ID},
					PegelOnlineSource.COLUMN_STATION_NAME + "=? AND "
							+ PegelOnlineSource.COLUMN_LEVEL_TYPE + "=?",
					new String[]{station, "W"},
					null
			);

			cursor.moveToFirst();
			if (cursor.getCount() == 1) {
				Date lastMeasurement = new Date(cursor.getLong(0));
				long id = cursor.getLong(1);
				Date currentDate = new Date();

				int maxAge = getResources().getInteger(R.integer.station_max_age_in_ms);
				long diff = currentDate.getTime() - lastMeasurement.getTime();

				if (forceSync || diff > maxAge || stations.length > 1) synchronizeStation(odsServerName, station, true, id, timestamp);


			} else if (cursor.getCount() > 1) {
				Timber.w("Found more than one timestamp when querying ods db for station " + station
						+ " --> ignoring sync request");
			} else {
				synchronizeStation(odsServerName, station, false, 0, timestamp);
			}
			cursor.close();
		}

		if (syncStatusReceiver != null) syncStatusReceiver.send(0, null);
	}


	private void synchronizeStation(
			String odsServerName,
			String stationName,
			boolean oldValuePresent,
			long id,
			long timestamp) {

		try {
			PegelOnlineSource source = PegelOnlineSource.INSTANCE;
			String jsonResult = new RestCall.Builder(RestCall.RequestType.GET, odsServerName)
					.path(source.getSourceUrlPath())
					.path(URLEncoder.encode(stationName, "UTF-8"))
					.build()
					.execute();

			JSONObject json = new JSONObject(jsonResult);

			ContentValues contentValues = source.saveData(json, timestamp);

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
			Timber.e(re, "Failed to synchronize station " + stationName);
		} catch(JSONException je) {
			Timber.e(je, "Failed to synchronize station " + stationName);
		} catch (UnsupportedEncodingException uee) {
			Timber.e(uee, "Failed to synchronize station " + stationName);
		}
	}



	public static final class SyncStatusReceiver extends ResultReceiver {

		public interface SyncListener {
			public void onSyncFinished();
		}

		private boolean syncFinished = false;
		private SyncListener listener;

		public SyncStatusReceiver(Handler handler) {
			super(handler);
		}


		public void setSyncListener(SyncListener listener) {
			this.listener = listener;
		}


		@Override
		public void onReceiveResult(int resultCode, Bundle data) {
			syncFinished = true;
			if (listener != null) listener.onSyncFinished();
		}


		public boolean isSyncFinished() {
			return syncFinished;
		}


		public void resetSyncFinished() {
			syncFinished = false;
		}

	}
}
