package de.bitdroid.flooding.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;

public class ContentProviderContext extends ContextWrapper {

	private final ContentResolver resolver;

	public ContentProviderContext(Context targetContext, ContentResolver resolver) {
		super(targetContext);
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
