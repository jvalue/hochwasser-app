package de.bitdroid.flooding.ui;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.auth.LoginManager;
import de.bitdroid.flooding.utils.VersionUtils;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;

/**
 * Container activity for the main navigation drawer.
 */
public class MainDrawerActivity extends AbstractRoboDrawerActivity {

	@Inject private VersionUtils versionUtils;
	@Inject private LoginManager loginManager;

	@Override
	public void init(Bundle bundle) {

		// setup top image
		addAccount(new MaterialAccount(this.getResources(),loginManager.getAccountName(), "", null, R.drawable.drawer_background));
		setDrawerHeaderImage(R.drawable.drawer_background);

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

}
