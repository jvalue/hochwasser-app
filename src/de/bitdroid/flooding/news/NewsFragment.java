package de.bitdroid.flooding.news;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import de.bitdroid.flooding.R;


public final class NewsFragment extends Fragment {


	@Override
	public View onCreateView(
			LayoutInflater inflater, 
			ViewGroup container, 
			Bundle savedInstanceState) {

		// view
		View view = inflater.inflate(R.layout.news, container, false);
		ListView listView = (ListView) view.findViewById(R.id.list);
		ListAdapter listAdapter = new NewsListAdapter(getActivity().getApplicationContext());
		listView.setAdapter(listAdapter);

		return view;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		NewsManager.getInstance(getActivity().getApplicationContext()).markAllItemsRead();
	}

}
