package de.bitdroid.flooding.news;

import java.util.LinkedList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import de.bitdroid.flooding.R;


public final class NewsFragment extends Fragment implements AbsListView.MultiChoiceModeListener {

	private ArrayAdapter<NewsItem> listAdapter;

	@Override
	public View onCreateView(
			LayoutInflater inflater, 
			ViewGroup container, 
			Bundle savedInstanceState) {

		// view
		View view = inflater.inflate(R.layout.news, container, false);
		ListView listView = (ListView) view.findViewById(R.id.list);
		listAdapter = new NewsListAdapter(getActivity().getApplicationContext());
		listView.setAdapter(listAdapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(this);

		return view;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		NewsManager.getInstance(getActivity().getApplicationContext()).markAllItemsRead();
	}


	private final List<NewsItem> selectedItems = new LinkedList<NewsItem>();

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.delete:
				Context context = getActivity().getApplicationContext();
				NewsManager manager = NewsManager.getInstance(context);
				for (NewsItem newsItem : selectedItems) {
					manager.removeItem(newsItem);
				}
				listAdapter.notifyDataSetChanged();
				Toast.makeText(
						context, 
						context.getString(R.string.news_deleted, selectedItems.size()), 
						Toast.LENGTH_SHORT)
					.show();
				selectedItems.clear();

				mode.finish();
				return true;
		}
		return false;
	}


	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.news_list_menu, menu);
		return true;
	}


	@Override
	public void onDestroyActionMode(ActionMode mode) { }


	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}


	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
		NewsItem item = listAdapter.getItem(position);
		if (checked) selectedItems.add(item);
		else selectedItems.remove(item);
		mode.setTitle(getActivity().getString(R.string.news_selected, selectedItems.size()));
	}

}
