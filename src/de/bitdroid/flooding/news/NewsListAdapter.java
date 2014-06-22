package de.bitdroid.flooding.news;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import de.bitdroid.flooding.R;


final class NewsListAdapter extends ArrayAdapter<NewsItem> {
	
	private final static SimpleDateFormat dateFormatter
		= new SimpleDateFormat("dd/M/yyyy hh:mm a");

	private final List<NewsItem> items = new LinkedList<NewsItem>();
	private final Context context;

	public NewsListAdapter(Context context) {
		super(context, R.layout.news_item);

		this.items.addAll(NewsManager.getInstance().getItems());
		this.context = context;

		addAll(items);
	}


	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater 
			= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view = inflater.inflate(R.layout.news_item, parent, false);
		NewsItem item = items.get(position);

		TextView title = (TextView) view.findViewById(R.id.news_title);
		TextView date = (TextView) view.findViewById(R.id.news_timestamp);
		TextView content = (TextView) view.findViewById(R.id.news_content);

		title.setText(item.getTitle());
		date.setText(dateFormatter.format(item.getTimestamp()));
		content.setText(item.getContent());

		return view;
	}

}
