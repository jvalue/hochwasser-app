package de.bitdroid.flooding;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import de.bitdroid.flooding.ods.OdsContract;
import de.bitdroid.flooding.ods.json.PegelonlineParser;
import de.bitdroid.flooding.utils.Log;


final class StationsListAdapter extends BaseAdapter 
		implements LoaderManager.LoaderCallbacks<Cursor>, OdsContract {

	private final List<String> items = new LinkedList<String>();
	private final Context context;

	public StationsListAdapter(Context context) {
		if (context == null) throw new NullPointerException("context cannot be null");
		this.context = context;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater 
			= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View itemView = inflater.inflate(R.layout.item, parent, false);
		TextView textView = (TextView) itemView.findViewById(R.id.item_text);
		textView.setText(items.get(position));

		return itemView;

	}

	@Override
	public Object getItem(int position) { 
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public int getCount() {
		return items.size();
	}


	static final int ODS_LOADER_ID = 42;

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		Log.debug("onCreateLoader called");
		if (id != ODS_LOADER_ID) return null;
		return new CursorLoader(
				context,
				BASE_CONTENT_URI,
				new String[] {
					COLUMN_SERVER_ID,
					COLUMN_SYNC_STATUS,
					COLUMN_JSON_DATA
				}, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.debug("onLoadFinished called");
		items.clear();
		cursor.moveToFirst();
		while (cursor.moveToNext()) {
			int idx = cursor.getColumnIndex(COLUMN_JSON_DATA);
			try {
				JSONObject json = new JSONObject(cursor.getString(idx));
				items.add(PegelonlineParser.getStationName(json));
			} catch(JSONException je) {
				Log.error(android.util.Log.getStackTraceString(je));
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

}
