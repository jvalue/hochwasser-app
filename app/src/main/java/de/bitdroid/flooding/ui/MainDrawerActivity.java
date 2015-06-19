package de.bitdroid.flooding.ui;


import android.accounts.Account;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.roboguice.shaded.goole.common.base.Optional;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.auth.LoginManager;
import de.bitdroid.flooding.utils.VersionUtils;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;

/**
 * Container activity for the main navigation drawer.
 */
public class MainDrawerActivity extends AbstractRoboDrawerActivity {

	@Inject private VersionUtils versionUtils;
	@Inject private LoginManager loginManager;

	@Override
	public void init(Bundle bundle) {

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
	}


	protected void setSection(int idx) {
		MaterialSection<?> section = getSectionList().get(idx);
		setSection(section);
		changeToolbarColor(section);
		if (section.getTargetFragment()  != null) setFragment((Fragment) section.getTargetFragment(), section.getTitle());
	}

}
