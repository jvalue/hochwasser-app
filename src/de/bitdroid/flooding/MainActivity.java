package de.bitdroid.flooding;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		ListView listView = (ListView) findViewById(R.id.content_list);
		listView.setAdapter(new StationsListAdapter(this));
    }

}
