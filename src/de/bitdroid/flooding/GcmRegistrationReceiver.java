package de.bitdroid.flooding;

import android.content.Context;
import android.widget.Toast;

import de.bitdroid.flooding.ods.AbstractGcmRegistrationReceiver;
import de.bitdroid.flooding.ods.OdsSource;


public final class GcmRegistrationReceiver extends AbstractGcmRegistrationReceiver {

	@Override
	public void onReceive(
			Context context, 
			OdsSource source, 
			boolean register, 
			String errorMsg) {

		if (errorMsg != null)
			Toast.makeText(
					context,
					context.getString(R.string.error_server_connection, errorMsg),
					Toast.LENGTH_LONG).show();
	}
}
