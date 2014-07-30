package de.bitdroid.flooding.news;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Pair;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.main.MainActivity;
import de.bitdroid.flooding.utils.ShowcaseSeries;
import de.timroes.android.listview.EnhancedListView;


public final class NewsFragment extends Fragment implements AbsListView.MultiChoiceModeListener,
		LoaderManager.LoaderCallbacks<Map<NewsItem, Boolean>> {

	private static final int LOADER_ID = 40;

	private static final SimpleDateFormat dateFormatter
			= new SimpleDateFormat("dd/M/yyyy hh:mm a");

	private EnhancedListView listView;
	private ArrayAdapter<Pair<NewsItem, Boolean>> listAdapter;
	private ShowcaseView currentShowcaseView;
	private NewsManager manager;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		this.manager = NewsManager.getInstance(getActivity().getApplicationContext());
	}

	@Override
	public View onCreateView(
			LayoutInflater inflater, 
			ViewGroup container, 
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.news, container, false);
		listView = (EnhancedListView) view.findViewById(R.id.list);
		listAdapter = new ArrayAdapter<Pair<NewsItem, Boolean>>(
				getActivity().getApplicationContext(),
				R.layout.news_item,
				android.R.id.text1) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				LayoutInflater inflater
						= (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View view = inflater.inflate(R.layout.news_item, parent, false);

				Pair<NewsItem, Boolean> item = getItem(position);

				TextView title = (TextView) view.findViewById(R.id.news_title);
				TextView data = (TextView) view.findViewById(R.id.news_timestamp);
				TextView content = (TextView) view.findViewById(R.id.news_content);

				title.setText(item.first.getTitle());
				data.setText(dateFormatter.format(item.first.getTimestamp()));
				content.setText(item.first.getContent());

				return view;
			}
		};
		listView.setAdapter(listAdapter);
		listView.setEmptyView(view.findViewById(R.id.empty));

		// enable editing mode
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(this);

		// enable swiping and undo
		listView.setDismissCallback(new EnhancedListView.OnDismissCallback() {
			@Override
			public EnhancedListView.Undoable onDismiss(EnhancedListView listView, int pos) {
				final Pair<NewsItem, Boolean> item = listAdapter.getItem(pos);
				listAdapter.remove(item); // hack to stop list from flashing
				manager.removeItem(item.first);

				return new EnhancedListView.Undoable() {
					@Override
					public void undo() {
						manager.addItem(item.first, false);
					}
				};
			}
		});
		listView.enableSwipeToDismiss();
		listView.setUndoStyle(EnhancedListView.UndoStyle.COLLAPSED_POPUP);

		// enable navigation to other fragments
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				NewsItem item = listAdapter.getItem(pos).first;
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
		manager.markAllItemsRead();

		if (firstStart()) {
			addHelperNews();
			showHelperOverlay();
		}
	}


	@Override
	public void onResume() {
		super.onResume();
		getLoaderManager().initLoader(LOADER_ID, null, this);
	}


	@Override
	public void onStop() {
		super.onStop();
		if (currentShowcaseView != null) currentShowcaseView.hide();
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.news_menu, menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch(menuItem.getItemId()) {
			case R.id.news:
				addHelperNews();
				return true;

			case R.id.help:
				showHelperOverlay();
				return true;
		}
		return super.onOptionsItemSelected(menuItem);
	}



	private final List<NewsItem> selectedItems = new LinkedList<NewsItem>();

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.delete:
				for (NewsItem newsItem : selectedItems) {
					manager.removeItem(newsItem);
				}
				Toast.makeText(
						getActivity(), 
						getActivity().getString(R.string.news_deleted, selectedItems.size()), 
						Toast.LENGTH_SHORT)
					.show();
				selectedItems.clear();

				mode.finish();
				return true;

			case R.id.select_all:
				selectedItems.clear();
				for (int i = 0; i < listAdapter.getCount(); i++) {
					listView.setItemChecked(i, true);
				}
				return true;

		}
		return false;
	}


	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.news_action_mode_menu, menu);
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
		NewsItem item = listAdapter.getItem(position).first;
		if (checked) selectedItems.add(item);
		else selectedItems.remove(item);
		mode.setTitle(getActivity().getString(R.string.news_selected, selectedItems.size()));
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
				return  -1 * Long.valueOf(item1.first.getTimestamp())
						.compareTo(item2.first.getTimestamp());
			}
		});

		listAdapter.clear();
		listAdapter.addAll(sortedItems);
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
		manager.addItem(getString(R.string.news_intro_alarms_title), getString(R.string.news_intro_alarms_content), 1, false);
		manager.addItem(getString(R.string.news_intro_data_title), getString(R.string.news_intro_data_content), 2, false);
	}


	private void showHelperOverlay() {
		new ShowcaseSeries() {
			@Override
			public ShowcaseView getShowcase(int id) {
				Activity activity = getActivity();
				Target target;
				switch(id) {
					case 0:
						target = new ActionViewTarget(activity, ActionViewTarget.Type.TITLE);
						currentShowcaseView = new ShowcaseView.Builder(activity)
							.setTarget(target)
							.setContentTitle(R.string.help_news_welcome_title)
							.setContentText(R.string.help_news_welcome_content)
							.setStyle(R.style.CustomShowcaseTheme)
							.build();
						break;

					case 1:
						View view = listView.getChildAt(0);
						if (view == null) view = listView.getEmptyView();
						target = new ViewTarget(view);
						currentShowcaseView = new ShowcaseView.Builder(activity)
							.setTarget(target)
							.setContentTitle(R.string.help_news_news_title)
							.setContentText(R.string.help_news_news_content)
							.setStyle(R.style.CustomShowcaseTheme)
							.build();
						break;

					default:
						currentShowcaseView = null;
				}
				return currentShowcaseView;
			}
		}.start();
	}
}
