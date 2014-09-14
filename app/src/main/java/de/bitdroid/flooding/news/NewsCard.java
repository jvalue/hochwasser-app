package de.bitdroid.flooding.news;

import android.app.Activity;
import android.content.Intent;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.main.MainActivity;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.internal.dismissanimation.SwipeDismissAnimation;

final class NewsCard extends Card {

	private final Pair<NewsItem, Boolean> data;

	public NewsCard(
			final Activity activity,
			final NewsManager manager,
			final Pair<NewsItem, Boolean> data,
			final SwipeDismissAnimation dismissAnimation) {

		super(activity, R.layout.news_card);
		this.data = data;

		setSwipeable(true);
		setOnSwipeListener(new OnSwipeListener() {
			@Override
			public void onSwipe(Card card) {
				// Delay removing from manager until anim has finished,
				// otherwise loader will remove card first
				// only relevant if this was called from a manual dismiss animation
				final Timer timer = new Timer();
				TimerTask task = new TimerTask() {
					@Override
					public void run() {
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								timer.cancel();
								manager.removeItem(data.first);
							}
						});
					}
				};
				timer.schedule(task, 500);
			}
		});
		setOnUndoSwipeListListener(new OnUndoSwipeListListener() {
			@Override
			public void onUndoSwipe(Card card) {
				manager.addItem(data.first, false);
			}
		});

		if (getNewsItem().isNavigationEnabled()) {
			setOnClickListener(new OnCardClickListener() {
				@Override
				public void onClick(Card card, View view) {
					Intent intent = new Intent(MainActivity.ACTION_NAVIGATE);
					intent.putExtra(MainActivity.EXTRA_POSITION, data.first.getNavigationPos());
					activity.sendBroadcast(intent);
				}
			});
		}

		// custom header with overflow button
		// header contains all data since this makes creating custom layouts easier
		CardHeader header = new CardHeader(getContext(), R.layout.news_card_header) {
			@Override
			public void setupInnerViewElements(ViewGroup parent, View view) {
				TextView title = (TextView) parent.findViewById(R.id.news_title);
				title.setText(data.first.getTitle());

				TextView msg  = (TextView) parent.findViewById(R.id.news_timestamp);
				TextView content = (TextView) parent.findViewById(R.id.news_content);

				Date date = new Date(data.first.getTimestamp());

				msg.setText(DateFormat.getDateFormat(getContext()).format(date)
						+ " " + DateFormat.getTimeFormat(getContext()).format(date));
				content.setText(Html.fromHtml(data.first.getContent()));
			}
		};

		header.setPopupMenu(R.menu.news_card_menu, new CardHeader.OnClickCardHeaderPopupMenuListener() {
			@Override
			public void onMenuItemClick(BaseCard card, MenuItem item) {
				switch (item.getItemId()) {
					case R.id.share:
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_SEND);
						intent.putExtra(Intent.EXTRA_TEXT, data.first.getContent());
						intent.setType("text/plain");
						activity.startActivity(Intent.createChooser(intent, getContext().getResources().getText(R.string.share_with)));
						return;

					case R.id.delete:
						// this will call onSwipe
						dismissAnimation.animateDismiss(NewsCard.this);
						return;
				}
			}
		});

		addCardHeader(header);
	}


	@Override
	public void setupInnerViewElements(ViewGroup parent, View view) {
		//  nothing to do here, layout is in header
	}

	public NewsItem getNewsItem() {
		return data.first;
	}

	public boolean isRead() {
		return data.second;
	}

}
