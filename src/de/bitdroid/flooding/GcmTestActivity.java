package de.bitdroid.flooding;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import de.bitdroid.flooding.ods.GcmException;
import de.bitdroid.flooding.ods.OdsSource;
import de.bitdroid.flooding.ods.OdsSourceManager;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;

public class GcmTestActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gcm);


		final PegelOnlineSource source = new PegelOnlineSource();

		// register 
		Button registerButton = (Button) findViewById(R.id.register_button);
		registerButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new RegistrationTask(
					GcmTestActivity.this.getApplicationContext(),
					source, true).execute();
			}
		});


		// register 
		Button unregisterButton = (Button) findViewById(R.id.unregister_button);
		unregisterButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new RegistrationTask(
					GcmTestActivity.this.getApplicationContext(),
					source, false).execute();
			}
		});
	}


	private static final class RegistrationTask extends AsyncTask<Void, Void, GcmException> {
		private final Context context;
		private final OdsSource source;
		private final boolean register; 

		public RegistrationTask(Context context, OdsSource source, boolean register) {
			this.context = context;
			this.source = source;
			this.register = register;
		}

		@Override
		protected GcmException doInBackground(Void... param) {
			try {
				if (register) OdsSourceManager.getInstance(context).startPushNotifications(source);
				else OdsSourceManager.getInstance(context).stopPushNotifications(source);
			} catch (GcmException ge) {
				return ge;
			}
			return null;
		}

		@Override
		protected void onPostExecute(GcmException ge) {
			if (ge != null) Toast.makeText(context, ge.getMessage(), Toast.LENGTH_LONG).show();
			else Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
		}

	}
}
