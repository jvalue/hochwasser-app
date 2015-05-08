package de.bitdroid.flooding.news;

import de.bitdroid.flooding.utils.BaseAndroidTestCase;
import de.bitdroid.flooding.utils.PrefsRenamingDelegatingContext;
import de.bitdroid.flooding.utils.SharedPreferencesHelper;


public final class NewsManagerTest extends BaseAndroidTestCase {

	private static final String PREFIX = NewsManagerTest.class.getSimpleName();

	private int newItemCounter, deletedItemCounter, readCounter;


	@Override
	public void beforeClass() {
		setContext(new PrefsRenamingDelegatingContext(getContext(), PREFIX));
	}

	@Override
	public void beforeTest() {
		SharedPreferencesHelper.clearAll((PrefsRenamingDelegatingContext) getContext());
		newItemCounter = 0;
		deletedItemCounter = 0;
		readCounter = 0;
	}


	public void testCRUD() {
		NewsManager manager = NewsManager.getInstance(getContext());

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


	public void testListener() {
		NewsManager manager = NewsManager.getInstance(getContext());
		final NewsItem item = new NewsItem.Builder("hello", "world", 41).build();

		manager.registerListener(new NewsUpdateListener() {
			@Override
			public void onNewItem(NewsItem newItem) {
				assertEquals(item, newItem);
				newItemCounter++;
			}

			@Override
			public void onDeletedItem(NewsItem deletedItem) {
				assertEquals(item, deletedItem);
				deletedItemCounter++;
			}

			@Override
			public void onAllItemsRead() {
				readCounter++;
			}
		});

		manager.addItem(item, false);
		assertEquals(1, newItemCounter);
		assertEquals(0, deletedItemCounter);
		assertEquals(0, readCounter);

		manager.markAllItemsRead();
		assertEquals(1, newItemCounter);
		assertEquals(0, deletedItemCounter);
		assertEquals(1, readCounter);

		manager.removeItem(item);
		assertEquals(1, newItemCounter);
		assertEquals(1, deletedItemCounter);
		assertEquals(1, readCounter);
	}

}
