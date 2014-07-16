package de.bitdroid.flooding.utils;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.test.AndroidTestCase;


public final class AbstractLoaderCallbacksTest extends AndroidTestCase {

	private int 
		onLoadFinishedHelperCount = 0,
		onLoaderResetHelperCount = 0;

	public void testLoaderId() {
		final int loaderId = 42;
		AbstractLoaderCallbacks callback = new AbstractLoaderCallbacks(loaderId) {

			@Override
			protected void onLoadFinishedHelper(Loader<Cursor> loader, Cursor cursor) {
				onLoadFinishedHelperCount++;
			}

			@Override
			protected void onLoaderResetHelper(Loader<Cursor> loader) {
				onLoaderResetHelperCount++;
			}

			@Override
			protected Loader<Cursor> getCursorLoader() {
				return getLoader(loaderId);
			}
		};

		assertEquals(0, onLoadFinishedHelperCount);
		assertEquals(0, onLoaderResetHelperCount);

		assertNull(callback.onCreateLoader(43, new Bundle()));
		assertNotNull(callback.onCreateLoader(loaderId, new Bundle()));

		callback.onLoadFinished(getLoader(43), null);
		assertEquals(0, onLoadFinishedHelperCount);
		callback.onLoadFinished(getLoader(loaderId), null);
		assertEquals(1, onLoadFinishedHelperCount);

		callback.onLoaderReset(getLoader(43));
		assertEquals(0, onLoaderResetHelperCount);
		callback.onLoaderReset(getLoader(loaderId));
		assertEquals(1, onLoaderResetHelperCount);
	}


	private Loader<Cursor> getLoader(final int loaderId) {
		return new CursorLoader(getContext()) {
			@Override
			public int getId() {
				return loaderId;
			}
		};
	}

}
