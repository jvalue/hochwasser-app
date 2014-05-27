package de.bitdroid.flooding;

import static de.bitdroid.flooding.ods.OdsSource.COLUMN_SERVER_ID;
import static de.bitdroid.flooding.ods.OdsSource.COLUMN_SYNC_STATUS;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

import de.bitdroid.flooding.pegelonline.PegelonlineTableAdapter;
import de.bitdroid.flooding.utils.Log;


public abstract class StationsLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

	public static final int ODS_LOADER_ID = 42;
	private final Context context;

	public StationsLoaderCallbacks(Context context) {
		if (context == null) throw new NullPointerException("context cannot be null");
		this.context = context;
	}


	@Override
	public final Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		Log.debug("onCreateLoader called");
		if (id != ODS_LOADER_ID) return null;
		return new CursorLoader(
				context,
				new PegelonlineTableAdapter().toUri(),
				new String[] {
					COLUMN_SERVER_ID,
					COLUMN_SYNC_STATUS
				}, null, null, null);
	}

	@Override
	public final void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader.getId() != ODS_LOADER_ID) return;
		onLoadFinishedHelper(loader, cursor);
	}

	protected abstract void onLoadFinishedHelper(Loader<Cursor> loader, Cursor cursor);


	@Override
	public final void onLoaderReset(Loader<Cursor> loader) {
		if (loader.getId() != ODS_LOADER_ID) return;
		onLoaderResetHelper(loader);
	}

	protected abstract void onLoaderResetHelper(Loader<Cursor> loader);

}
