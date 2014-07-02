package de.bitdroid.flooding;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import de.bitdroid.flooding.ods.AbstractGcmRegistrationReceiver;
import de.bitdroid.flooding.ods.OdsSource;
import de.bitdroid.flooding.ods.OdsSourceManager;
import de.bitdroid.flooding.utils.Log;


public final class GcmRegistrationReceiver extends AbstractGcmRegistrationReceiver {

	private static final int NOTIFICATION_ID = 4200;

	@Override
	public void onReceive(
			Context context, 
			OdsSource source, 
			boolean register, 
			String errorMsg) {

		if (errorMsg != null) {
			NotificationManager manager 
				= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

			Intent intent = new Intent(context, GcmRetryReceiver.class);
			intent.putExtra(GcmRetryReceiver.EXTRA_SOURCE, source.toString());
			intent.putExtra(GcmRetryReceiver.EXTRA_REGISTER, register);

			PendingIntent pendingIntent = PendingIntent.getBroadcast(
					context, 
					42, 
					intent,
					PendingIntent.FLAG_UPDATE_CURRENT);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_menu_home)
				.setContentTitle(context.getString(R.string.error_gcm_failed_title))
				.setStyle(new NotificationCompat.BigTextStyle().bigText(
							context.getString(R.string.error_gcm_failed_content)))
				.setAutoCancel(true)
				.setContentIntent(pendingIntent);

			manager.notify(NOTIFICATION_ID, builder.build());
		}
	}

	public final static class GcmRetryReceiver extends BroadcastReceiver {

		private static final String 
			EXTRA_SOURCE = "EXTRA_SOURCE",
			EXTRA_REGISTER = "EXTRA_REGISTER";

		@Override
		public void onReceive(Context context, Intent intent) {
			OdsSource source = OdsSource.fromString(intent.getStringExtra(EXTRA_SOURCE));
			boolean register = intent.getBooleanExtra(EXTRA_REGISTER, false);

			OdsSourceManager manager = OdsSourceManager.getInstance(context);
			if (register) manager.startPushNotifications(source);
			else manager.stopPushNotifications(source);
		}

	}
}
