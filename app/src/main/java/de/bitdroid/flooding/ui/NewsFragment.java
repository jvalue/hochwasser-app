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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.brnunes.swipeablerecyclerview.SwipeableRecyclerViewTouchListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.news.DefaultTransformer;
import de.bitdroid.flooding.news.NewsItem;
import de.bitdroid.flooding.news.NewsManager;
import roboguice.inject.InjectView;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import timber.log.Timber;

public class NewsFragment extends AbstractFragment {

	private static final String PREFS_NAME = NewsFragment.class.getSimpleName();
	private static final String KEY_FIRST_START = "KEY_FIRST_START";

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

		// load items
		loadNews();

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
		newsManager.addItem(getString(R.string.news_intro_data_title), getString(R.string.news_intro_data_content), 2, true, false, false);
		newsManager.addItem(getString(R.string.news_intro_alarms_title), getString(R.string.news_intro_alarms_content), 1, true, false, false);
	}


	private void loadNews() {
		Observable<Void> newsObservable = null;

		// add helper news
		if (isFirstStart()) {
			showSpinner();
			newsObservable = Observable.defer(new Func0<Observable<Void>>() {
				@Override
				public Observable<Void> call() {
					addHelperNews();
					return Observable.just(null);
				}
			});
		} else {
			newsObservable = Observable.just(null);
		}

		// actually load news
		compositeSubscription.add(newsObservable
				.flatMap(new Func1<Void, Observable<List<NewsItem>>>() {
					@Override
					public Observable<List<NewsItem>> call(Void aVoid) {
						return newsManager.getAllNews();
					}
				})
				.compose(new DefaultTransformer<List<NewsItem>>())
				.subscribe(new Action1<List<NewsItem>>() {
					@Override
					public void call(List<NewsItem> items) {
						if (isSpinnerVisible()) hideSpinner();
						Collections.sort(items);
						adapter.setItems(items);
						adapter.notifyDataSetChanged();
					}
				}, new Action1<Throwable>() {
					@Override
					public void call(Throwable throwable) {
						if (isSpinnerVisible()) hideSpinner();
						// should (TM) never happen
						Timber.e(throwable, "failed to fetch news entries");
						Toast.makeText(NewsFragment.this.getActivity(), "Error getting news", Toast.LENGTH_SHORT).show();
					}
				}));
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

		private final View containerView;
		private final TextView titleView, timestampView, contentView;
		private final ImageView iconView;

		public NewsItemViewHolder(View itemView) {
			super(itemView);
			this.containerView = itemView.findViewById(R.id.container);
			this.titleView = (TextView) itemView.findViewById(R.id.news_title);
			this.timestampView = (TextView) itemView.findViewById(R.id.news_timestamp);
			this.contentView = (TextView) itemView.findViewById(R.id.news_content);
			this.iconView = (ImageView) itemView.findViewById(R.id.icon);
		}

		public void setItem(final NewsItem item) {
			// setup navigation to other sections on click
			containerView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((MainDrawerActivity) getActivity()).setSection(item.getNavigationPos());
				}
			});

			// setup content
			titleView.setText(item.getTitle());
			Date date = new Date(item.getTimestamp());
			timestampView.setText(DateFormat.getDateFormat(getActivity()).format(date) + " " + DateFormat.getTimeFormat(getActivity()).format(date));
			contentView.setText(Html.fromHtml(item.getContent()));
			if (item.getIsWarning()) iconView.setImageResource(R.drawable.ic_warning);
			else iconView.setImageResource(R.drawable.ic_announcement);
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
				adapter.getNewsList().remove(position);
			}
			adapter.notifyDataSetChanged();
			loadNews();
		}

	}
}

