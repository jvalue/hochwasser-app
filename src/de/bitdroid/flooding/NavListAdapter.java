package de.bitdroid.flooding;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


final class NavListAdapter extends ArrayAdapter<NavItem> {

	private final Context context;

	public NavListAdapter(Context context, NavItem[] items) {
		super(context, R.layout.nav_item, items);
		this.context = context;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater 
			= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view = inflater.inflate(R.layout.nav_item, parent, false);

		NavItem item = getItem(position);

		ImageView icon = (ImageView) view.findViewById(R.id.icon);
		TextView title = (TextView) view.findViewById(R.id.title);

		icon.setImageResource(item.getIconId());
		title.setText(item.getTitle());

		return view;
	}

}
