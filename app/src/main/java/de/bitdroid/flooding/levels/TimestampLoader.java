package de.bitdroid.flooding.levels;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.bitdroid.flooding.monitor.SourceMonitor;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.ods.data.OdsSource;
import timber.log.Timber;

import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_TIMESTAMP;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_TYPE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_WATER_NAME;
import static de.bitdroid.ods.data.OdsSource.COLUMN_TIMESTAMP;


/**
 * Loads all timestamps for a given river, including the most recent timestamp in
 * the regular ODS database.
 */
public final class TimestampLoader extends AsyncTaskLoader<Cursor> {

	private final SourceMonitor sourceMonitor;
	private final String waterName;

	private final OdsSource source;

	private Cursor cursor;
	private ContentObserver contentObserver;

	public TimestampLoader(Context context, SourceMonitor sourceMonitor, String waterName) {
		super(context);

		this.sourceMonitor = sourceMonitor;
		this.waterName = waterName;

		this.source = PegelOnlineSource.INSTANCE;
	}


	@Override
	public Cursor loadInBackground() {
		MatrixCursor resultCursor = new MatrixCursor(new String[] { COLUMN_LEVEL_TIMESTAMP} );

		Set<Long> timestamps = new HashSet<Long>();
		timestamps.addAll(sourceMonitor.getAvailableTimestamps(source));

		Cursor currentDataCursor = getContext().getContentResolver().query(
				source.toUri(),
				new String[] { COLUMN_TIMESTAMP },
				COLUMN_WATER_NAME + "=? AND " + COLUMN_LEVEL_TYPE + "=?",
				new String[] { waterName, "W" },
				null);

		currentDataCursor.moveToFirst();
		if (currentDataCursor.getCount() == 0) return resultCursor;

		timestamps.add(currentDataCursor.getLong(currentDataCursor.getColumnIndex(COLUMN_TIMESTAMP)));
		currentDataCursor.close();

		List<Long> sortedTimemstamps = new LinkedList<Long>();
		sortedTimemstamps.addAll(timestamps);
		Collections.sort(sortedTimemstamps);

		for (Long timestamp : sortedTimemstamps) {
			resultCursor.addRow(new Object[] { Long.valueOf(timestamp) });
		}

		return resultCursor;
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
					TimestampLoader.this.onContentChanged();
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


	private void release(Cursor cursor) {
		if (cursor != null && !cursor.isClosed()) cursor.close();
	}

}
