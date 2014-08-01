package de.bitdroid.flooding.news;

import android.app.Activity;
import android.content.Intent;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.main.MainActivity;
import it.gmariotti.cardslib.library.internal.Card;

final class NewsCard extends Card {

	private static final SimpleDateFormat dateFormatter
			= new SimpleDateFormat("dd/M/yyyy hh:mm a");

	private final Pair<NewsItem, Boolean> data;

	public NewsCard(
			final Activity activity,
			final NewsManager manager,
			final Pair<NewsItem, Boolean> data) {

		super(activity, R.layout.news_card);
		this.data = data;

		setSwipeable(true);
		setOnSwipeListener(new OnSwipeListener() {
			@Override
			public void onSwipe(Card card) {
				manager.removeItem(data.first);
			}
		});
		setOnUndoSwipeListListener(new OnUndoSwipeListListener() {
			@Override
			public void onUndoSwipe(Card card) {
				manager.addItem(data.first, false);
			}
		});


		setOnClickListener(new OnCardClickListener() {
			@Override
			public void onClick(Card card, View view) {
				Intent intent = new Intent(MainActivity.ACTION_NAVIGATE);
				intent.putExtra(MainActivity.EXTRA_POSITION, data.first.getNavigationPos());
				activity.sendBroadcast(intent);
			}
		});
	}


	@Override
	public void setupInnerViewElements(ViewGroup parent, View view) {
		TextView title = (TextView) parent.findViewById(R.id.news_title);
		TextView msg  = (TextView) parent.findViewById(R.id.news_timestamp);
		TextView content = (TextView) parent.findViewById(R.id.news_content);

		title.setText(data.first.getTitle());
		msg.setText(dateFormatter.format(data.first.getTimestamp()));
		content.setText(data.first.getContent());
	}

	public NewsItem getNewsItem() {
		return data.first;
	}

	public boolean isRead() {
		return data.second;
	}

}
