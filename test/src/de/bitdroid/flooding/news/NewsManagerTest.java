package de.bitdroid.flooding.news;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;


public final class NewsManagerTest extends AndroidTestCase {


	public void testCRUD() {
		Context context = new RenamingDelegatingContext(getContext(), "test_");
		NewsManager manager = NewsManager.getInstance(context);

		assertTrue(manager.getAllItems().isEmpty());
		assertTrue(manager.getUnreadItems().isEmpty());
		assertTrue(manager.getReadItems().isEmpty());

		manager.markAllItemsRead();
		assertTrue(manager.getAllItems().isEmpty());

		NewsItem i1 = new NewsItem.Builder("hello", "world", 21).build();
		NewsItem i2 = new NewsItem.Builder("hello", "world", 21).setNavigationPos(42).build();

		manager.addItem(i1, false);
		assertTrue(manager.getAllItems().contains(i1));
		assertTrue(manager.getAllItems().size() == 1);
		assertTrue(manager.getUnreadItems().size() == 1);
		assertTrue(manager.getReadItems().size() == 0);

		manager.addItem(i2, false);
		assertTrue(manager.getAllItems().contains(i1));
		assertTrue(manager.getAllItems().contains(i2));
		assertTrue(manager.getAllItems().size() == 2);
		assertTrue(manager.getUnreadItems().size() == 2);
		assertTrue(manager.getReadItems().size() == 0);

		manager.markAllItemsRead();
		assertTrue(manager.getUnreadItems().size() == 0);
		assertTrue(manager.getReadItems().size() == 2);

		manager.removeItem(i1);
		assertTrue(!manager.getAllItems().contains(i1));
		assertTrue(!manager.getReadItems().contains(i1));
		assertTrue(manager.getAllItems().size() == 1);

		manager.removeItem(i2);
		assertTrue(!manager.getAllItems().contains(i2));
		assertTrue(manager.getAllItems().size() == 0);
	}

}
