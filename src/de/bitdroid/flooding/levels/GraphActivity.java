package de.bitdroid.flooding.levels;

import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_TIMESTAMP;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_VALUE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_KM;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_NAME;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_WATER_NAME;

import android.app.Activity;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.flooding.utils.AbstractLoaderCallbacks;
import de.bitdroid.flooding.utils.Log;

public class GraphActivity extends Activity {
	
	public static final String EXTRA_WATER_NAME = "waterName";
	private static final int LOADER_ID = 44;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.graph);


		final GraphView graph = new LineGraphView(this, "Water Levels");
		LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
		layout.addView(graph);

		final String waterName = getIntent().getExtras().getString(EXTRA_WATER_NAME);

		AbstractLoaderCallbacks loader = new AbstractLoaderCallbacks(LOADER_ID) {

			@Override
			protected void onLoadFinishedHelper(Loader<Cursor> loader, Cursor cursor) {
				if (cursor == null) return;
				cursor.moveToFirst();

				int kmIdx = cursor.getColumnIndex(COLUMN_STATION_KM);
				int valueIdx = cursor.getColumnIndex(COLUMN_LEVEL_VALUE);

				GraphViewData[] data = new GraphViewData[cursor.getCount()];
				Log.debug("Length = " + data.length);
				int i = 0;
				do {
					data[i] = new GraphViewData(
							cursor.getDouble(kmIdx), 
							cursor.getDouble(valueIdx));
					Log.debug(data[i].toString());
					i++;
				} while(cursor.moveToNext());


				graph.removeAllSeries();
				graph.addSeries(new GraphViewSeries(data));
			}

			@Override
			protected void onLoaderResetHelper(Loader<Cursor> loader) { }

			@Override
			protected Loader<Cursor> getCursorLoader() {
				return new CursorLoader(
						getApplicationContext(),
						new PegelOnlineSource().toUri(),
						new String[] { 
							COLUMN_STATION_NAME,
							COLUMN_STATION_KM,
							COLUMN_LEVEL_TIMESTAMP,
							COLUMN_LEVEL_VALUE,
							COLUMN_LEVEL_UNIT
						}, COLUMN_WATER_NAME + "=?", 
						new String[] { waterName }, 
						null);
			}
		};

		getLoaderManager().initLoader(LOADER_ID, null, loader);
    }
}
