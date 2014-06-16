package de.bitdroid.flooding.levels;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;

import de.bitdroid.flooding.monitor.SourceMonitor;
import de.bitdroid.flooding.ods.OdsSource;


final class MonitorSourceLoader extends AsyncTaskLoader<Cursor> {

	private final Context context;
	private final OdsSource source;
	private final String[] projection;
	private final String selection;
	private final String[] selectionArgs;
	private final String sortOrder;

	private Cursor cursor;

	public MonitorSourceLoader(
			Context context,
			OdsSource source,
			String[] projection,
			String selection,
			String[] selectionArgs,
			String sortOrder) {

		super(context);

		this.context = context;
		this.source = source;
		this.projection = projection;
		this.selection = selection;
		this.selectionArgs = selectionArgs;
		this.sortOrder = sortOrder;
	}


	@Override
	public Cursor loadInBackground() {
		return SourceMonitor
			.getInstance(context)
			.query(source, projection, selection, selectionArgs, sortOrder);
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
	}


	private void release(Cursor cursor) {
		if (cursor != null && !cursor.isClosed()) cursor.close();
	}

}
