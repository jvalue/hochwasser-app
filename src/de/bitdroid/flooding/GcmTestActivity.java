package de.bitdroid.flooding;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class GcmTestActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gcm);


		// register 
		Button registerButton = (Button) findViewById(R.id.register_button);
		registerButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(GcmTestActivity.this.getApplicationContext(), "Stub", Toast.LENGTH_SHORT).show();
			}
		});


		// register 
		Button unregisterButton = (Button) findViewById(R.id.unregister_button);
		unregisterButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(GcmTestActivity.this.getApplicationContext(), "Stub", Toast.LENGTH_SHORT).show();
			}
		});
	}
}
