package de.bitdroid.flooding.news;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.utils.Assert;


final class NewsListAdapter extends BaseAdapter {
	
	private final static SimpleDateFormat dateFormatter
		= new SimpleDateFormat("dd/M/yyyy hh:mm a");


	private final List<NewsItem> items = new ArrayList<NewsItem>();
	private final Context context;
	private final NewsManager manager;

	public NewsListAdapter(Context context) {
		this.context = context;
		this.manager = NewsManager.getInstance(context);
		this.items.addAll(manager.getAllItems());
		Collections.sort(items);
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater 
			= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view = inflater.inflate(R.layout.news_item, parent, false);
		NewsItem item = getItem(position);

		TextView title = (TextView) view.findViewById(R.id.news_title);
		TextView date = (TextView) view.findViewById(R.id.news_timestamp);
		TextView content = (TextView) view.findViewById(R.id.news_content);

		title.setText(item.getTitle());
		date.setText(dateFormatter.format(item.getTimestamp()));
		content.setText(item.getContent());

		return view;
	}


	@Override
	public NewsItem getItem(int pos) {
		Assert.assertValidIdx(items, pos);
		return items.get(pos);
	}


	@Override
	public long getItemId(int pos) {
		return pos;
	}


	@Override
	public int getCount() {
		return items.size();
	}


	public void addItem(NewsItem item) {
		Assert.assertNotNull(item);
		manager.addItem(item, false);
		items.add(item);
		Collections.sort(items);
		notifyDataSetChanged();
	}


	public void removeItem(NewsItem item) {
		Assert.assertNotNull(item);
		Assert.assertTrue(items.contains(item), "item not in list");
		items.remove(item);
		manager.removeItem(item);
		notifyDataSetChanged();
	}


	@Override
	public void notifyDataSetInvalidated() {
		items.clear();
		items.addAll(manager.getAllItems());
		Collections.sort(items);
		notifyDataSetChanged();
	}

}
