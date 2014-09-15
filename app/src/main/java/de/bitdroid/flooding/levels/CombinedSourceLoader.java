package de.bitdroid.flooding.levels;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;

import de.bitdroid.flooding.monitor.SourceMonitor;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.ods.data.OdsSource;
import timber.log.Timber;

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
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_NAME;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_WATER_NAME;
import static de.bitdroid.ods.data.OdsSource.COLUMN_TIMESTAMP;


public final class CombinedSourceLoader extends AsyncTaskLoader<Cursor> {

	public static final long MOST_CURRENT_TIMESTAMP = Long.MAX_VALUE;

	private final OdsSource source;
	private final String[] projection, selectionArgs;
	private final String selection;
	private long selectedTimestamp;

	private Cursor cursor;
	private ContentObserver contentObserver;

	public CombinedSourceLoader(Context context, String waterName) {
		super(context);

		this.source = PegelOnlineSource.INSTANCE;
		this.projection = new String[] {
						COLUMN_STATION_NAME,
						COLUMN_STATION_KM,
						COLUMN_LEVEL_TIMESTAMP,
						COLUMN_LEVEL_VALUE,
						COLUMN_LEVEL_UNIT,
						COLUMN_LEVEL_ZERO_VALUE,
						COLUMN_LEVEL_ZERO_UNIT,
						COLUMN_CHARVALUES_MW_VALUE,
						COLUMN_CHARVALUES_MW_UNIT,
						COLUMN_CHARVALUES_MHW_VALUE,
						COLUMN_CHARVALUES_MHW_UNIT,
						COLUMN_CHARVALUES_MNW_VALUE,
						COLUMN_CHARVALUES_MNW_UNIT,
						COLUMN_CHARVALUES_MTHW_VALUE,
						COLUMN_CHARVALUES_MTHW_UNIT,
						COLUMN_CHARVALUES_MTNW_VALUE,
						COLUMN_CHARVALUES_MTNW_UNIT,
						COLUMN_CHARVALUES_HTHW_VALUE,
						COLUMN_CHARVALUES_HTHW_UNIT,
						COLUMN_CHARVALUES_NTNW_VALUE,
						COLUMN_CHARVALUES_NTNW_UNIT};
		this.selection = COLUMN_WATER_NAME + "=? AND " + COLUMN_LEVEL_TYPE + "=?";
		this.selectionArgs = new String[] { waterName, "W" };
		this.selectedTimestamp = MOST_CURRENT_TIMESTAMP;
	}


	@Override
	public Cursor loadInBackground() {

		if (selectedTimestamp == MOST_CURRENT_TIMESTAMP) {
			// get most current one
			Timber.i("getting most current data");
			return getContext().getContentResolver().query(
					source.toUri(),
					projection,
					selection,
					selectionArgs,
					null);

		} else {
			// get recorded data
			Timber.i("getting recorded data");
			String timestampSelection = COLUMN_TIMESTAMP + "=?";
			String timestampArg = String.valueOf(selectedTimestamp);

			String selection = this.selection;
			if (selection == null) selection = timestampSelection;
			else selection = selection + " AND " + timestampSelection;

			String[] selectionArgs = new String[this.selectionArgs.length + 1];
			System.arraycopy(this.selectionArgs, 0, selectionArgs, 0, this.selectionArgs.length);
			selectionArgs[selectionArgs.length - 1] = timestampArg;

			return SourceMonitor
					.getInstance(getContext())
					.query(source, projection, selection, selectionArgs, null);
		}

	}


	@Override
	public void deliverResult(Cursor cursor) {
		if (isReset()) {
			release(cursor);
			return;
		}

		Cursor oldCursor = this.cursor;
		this.cursor = cursor;

		if (isStarted()) {
			super.deliverResult(cursor);
		}

		if (oldCursor != cursor) {
			release(oldCursor);
		}
	}


	@Override
	protected void onStartLoading() {
		if (cursor != null) deliverResult(cursor);
		if (contentObserver == null) {
			contentObserver = new ContentObserver(new Handler()) {
				@Override
				public void onChange(boolean selfChange) {
					CombinedSourceLoader.this.onContentChanged();
				}

				@Override
				public void onChange(boolean selfChange, Uri uri) {
					onChange(selfChange);
				}
			};
			getContext().getContentResolver().registerContentObserver(source.toUri(), true, contentObserver);

		}
		if (takeContentChanged() || cursor == null) forceLoad();
	}


	@Override
	protected void onStopLoading() {
		cancelLoad();
	}


	@Override
	public void onCanceled(Cursor cursor) {
		super.onCanceled(cursor);
		release(cursor);
	}


	@Override
	protected void onReset() {
		onStopLoading();

		release(cursor);
		cursor = null;

		if (contentObserver != null) {
			getContext().getContentResolver().unregisterContentObserver(contentObserver);
			contentObserver = null;
		}
	}


	public void setTimestamp(long timestamp) {
		this.selectedTimestamp = timestamp;
		onContentChanged();
	}


	private void release(Cursor cursor) {
		if (cursor != null && !cursor.isClosed()) cursor.close();
	}

}
