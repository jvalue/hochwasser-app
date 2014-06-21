package de.bitdroid.flooding;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import de.bitdroid.flooding.alarms.AlarmsFragment;
import de.bitdroid.flooding.levels.ChooseRiverFragment;
import de.bitdroid.flooding.monitor.SourceMonitor;
import de.bitdroid.flooding.news.NewsFragment;
import de.bitdroid.flooding.ods.GcmStatus;
import de.bitdroid.flooding.ods.OdsSource;
import de.bitdroid.flooding.ods.OdsSourceManager;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;

public class MainActivity extends Activity {

	private final String PREFS_KEY_FIRST_START = "FIRST_START";

	private DrawerLayout drawerLayout;
	private ListView drawerList;
	private ActionBarDrawerToggle drawerToggle;
	private CharSequence drawerTitle, fragmentTitle;

	private String[] navItems;
	private Fragment[] fragments;

	private int currentNavItem = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		drawerTitle = fragmentTitle = getTitle();
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
		drawerList = (ListView) findViewById(R.id.menu);

		navItems = getResources().getStringArray(R.array.nav_items);
		fragments = new Fragment[navItems.length];
		fragments[0] = new NewsFragment();
		fragments[1] = new AlarmsFragment();
		fragments[2] = new ChooseRiverFragment();
		fragments[3] = new SettingsFragment();

		// set drawer shadow
		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		// set nav items
		drawerList.setAdapter(new ArrayAdapter<String>(
					getApplicationContext(),
					R.layout.nav_item,
					navItems));
		drawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				navigateTo(position);
			}
		});

		// app icon toggles drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// drawer listener for options menus and title
		drawerToggle = new ActionBarDrawerToggle(
				this,
				drawerLayout,
				R.drawable.ic_drawer,
				R.string.nav_open,
				R.string.nav_close) {
			@Override
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(fragmentTitle);
				invalidateOptionsMenu();
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(drawerTitle);
				invalidateOptionsMenu();
			}
		};
		drawerLayout.setDrawerListener(drawerToggle);

		// move to home screen
		navigateTo(currentNavItem);

		// load default pref values
		PreferenceManager.setDefaultValues(
				getApplicationContext(),
				R.xml.preferences,
				false);
		SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(getApplicationContext());


		// set ODS server name
		OdsSourceManager sourceManager = OdsSourceManager.getInstance(getApplicationContext());
		if (sourceManager.getOdsServerName() == null) {
			sourceManager.setOdsServerName(
					prefs.getString(getString(R.string.prefs_ods_servername_key), null));
		}


		// monitor setup
		OdsSource source = PegelOnlineSource.INSTANCE;
		boolean enabled = prefs.getBoolean(getString(R.string.prefs_ods_monitor_key), false);
		SourceMonitor monitor = SourceMonitor.getInstance(getApplicationContext());
		if (enabled && !monitor.isBeingMonitored(source)) {
			monitor.startMonitoring(source);
		}

		OdsSourceManager manager = OdsSourceManager.getInstance(getApplicationContext());
		GcmStatus status = manager.getPushNotificationsRegistrationStatus(source);
		if (!status.equals(GcmStatus.REGISTERED)) {
			manager.startPushNotifications(source);
		}

	
		// start first manual sync
		boolean firstStart = prefs.getBoolean(PREFS_KEY_FIRST_START, true);
		if (firstStart) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean(PREFS_KEY_FIRST_START, false);
			editor.commit();

			manager.startManualSync(source);
		}
	}


	@Override
	public void setTitle(CharSequence title) {
		fragmentTitle = title;
		getActionBar().setTitle(title);
	}


	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
		// disable / enable entries
		menu.findItem(R.id.select_about).setVisible(drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		switch(item.getItemId()) { 
			case R.id.select_about:
				OdsSourceManager manager = OdsSourceManager.getInstance(getApplicationContext());

				new AlertDialog.Builder(this)
					.setTitle(R.string.main_dialog_info_title)
					.setMessage(getString(R.string.main_dialog_info_msg,
								formatTime(manager.getLastSync(PegelOnlineSource.INSTANCE)),
								formatTime(manager.getLastSuccessfulSync(PegelOnlineSource.INSTANCE)),
								formatTime(manager.getLastFailedSync(PegelOnlineSource.INSTANCE))))
					.setPositiveButton(R.string.btn_ok, null)
					.show();

				return true;
		}

		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onResume() {
		super.onResume();
		if (!arePlayServicesAvailable()) finish();
	}


	private static final String EXTRA_CURRENT_NAV_ITEM = "EXTRA_CURRENT_NAV_ITEM";

	@Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		state.putInt(EXTRA_CURRENT_NAV_ITEM, currentNavItem);
	}
	

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		currentNavItem = state.getInt(EXTRA_CURRENT_NAV_ITEM);
		navigateTo(currentNavItem);
	}


	private void navigateTo(int position) {
		currentNavItem = position;
		getFragmentManager().beginTransaction().replace(R.id.frame, fragments[position]).commit();
		drawerList.setItemChecked(position, true);
		setTitle(navItems[position]);
		drawerLayout.closeDrawer(drawerList);
	}


	private boolean arePlayServicesAvailable() {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		if (status == ConnectionResult.SUCCESS) return true;

		if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, 42);
			dialog.show();
		}

		return false;
	}


	private final static SimpleDateFormat dateFormatter 
		= new SimpleDateFormat("dd/M/yyyy hh:mm a");

	private String formatTime(Calendar time) {
		if (time == null) return getString(R.string.main_dialog_info_never);
		else return dateFormatter.format(time.getTime());
	}

}
