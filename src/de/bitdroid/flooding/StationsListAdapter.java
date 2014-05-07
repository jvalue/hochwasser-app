package de.bitdroid.flooding;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import de.bitdroid.flooding.rest.ODSTable;
import de.bitdroid.flooding.rest.RestContentProvider;
import de.bitdroid.flooding.utils.Log;


final class StationsListAdapter extends BaseAdapter {

	private final List<String> items = new LinkedList<String>();
	private final Context context;

	public StationsListAdapter(Context context) {
		if (context == null) throw new NullPointerException("context cannot be null");
		this.context = context;
		loadData();
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


	@Override
	public void notifyDataSetChanged() {
		loadData();
		super.notifyDataSetChanged();
	}


	private void loadData() {
		Log.debug("reloading data"); items.clear();
		Cursor cursor = context.getContentResolver().query(
				RestContentProvider.CONTENT_URI,
				ODSTable.COLUMN_NAMES,
				null, null, null);

		cursor.moveToFirst();
		int idx = cursor.getColumnIndex(ODSTable.COLUMN_SERVER_ID);
		for (int i = 0; i < cursor.getCount(); i++) {
			items.add(cursor.getString(idx));
			cursor.moveToNext();
		}
	}
}
