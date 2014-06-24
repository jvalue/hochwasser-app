package de.bitdroid.flooding.news;

import java.util.LinkedList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import de.bitdroid.flooding.MainActivity;
import de.bitdroid.flooding.R;
import de.timroes.android.listview.EnhancedListView;


public final class NewsFragment extends Fragment implements AbsListView.MultiChoiceModeListener {

	private NewsListAdapter listAdapter;
	private EnhancedListView listView;

	@Override
	public View onCreateView(
			LayoutInflater inflater, 
			ViewGroup container, 
			Bundle savedInstanceState) {

		// view
		View view = inflater.inflate(R.layout.news, container, false);
		listView = (EnhancedListView) view.findViewById(R.id.list);
		listAdapter = new NewsListAdapter(getActivity().getApplicationContext());
		listView.setAdapter(listAdapter);

		// enable editing mode
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(this);

		// enable swiping and undo
		listView.setDismissCallback(new EnhancedListView.OnDismissCallback() {
			@Override
			public EnhancedListView.Undoable onDismiss(EnhancedListView listView, int pos) {
				final NewsItem item = listAdapter.getItem(pos);
				listAdapter.removeItem(item);
				return new EnhancedListView.Undoable() {
					@Override
					public void undo() {
						listAdapter.addItem(item);
					}
				};
			}
		});
		listView.enableSwipeToDismiss();

		// enable navigation to other fragments
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				NewsItem item = listAdapter.getItem(pos);
				if (!item.isNavigationEnabled()) return;

				Intent intent = new Intent(MainActivity.ACTION_NAVIGATE);
				intent.putExtra(MainActivity.EXTRA_POSITION, item.getNavigationPos());
				getActivity().sendBroadcast(intent);
			}
		});


		return view;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		NewsManager.getInstance(getActivity().getApplicationContext()).markAllItemsRead();


		if (firstStart()) addHelperNews();
	}


	private final List<NewsItem> selectedItems = new LinkedList<NewsItem>();

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.delete:
				for (NewsItem newsItem : selectedItems) {
					listAdapter.removeItem(newsItem);
				}
				Toast.makeText(
						getActivity(), 
						getActivity().getString(R.string.news_deleted, selectedItems.size()), 
						Toast.LENGTH_SHORT)
					.show();
				selectedItems.clear();

				mode.finish();
				return true;
		}
		return false;
	}


	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.news_list_menu, menu);
		listView.disableSwipeToDismiss();
		return true;
	}


	@Override
	public void onDestroyActionMode(ActionMode mode) {
		listView.enableSwipeToDismiss();
		selectedItems.clear();
	}


	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}


	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
		NewsItem item = listAdapter.getItem(position);
		if (checked) selectedItems.add(item);
		else selectedItems.remove(item);
		mode.setTitle(getActivity().getString(R.string.news_selected, selectedItems.size()));
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
		NewsManager manager = NewsManager.getInstance(getActivity().getApplicationContext());
		manager.addItem(new NewsItem.Builder(
				"Alarms",
				"If you want to be alarmed when water levels reach a certain level, head over to the alarms section!",
				System.currentTimeMillis())
			.setNavigationPos(1)
			.build(),
			false);

		manager.addItem(new NewsItem.Builder(
				"Data",
				"Want more details about the current water sitation? Check our the data section!",
				System.currentTimeMillis())
			.setNavigationPos(2)
			.build(),
			false);
		listAdapter.notifyDataSetInvalidated();
	}
}
