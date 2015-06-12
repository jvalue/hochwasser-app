package de.bitdroid.flooding.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.utils.VersionUtils;
import roboguice.inject.InjectView;


public final class SettingsFragment extends AbstractFragment {

	@InjectView(R.id.versionTextView) private TextView version;
	@InjectView(R.id.authorTextView) private TextView authors;
	@InjectView(R.id.creditsTextView) private TextView credits;
	@InjectView(R.id.logoutTextView) private TextView logout;

	@Inject UiUtils uiUtils;
	@Inject private VersionUtils versionUtils;

	public SettingsFragment() {
		super(R.layout.fragment_settings);
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		version.setText(versionUtils.getVersion());
		setOnClickDialogForTextView(authors, R.string.settings_about, R.string.settings_about_message);
		setOnClickDialogForTextView(credits, R.string.settings_credits, R.string.settings_credits_message);

		logout.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				uiUtils.logout(getActivity());
			}
		});
	}

	private AlertDialog.Builder setOnClickDialogForTextView(TextView textView, final int titleResourceId, final int msgResourceId) {
		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity())
				.setTitle(titleResourceId)
				.setMessage(Html.fromHtml(getString(msgResourceId)))
				.setPositiveButton(android.R.string.ok, null);

		textView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog dialog = dialogBuilder.show();
				((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
			}
		});

		return dialogBuilder;
	}

}
