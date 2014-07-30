package de.bitdroid.flooding.news;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;


public final class NewsManagerTest extends AndroidTestCase {


	public void testCRUD() {
		Context context = new RenamingDelegatingContext(getContext(), "test_");
		NewsManager manager = NewsManager.getInstance(context);

		assertTrue(manager.getAllItems().isEmpty());

		manager.markAllItemsRead();
		assertTrue(manager.getAllItems().isEmpty());

		NewsItem i1 = new NewsItem.Builder("hello", "world", 21).build();
		NewsItem i2 = new NewsItem.Builder("hello", "world", 21).setNavigationPos(42).build();

		manager.addItem(i1, false);
		assertTrue(manager.getAllItems().containsKey(i1));
		assertFalse(manager.getAllItems().get(i1));
		assertTrue(manager.getAllItems().size() == 1);

		manager.addItem(i2, false);
		assertTrue(manager.getAllItems().containsKey(i1));
		assertTrue(manager.getAllItems().containsKey(i2));
		assertFalse(manager.getAllItems().get(i1));
		assertFalse(manager.getAllItems().get(i2));
		assertTrue(manager.getAllItems().size() == 2);

		manager.markAllItemsRead();
		assertTrue(manager.getAllItems().get(i1));
		assertTrue(manager.getAllItems().get(i2));

		manager.removeItem(i1);
		assertFalse(manager.getAllItems().containsKey(i1));
		assertTrue(manager.getAllItems().containsKey(i2));
		assertTrue(manager.getAllItems().size() == 1);

		manager.removeItem(i2);
		assertTrue(manager.getAllItems().size() == 0);
	}

}
