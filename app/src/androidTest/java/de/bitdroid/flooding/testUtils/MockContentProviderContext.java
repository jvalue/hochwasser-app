package de.bitdroid.flooding.testUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.test.RenamingDelegatingContext;

public class MockContentProviderContext extends RenamingDelegatingContext {

	private final ContentResolver resolver;

	public MockContentProviderContext(Context targetContext, ContentResolver resolver, String prefix) {
		super(targetContext, prefix);
		this.resolver = resolver;
	}

	@Override
	public ContentResolver getContentResolver() {
		return resolver;
	}

	@Override
	public Context getApplicationContext() {
		return this;
	}

}
