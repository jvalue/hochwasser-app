package de.bitdroid.flooding.network;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthException;

import org.roboguice.shaded.goole.common.base.Optional;

import java.net.UnknownHostException;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.auth.RestrictedResource;
import retrofit.RetrofitError;
import timber.log.Timber;

/**
 * Handles network, auth and exceptions in general.
 */
public class DefaultErrorAction extends AbstractErrorAction {

	private final Context context;
	private final Optional<RestrictedResource> restrictedResource;
	private final String logMessage;

	public DefaultErrorAction(Context context, String logMessage) {
		this(context, null, logMessage);
	}

	public DefaultErrorAction(Context context, RestrictedResource restrictedResource, String logMessage) {
		this.context = context;
		this.restrictedResource = Optional.fromNullable(restrictedResource);
		this.logMessage = logMessage;
	}

	@Override
	protected void doCall(Throwable throwable) {
		if (throwable instanceof UnknownHostException) {
			onNetworkError(throwable);

		} else if (throwable instanceof RetrofitError) {
			RetrofitError retrofitError = (RetrofitError) throwable;
			switch (retrofitError.getKind()) {
				case NETWORK:
					onNetworkError(throwable);
					break;

				case HTTP:
					int status = retrofitError.getResponse().getStatus();
					if (status >= 400 && status < 500) {
						onAuthError(throwable);
						break;
					}

				case CONVERSION:
				case UNEXPECTED:
					onInternalError(throwable);
			}

		} else if (throwable instanceof GoogleAuthException) {
			onAuthError(throwable);

		} else {
			onInternalError(throwable);
		}
	}


	private void onNetworkError(Throwable throwable) {
		Timber.d(throwable, logMessage);
		new AlertDialog.Builder(context)
				.setTitle(R.string.error_network_title)
				.setMessage(R.string.error_network_message)
				.setPositiveButton(android.R.string.ok, null)
				.show();
	}


	private void onAuthError(Throwable throwable) {
		if (!restrictedResource.isPresent()) {
			Timber.w(throwable, "found auth error with no restricted resource");
			return;
		}

		Timber.w(throwable, logMessage);
		new AlertDialog.Builder(context)
				.setTitle(R.string.error_auth_title)
				.setMessage(R.string.error_auth_message)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						restrictedResource.get().logout();
					}
				})
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						restrictedResource.get().logout();
					}
				})
				.show();
	}


	private void onInternalError(Throwable throwable) {
		Timber.e(throwable, logMessage);
		AlertDialog.Builder builder = new AlertDialog.Builder(context)
				.setTitle(R.string.error_internal_title)
				.setPositiveButton(android.R.string.ok, null);

		LayoutInflater inflater = LayoutInflater.from(context);
		View messageView = inflater.inflate(R.layout.dialog_internal_error, null, false);

		View expandDetailsView = messageView.findViewById(R.id.details_expand);
		final TextView detailsTextView = (TextView) messageView.findViewById(R.id.details);
		detailsTextView.setText(Log.getStackTraceString(throwable));

		expandDetailsView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// toggle details
				if (detailsTextView.getVisibility() == View.GONE) {
					detailsTextView.setVisibility(View.VISIBLE);
				} else {
					detailsTextView.setVisibility(View.GONE);
				}

			}
		});

		builder
				.setView(messageView)
				.show();
	}

}
