package de.bitdroid.flooding.ui;

import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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

import java.util.List;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.auth.LoginManager;
import de.bitdroid.flooding.auth.RestrictedResource;
import de.bitdroid.flooding.network.AbstractErrorAction;
import de.bitdroid.flooding.network.NetworkUtils;
import roboguice.fragment.provided.RoboListFragment;
import rx.Observable;
import rx.functions.Action1;
import timber.log.Timber;


abstract class AbstractSelectionFragment<T> extends RoboListFragment implements RestrictedResource {

	private final int searchHintStringId;

	@Inject private LoginManager loginManager;
	@Inject private NetworkUtils networkUtils;

	private ArrayAdapter<T> listAdapter = null;
	private MenuItem searchMenuItem;
	private EditText searchBox = null;

	AbstractSelectionFragment(int searchHintStringId) {
		this.searchHintStringId = searchHintStringId;
	}

	protected abstract ArrayAdapter<T> getAdapter();
	protected abstract void onItemClicked(T item);
	protected abstract void onMapClicked();
	protected abstract Observable<List<T>> loadItems();


	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}


    @Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_list_waters, container, false);
    }


	@Override
	public final void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		listAdapter = getAdapter();
		setListAdapter(listAdapter);

		loadItems()
				.compose(networkUtils.<List<T>>getDefaultTransformer())
				.subscribe(new Action1<List<T>>() {
					@Override
					public void call(List<T> items) {
						listAdapter.clear();
						listAdapter.addAll(items);
					}
				}, new AbstractErrorAction(AbstractSelectionFragment.this) {
					@Override
					protected void doCall(Throwable throwable) {
						Timber.e("failed to load data");
					}
				});
	}


	@Override
	public void onPause() {
		super.onPause();
		MenuItemCompat.collapseActionView(searchMenuItem);
	}


	@Override
	public final void onListItemClick(ListView list, View item, int position, long id) {
		hideKeyboard();
		onItemClicked(listAdapter.getItem(position));
	}


	@Override
	public final void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_select_water, menu);

		final ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
		searchMenuItem = menu.findItem(R.id.search);
		MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				actionBar.setIcon(R.drawable.ic_action_search);
				showKeyboard();
				searchBox.requestFocus();
				// TODO show empty view
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				actionBar.setDisplayHomeAsUpEnabled(true);
				actionBar.setHomeButtonEnabled(true);
				actionBar.setDisplayShowHomeEnabled(true);
				actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
				searchBox.setText("");
				hideKeyboard();
				return true;
			}
		});

		// MenuItemCompat.setActionView(searchMenuItem, R.layout.search);
		searchBox = (EditText) MenuItemCompat.getActionView(searchMenuItem).findViewById(R.id.search_box);
		searchBox.setHint(getString(searchHintStringId));
		searchBox.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				listAdapter.getFilter().filter(s);
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
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
	public void logout() {
		loginManager.clearToken();
		loginManager.clearAccountName();
		Intent intent = new Intent(getActivity(), LoginActivity.class);
		startActivity(intent);
		getActivity().finish();
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

}

