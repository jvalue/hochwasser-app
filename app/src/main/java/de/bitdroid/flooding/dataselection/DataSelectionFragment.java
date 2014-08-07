package de.bitdroid.flooding.dataselection;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.map.SelectionMapActivity;
import de.bitdroid.ods.data.OdsSourceManager;
import de.bitdroid.ods.data.SyncAdapter;
import de.bitdroid.utils.Assert;
import de.bitdroid.utils.Log;

abstract class DataSelectionFragment<T> extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	static final String
		EXTRA_ACTIVITY = "EXTRA_ACTIVITY",
		EXTRA_ANIM_EXIT = "EXTRA_ANIM_EXIT",
		EXTRA_ANIM_ENTER = "EXTRA_ANIM_ENTER";

	static Bundle getDefaultExtras(Class<?> activityClass, int animEnter, int animExit) {
		Assert.assertNotNull(activityClass);
		Bundle extras = new Bundle();
		extras.putString(EXTRA_ACTIVITY, activityClass.getName());
		extras.putInt(EXTRA_ANIM_ENTER, animEnter);
		extras.putInt(EXTRA_ANIM_EXIT, animExit);
		return extras;
	}


	private ArrayAdapter<T> listAdapter = null;
	private EditText searchBox = null;
	private ProgressBar emptyProgressBar;
	private TextView emptyTextView;


	protected abstract ArrayAdapter<T> getAdapter();
	protected abstract Intent getActivityIntent(Class<?> activityClass, T item);

	protected abstract int getLoaderId();
	protected abstract Loader<Cursor> getLoader();
	protected abstract void onLoadFinished(Cursor cursor, ArrayAdapter<T> listAdapter);

	protected abstract void addMapExtras(Intent intent);


	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}


    @Override
	public final View onCreateView(
			LayoutInflater inflater,
			ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.data_list, container, false);

		// search box
		searchBox = (EditText) view.findViewById(R.id.search);
		searchBox.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence text, int arg1, int arg2, int arg3) {
				listAdapter.getFilter().filter(text);
			}

			@Override
			public void beforeTextChanged(CharSequence text, int arg1, int arg2, int arg3) { }

			@Override
			public void afterTextChanged(Editable e) { }
		});

		// empty view
		this.emptyProgressBar = (ProgressBar) view.findViewById(R.id.empty_progressBar);
		this.emptyTextView = (TextView) view.findViewById(R.id.empty_text);

		OdsSourceManager manager = OdsSourceManager.getInstance(getActivity().getApplicationContext());
		if (manager.isSyncRunning()) showSyncRunning();
		else showSyncStopped();

		return view;
    }


	@Override
	public final void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		listAdapter = getAdapter();
		setListAdapter(listAdapter);
	}


	@Override
	public final void onResume() {
		super.onResume();
		getLoaderManager().initLoader(getLoaderId(), null, this);

		// listen for sync started / stopped
		IntentFilter filter = new IntentFilter();
		filter.addAction(SyncAdapter.ACTION_SYNC_ALL_START);
		filter.addAction(SyncAdapter.ACTION_SYNC_ALL_FINISH);
		getActivity().registerReceiver(receiver, filter);
	}


	@Override
	public void onPause() {
		super.onPause();

		getActivity().unregisterReceiver(receiver);
	}
	

	@Override
	public final void onListItemClick(ListView list, View item, int position, long id) {
		try {
			Class<?> activityClass = Class.forName(getArguments().getString(EXTRA_ACTIVITY));
			startActivity(getActivityIntent(activityClass, listAdapter.getItem(position)));
		} catch (ClassNotFoundException cnfe) {
			Log.error("failed to start activity", cnfe);
		}
	}


	@Override
	public final void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.select_water_menu, menu);
	}


	@Override
	public final boolean onOptionsItemSelected(MenuItem menuItem) {
		switch(menuItem.getItemId()) {
			case R.id.search:
				InputMethodManager inputManager = (InputMethodManager) getActivity()
					.getSystemService(Service.INPUT_METHOD_SERVICE);
				if (searchBox.getVisibility() == View.GONE) {
					searchBox.setVisibility(View.VISIBLE);
					inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
					inputManager.showSoftInput(searchBox, InputMethodManager.SHOW_IMPLICIT);
					searchBox.requestFocus();
				} else {
					searchBox.setVisibility(View.GONE);
					inputManager.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
				}
				return true;

			case R.id.map:
				Intent mapIntent = new Intent(getActivity().getApplicationContext(), SelectionMapActivity.class);
				mapIntent.putExtra(SelectionMapActivity.EXTRA_ACTIVITY_CLASS_NAME, getArguments().getString(EXTRA_ACTIVITY));
				mapIntent.putExtra(SelectionMapActivity.EXTRA_ANIM_ENTER, getArguments().getInt(EXTRA_ANIM_ENTER));
				mapIntent.putExtra(SelectionMapActivity.EXTRA_ANIM_EXIT, getArguments().getInt(EXTRA_ANIM_EXIT));
				addMapExtras(mapIntent);
				startActivity(mapIntent);
				return true;
		}
		return super.onOptionsItemSelected(menuItem);
	}


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		if (id != getLoaderId()) return null;
		return getLoader();
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader.getId() != getLoaderId()) return;
		onLoadFinished(cursor, listAdapter);
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		listAdapter.clear();
	}


	private void showSyncRunning() {
		emptyProgressBar.setVisibility(View.VISIBLE);
		emptyTextView.setText(getString(R.string.data_empty_loading));
	}


	private void showSyncStopped() {
		emptyProgressBar.setVisibility(View.GONE);
		emptyTextView.setText(getString(R.string.data_empty));
	}


	@Override
	public void startActivity(Intent intent) {
		// hide keyboard
		InputMethodManager inputManager
				= (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);

		// start activity
		super.startActivity(intent);
		getActivity().overridePendingTransition(
				getArguments().getInt(EXTRA_ANIM_ENTER),
				getArguments().getInt(EXTRA_ANIM_EXIT));
	}

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (listAdapter != null && !listAdapter.isEmpty()) {
				String action = intent.getAction();
				if (action.equals(SyncAdapter.ACTION_SYNC_ALL_START)) showSyncRunning();
				else showSyncStopped();
			}
		}
	};
}
