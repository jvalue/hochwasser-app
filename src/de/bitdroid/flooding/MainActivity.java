package de.bitdroid.flooding;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		Button updateBtn = (Button) findViewById(R.id.content_update);
		updateBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "Update stub!", Toast.LENGTH_SHORT).show();
			}
		});

		Button clearBtn = (Button) findViewById(R.id.content_clear);
		clearBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "Clear stub!", Toast.LENGTH_SHORT).show();
			}
		});
    }

}
