package de.bitdroid.flooding.dataselection;

import android.app.ActionBar;
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
import de.bitdroid.ods.data.OdsSourceManager;
import de.bitdroid.ods.data.SyncAdapter;

abstract class BaseSelectionFragment<T> extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private ArrayAdapter<T> listAdapter = null;
	private ProgressBar emptyProgressBar;
	private TextView emptyTextView;
	private MenuItem searchMenuItem;
	private EditText searchBox = null;


	protected abstract ArrayAdapter<T> getAdapter();

	protected abstract int getLoaderId();
	protected abstract Loader<Cursor> getLoader();
	protected abstract void onLoadFinished(Cursor cursor, ArrayAdapter<T> listAdapter);

	protected abstract void onItemClicked(T item);
	protected abstract void onMapClicked();

	protected abstract int getSearchHintStringId();


	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}


    @Override
	public View onCreateView(
			LayoutInflater inflater,
			ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.data_list, container, false);

		// empty view
		this.emptyProgressBar = (ProgressBar) view.findViewById(R.id.empty_progressBar);
		this.emptyTextView = (TextView) view.findViewById(R.id.empty_text);

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

		// check if sync is already running
		OdsSourceManager manager = OdsSourceManager.getInstance(getActivity().getApplicationContext());
		if (manager.isSyncRunning()) showSyncRunning();
		else showSyncStopped();
	}


	@Override
	public void onPause() {
		super.onPause();

		getActivity().unregisterReceiver(receiver);
		searchMenuItem.collapseActionView();
	}
	

	@Override
	public final void onListItemClick(ListView list, View item, int position, long id) {
		hideKeyboard();
		// forward request to clients
		onItemClicked(listAdapter.getItem(position));
	}


	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		final ActionBar actionBar = getActivity().getActionBar();
		searchMenuItem = menu.findItem(R.id.search);
		searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				actionBar.setIcon(R.drawable.ic_action_search);
				showKeyboard();
				searchBox.requestFocus();
				emptyTextView.setText(getString(R.string.search_empty));
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				actionBar.setDisplayShowHomeEnabled(true);
				actionBar.setDisplayHomeAsUpEnabled(true);
				searchBox.setText("");
				hideKeyboard();
				emptyTextView.setText(getString(R.string.data_empty));
				return true;
			}
		});

		searchBox = (EditText) searchMenuItem.getActionView().findViewById(R.id.search_box);
		searchBox.setHint(getString(getSearchHintStringId()));
		searchBox.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				listAdapter.getFilter().filter(s);
			}

			@Override
			public void afterTextChanged(Editable s) { }
		});
	}


	@Override
	public final void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.select_water_menu, menu);
	}


	@Override
	public final boolean onOptionsItemSelected(MenuItem menuItem) {
		switch(menuItem.getItemId()) {
			case R.id.map:
				hideKeyboard();
				// forward request to clients
				onMapClicked();
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


	private void hideKeyboard() {
		InputMethodManager inputManager
				= (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
	}


	private void showKeyboard() {
		InputMethodManager inputManager
				= (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
		inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		inputManager.showSoftInput(searchBox, InputMethodManager.SHOW_IMPLICIT);
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
