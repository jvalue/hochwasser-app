package de.bitdroid.flooding.ui;


import android.accounts.Account;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.auth.LoginManager;
import de.bitdroid.flooding.utils.VersionUtils;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Container activity for the main navigation drawer.
 */
@ContentView(R.layout.activity_main_drawer)
public class MainDrawerActivity extends AbstractActivity implements Drawer.OnDrawerItemClickListener {

	public static final int
			ID_NEWS = 0,
			ID_ALARMS = 1,
			ID_WATER_LEVELS = 2,
			ID_SETTINGS = 3,
			ID_FEEDBACK = 4;


	@Inject private VersionUtils versionUtils;
	@Inject private LoginManager loginManager;

	@InjectView(R.id.toolbar) Toolbar toolbar;
	private Drawer drawer;
	private int visibleFragmentId;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setSupportActionBar(toolbar);

		/*
		// setup drawer header
		View drawerHeader = LayoutInflater.from(this).inflate(R.layout.drawer_header, null);
		TextView emailView = (TextView) drawerHeader.findViewById(R.id.text_email);
		Optional<Account> account = loginManager.getAccount();
		if (account.isPresent()) emailView.setText(account.get().name);
		setDrawerHeaderCustom(drawerHeader);

		// setup main sections
		addSection(newSection(getString(R.string.nav_home), R.drawable.ic_home, new NewsFragment()));
		Intent intent = new Intent(this, DataSelectionHandler.WaterSelectionActivity.class);
		addSection(newSection(getString(R.string.nav_alarms), R.drawable.ic_alert, new AlarmsFragment()));
		addSection(newSection(getString(R.string.nav_analysis), R.drawable.ic_chart, intent));

		// setup settings and feedback section
		addBottomSection(newSection(getString(R.string.nav_settings), R.drawable.ic_settings, new SettingsFragment()));

		String address = getString(R.string.feedback_mail_address);
		String subject = getString(
				R.string.feedback_mail_subject,
				getString(R.string.app_name),
				versionUtils.getVersion());
		Intent mailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", address, null));
		mailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		Intent mailer = Intent.createChooser(mailIntent, getString(R.string.feedback_mail_chooser));
		addBottomSection(newSection(getString(R.string.nav_feedback), R.drawable.ic_email, mailer));
		*/




		// show first fragment
		showFragment(new NewsFragment(), false);
		toolbar.setTitle(R.string.nav_home);
		visibleFragmentId = ID_NEWS;

		/*
		// setup image loading for nav drawer
		DrawerImagekoader.init(new DrawerImageLoader.IDrawerImageLoader() {
			@Override
			public void set(ImageView imageView, Uri uri, Drawable drawable) {
				Picasso.with(imageView.getContext()).load(uri).into(imageView);
			}

			@Override
			public void cancel(ImageView imageView) {
				Picasso.with(imageView.getContext()).cancelRequest(imageView);
			}

			@Override
			public Drawable placeholder(Context context) {
				return null;
			}
		});

		Account account = authManager.getAccount();
		AccountHeader accountHeader = new AccountHeaderBuilder()
				.withActivity(this)
				.withHeaderBackground(R.drawable.background)
				.addProfiles(
						new ProfileDrawerItem().withName(account.getName()).withEmail(account.getEmail()).withIcon(account.getImageUrl())
				)
				.withProfileImagesClickable(false)
				.withSelectionListEnabledForSingleProfile(false)
				.build();
				*/

		// setup account in nav drawer
		Account account = loginManager.getAccount().get();
		AccountHeader accountHeader = new AccountHeaderBuilder()
				.withActivity(this)
				.withHeaderBackground(R.drawable.drawer_background)
				.addProfiles(new ProfileDrawerItem().withName(account.name).withIcon(getResources().getDrawable(R.drawable.ic_duck)))
				.withSelectionListEnabledForSingleProfile(false)
				.withProfileImagesClickable(false)
				.build();

		// setup actual nav drawer
		drawer = new DrawerBuilder()
				.withActivity(this)
				.withToolbar(toolbar)
				.withAccountHeader(accountHeader)
				.addDrawerItems(
						new PrimaryDrawerItem().withName(R.string.nav_home).withIconTintingEnabled(true).withIcon(R.drawable.ic_home).withIdentifier(ID_NEWS),
						new PrimaryDrawerItem().withName(R.string.nav_alarms).withIconTintingEnabled(true).withIcon(R.drawable.ic_alert).withIdentifier(ID_ALARMS),
						new PrimaryDrawerItem().withName(R.string.nav_analysis).withIconTintingEnabled(true).withIcon(R.drawable.ic_chart).withIdentifier(ID_WATER_LEVELS)
				)
				.addStickyDrawerItems(
						new PrimaryDrawerItem().withName(R.string.nav_settings).withIconTintingEnabled(true).withIcon(R.drawable.ic_settings).withIdentifier(ID_SETTINGS),
						new PrimaryDrawerItem().withName(R.string.nav_feedback).withIconTintingEnabled(true).withIcon(R.drawable.ic_email).withIdentifier(ID_FEEDBACK)
				)
				.withOnDrawerItemClickListener(this)
				.build();

		/*
		// setup account in nav drawer
		TextView emailView = (TextView) drawer.getHeader().findViewById(R.id.text_email);
		// View drawerHeader = LayoutInflater.from(this).inflate(R.layout.drawer_header, null);
		// TextView emailView = (TextView) drawerHeader.findViewById(R.id.text_email);
		Optional<Account> account = loginManager.getAccount();
		if (account.isPresent()) emailView.setText(account.get().name);
		*/
	}


	private void showFragment(Fragment fragment, boolean replace) {
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		if (!replace) transaction.add(R.id.container, fragment);
		else transaction.replace(R.id.container, fragment);
		transaction.addToBackStack("").commit();
	}


	@Override
	public boolean onItemClick(AdapterView<?> adapterView, View view, int position, long id, IDrawerItem item) {
		drawer.closeDrawer();
		if (item.getIdentifier() == visibleFragmentId) return false;

		switch (item.getIdentifier()) {
			case ID_FEEDBACK:
				String address = getString(R.string.feedback_mail_address);
				String subject = getString(
						R.string.feedback_mail_subject,
						getString(R.string.app_name),
						versionUtils.getVersion());
				Intent mailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", address, null));
				mailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
				startActivity(Intent.createChooser(mailIntent, getString(R.string.feedback_mail_chooser)));
				break;

			case ID_NEWS:
				showFragment(new NewsFragment(), true);
				toolbar.setTitle(R.string.nav_home);
				break;

			case ID_ALARMS:
				showFragment(new AlarmsFragment(), true);
				toolbar.setTitle(R.string.nav_alarms);
				break;

			case ID_WATER_LEVELS:
				startActivity(new Intent(this, DataSelectionHandler.WaterSelectionActivity.class));
				break;

			case ID_SETTINGS:
				showFragment(new SettingsFragment(), true);
				break;

			default:
				return false;
		}

		visibleFragmentId = item.getIdentifier();
		return true;
	}


	public void showDrawerItem(int drawerItemId) {
		drawer.setSelectionByIdentifier(drawerItemId);
	}

}
