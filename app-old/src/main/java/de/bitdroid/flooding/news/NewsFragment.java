package de.bitdroid.flooding.news;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.bitdroid.flooding.R;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.dismissanimation.SwipeDismissAnimation;
import it.gmariotti.cardslib.library.view.CardListView;


public final class NewsFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Map<NewsItem, Boolean>> {

	private static final int LOADER_ID = 40;

	private CardListView listView;
	private CardArrayAdapter listAdapter;
	private SwipeDismissAnimation dismissAnimation;
	private NewsManager manager;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.manager = NewsManager.getInstance(getActivity().getApplicationContext());
	}

	@Override
	public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
		return inflater.inflate(R.layout.news, container, false);
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		manager.markAllItemsRead();

		listView = (CardListView) getActivity().findViewById(R.id.list);
		listView.setEmptyView(getActivity().findViewById(R.id.empty));
		listAdapter = new CardArrayAdapter(getActivity(), new ArrayList<Card>()) {
			@Override
			public void setEnableUndo(boolean enableUndo) {
				Map<String, Card> oldInternalObjects  = mInternalObjects;
				super.setEnableUndo(enableUndo);
				if (oldInternalObjects != null) mInternalObjects.putAll(oldInternalObjects);
			}

		};

		listAdapter.setEnableUndo(true);

		dismissAnimation = (SwipeDismissAnimation) new SwipeDismissAnimation(getActivity()).setup(listAdapter);

		listView.setAdapter(listAdapter);


		if (firstStart()) {
			addHelperNews();
		}
	}


	@Override
	public void onResume() {
		super.onResume();
		getLoaderManager().initLoader(LOADER_ID, null, this);
	}


	@Override
	public Loader<Map<NewsItem, Boolean>> onCreateLoader(int id, Bundle bundle) {
		if (id != LOADER_ID) return null;
		return new NewsLoader(getActivity().getApplicationContext());
	}


	@Override
	public void onLoadFinished(Loader<Map<NewsItem, Boolean>> loader, Map<NewsItem, Boolean> items) {
		if (loader.getId() != LOADER_ID) return;

		List<Pair<NewsItem, Boolean>> sortedItems = new LinkedList<Pair<NewsItem, Boolean>>();
		for (Map.Entry<NewsItem, Boolean> entry : items.entrySet()) {
			sortedItems.add(new Pair<NewsItem, Boolean>(entry.getKey(), entry.getValue()));
		}

		Collections.sort(sortedItems, new Comparator<Pair<NewsItem, Boolean>>() {
			@Override
			public int compare(Pair<NewsItem, Boolean> item1, Pair<NewsItem, Boolean> item2) {
				return -1 * Long.valueOf(item1.first.getTimestamp())
						.compareTo(item2.first.getTimestamp());
			}
		});

		listAdapter.clear();
		for (Pair<NewsItem, Boolean> item : sortedItems) {
			NewsCard card =  new NewsCard(getActivity(), manager, item, dismissAnimation);
			card.setId(String.valueOf(item.first.getTimestamp()));
			listAdapter.add(card);
		}

		listAdapter.setEnableUndo(true);
	}


	@Override
	public void onLoaderReset(Loader<Map<NewsItem, Boolean>> loader) {
		listAdapter.clear();
	}


	private static final String PREFS_NAME = "de.bitdroid.flooding.news.NewsFragment";
	private static final String KEY_FIRST_START = "KEY_FIRST_START";

	private boolean firstStart() {
		SharedPreferences prefs = getActivity().getSharedPreferences(
				PREFS_NAME,
				Context.MODE_PRIVATE);
		boolean firstStart = prefs.getBoolean(KEY_FIRST_START, true);
		if (!firstStart) return false;

		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(KEY_FIRST_START, false);
		editor.commit();
		return true;
	}


	private void addHelperNews() {
		manager.addItem(getString(R.string.news_intro_recording_title), getString(R.string.news_intro_recording_content), 3, true, false);
		manager.addItem(getString(R.string.news_intro_data_title), getString(R.string.news_intro_data_content), 2, true, false);
		manager.addItem(getString(R.string.news_intro_alarms_title), getString(R.string.news_intro_alarms_content), 1, true, false);
	}

}
