package de.bitdroid.flooding.utils;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;


public abstract class AbstractLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

	private final int loaderId;

	protected AbstractLoaderCallbacks(int loaderId) {
		this.loaderId = loaderId;
	}


	@Override
	public final Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		if (id != loaderId) return null;
		return getCursorLoader();
	}

	@Override
	public final void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader.getId() != loaderId) return;
		onLoadFinishedHelper(loader, cursor);
	}

	@Override
	public final void onLoaderReset(Loader<Cursor> loader) {
		if (loader.getId() != loaderId) return;
		onLoaderResetHelper(loader);
	}


	protected abstract void onLoadFinishedHelper(Loader<Cursor> loader, Cursor cursor);
	protected abstract void onLoaderResetHelper(Loader<Cursor> loader);
	protected abstract Loader<Cursor> getCursorLoader();

}
