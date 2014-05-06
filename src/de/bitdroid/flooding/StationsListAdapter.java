package de.bitdroid.flooding;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;


final class StationsListAdapter extends BaseAdapter {

	private final List<String> items = new LinkedList<String>();
	private final Context context;

	public StationsListAdapter(Context context) {
		if (context == null) throw new NullPointerException("context cannot be null");
		this.context = context;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
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
		Log.i("Flooding", "items.size() " + items.size());
		items.clear();
		Cursor cursor = context.getContentResolver().query(new Uri.Builder().scheme("content").authority("de.bitdroid.flooding.provider").path("foobar").build(), null, null, null, null);
		for (int i = 0; i < cursor.getCount(); i++) {
			items.add(cursor.getString(i));
			cursor.moveToNext();
		}
	
		Log.i("Flooding", "items.size() " + items.size());

		super.notifyDataSetChanged();
	}



	private void loadData() {
		RequestQueue queue = VolleyManager.getInstance(context).getRequestQueue();
		queue.add(new StringRequest(Method.GET,
					"http://faui2o2f.cs.fau.de:8080/open-data-service/ods/de/pegelonline/stationsFlat",
					new Response.Listener<String>() {
						@Override
						public void onResponse(String response) {
							items.clear();
							try {
								JSONArray array = new JSONArray(response);
								for (int i = 0; i < array.length(); i++) {
									items.add(array.get(i).toString());
								}
							} catch(JSONException je) {
								items.add(je.getMessage());
							}
							Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
							notifyDataSetChanged();
						}
					},
					new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							items.clear();
							items.add(error.getMessage());
							Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show();
							notifyDataSetChanged();
						}
					}));
	}

}
