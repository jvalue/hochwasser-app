package de.bitdroid.flooding.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.brnunes.swipeablerecyclerview.SwipeableRecyclerViewTouchListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.network.NetworkUtils;
import de.bitdroid.flooding.news.NewsItem;
import de.bitdroid.flooding.news.NewsManager;
import roboguice.inject.InjectView;

public class NewsFragment extends AbstractFragment {

	private static final String PREFS_NAME = NewsFragment.class.getSimpleName();
	private static final String KEY_FIRST_START = "KEY_FIRST_START";

	@Inject private NetworkUtils networkUtils;
	@Inject private NewsManager newsManager;

	@InjectView(R.id.list) RecyclerView recyclerView;
	private NewsAdapter adapter;


	public NewsFragment() {
		super(R.layout.fragment_news);
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// setup list
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
		recyclerView.setLayoutManager(layoutManager);
		adapter = new NewsAdapter();
		recyclerView.setAdapter(adapter);

		// add helper news
		if (isFirstStart()) addHelperNews();

		// load items
		List<NewsItem> items = newsManager.getAllNews();
		Collections.sort(items);
		adapter.setItems(items);

		// setup swipe to delete
		SwipeableRecyclerViewTouchListener swipeTouchListener = new SwipeableRecyclerViewTouchListener(recyclerView, new NewsItemSwipeListener());
		recyclerView.addOnItemTouchListener(swipeTouchListener);
	}


	private boolean isFirstStart() {
		SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		boolean firstStart = prefs.getBoolean(KEY_FIRST_START, true);
		if (!firstStart) return false;

		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(KEY_FIRST_START, false);
		editor.commit();
		return true;
	}


	private void addHelperNews() {
		newsManager.addItem(getString(R.string.news_intro_data_title), getString(R.string.news_intro_data_content), 2, true, false);
		newsManager.addItem(getString(R.string.news_intro_alarms_title), getString(R.string.news_intro_alarms_content), 1, true, false);
	}



	protected class NewsAdapter extends RecyclerView.Adapter<NewsItemViewHolder> {

		private final List<NewsItem> newsList = new ArrayList<>();

		@Override
		public void onBindViewHolder(NewsItemViewHolder holder, int position) {
			holder.setItem(newsList.get(position));
		}

		@Override
		public int getItemCount() {
			return newsList.size();
		}

		public void setItems(List<NewsItem> items) {
			this.newsList.clear();
			this.newsList.addAll(items);
			notifyDataSetChanged();
		}

		public List<NewsItem> getNewsList() {
			return newsList;
		}

		@Override
		public NewsItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_news, parent, false);
			return new NewsItemViewHolder(view);
		}

	}


	protected class NewsItemViewHolder extends RecyclerView.ViewHolder {

		private final TextView titleView, timestampView, contentView;

		public NewsItemViewHolder(View itemView) {
			super(itemView);
			this.titleView = (TextView) itemView.findViewById(R.id.news_title);
			this.timestampView = (TextView) itemView.findViewById(R.id.news_timestamp);
			this.contentView = (TextView) itemView.findViewById(R.id.news_content);
		}

		public void setItem(NewsItem item) {
			titleView.setText(item.getTitle());
			Date date = new Date(item.getTimestamp());
			timestampView.setText(DateFormat.getDateFormat(getActivity()).format(date) + " " + DateFormat.getTimeFormat(getActivity()).format(date));
			contentView.setText(Html.fromHtml(item.getContent()));
		}

	}


	protected class NewsItemSwipeListener implements SwipeableRecyclerViewTouchListener.SwipeListener {

		@Override
		public boolean canSwipe(int position) {
			return true;
		}

		@Override
		public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
			removeItems(reverseSortedPositions);
		}

		@Override
		public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
			removeItems(reverseSortedPositions);
		}

		private void removeItems(int[] reverseSortedPositions) {
			for (int position : reverseSortedPositions) {
				adapter.getNewsList().get(position).delete();
			}
			adapter.setItems(newsManager.getAllNews());
			adapter.notifyDataSetChanged();
		}

	}
}

