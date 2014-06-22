package de.bitdroid.flooding;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
	private LinearLayout drawerMenu;
	private ListView drawerMenuList;
	private ActionBarDrawerToggle drawerToggle;
	private CharSequence drawerTitle, fragmentTitle;
	private TextView titleView;

	private String[] navItems;
	private int currentNavItem = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
		drawerMenu = (LinearLayout) findViewById(R.id.menu);
		drawerMenuList = (ListView) findViewById(R.id.menu_entries);
		titleView = (TextView) findViewById(Resources.getSystem().getIdentifier("action_bar_title", "id", "android"));

		navItems = getResources().getStringArray(R.array.nav_items);
		drawerTitle = getTitle();
		fragmentTitle = navItems[currentNavItem];

		// set drawer shadow
		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		// set nav items
		drawerMenuList.setAdapter(new ArrayAdapter<String>(
					getApplicationContext(),
					R.layout.nav_item,
					navItems));
		drawerMenuList.setOnItemClickListener(new ListView.OnItemClickListener() {
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
				invalidateOptionsMenu();
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				invalidateOptionsMenu();
			}

			@Override
			public void onDrawerSlide(View drawerView, float offset) {
				CharSequence title = null;
				int alpha = 0;
				if (offset <= 0.5) {
					alpha = (int) ((0.5 - offset) * 2 * 255);
					title = fragmentTitle;
				} else {
					alpha = (int) ((offset - 0.5) * 2 * 255);
					title = drawerTitle;
				}

				titleView.setText(title);
				titleView.setTextColor(titleView.getTextColors().withAlpha(alpha));
			}
		};
		drawerLayout.setDrawerListener(drawerToggle);

		// move to home screen
		navigateTo(currentNavItem);
		setTitle(fragmentTitle);

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
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean drawerOpen = drawerLayout.isDrawerOpen(drawerMenu);
		for (int i = 0; i < menu.size(); i++) {
			MenuItem item = menu.getItem(i);
			item.setVisible(!drawerOpen);
		}
		return super.onPrepareOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
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
		setTitle(fragmentTitle);
	}


	private void navigateTo(int position) {
		currentNavItem = position;

		Fragment fragment = null;
		if (position == 0) fragment = new NewsFragment();
		else if (position == 1) fragment = new AlarmsFragment();
		else if (position == 2) fragment = new ChooseRiverFragment();
		else if (position == 3) fragment = new SettingsFragment();
		else if (position == 4) fragment = new AboutFragment();
		getFragmentManager().beginTransaction().replace(R.id.frame, fragment).commit();
		drawerMenuList.setItemChecked(position, true);
		fragmentTitle = navItems[position];
		drawerLayout.closeDrawer(drawerMenu);
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

}
