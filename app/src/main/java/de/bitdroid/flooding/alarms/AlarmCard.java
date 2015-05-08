package de.bitdroid.flooding.alarms;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.levels.StationActivity;
import de.bitdroid.flooding.ceps.RuleManager;
import de.bitdroid.flooding.gcm.GcmStatus;
import de.bitdroid.flooding.utils.Assert;
import de.bitdroid.flooding.utils.StringUtils;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.internal.dismissanimation.SwipeDismissAnimation;
import timber.log.Timber;

final class AlarmCard extends Card {

	private final LevelAlarm alarm;
	private final RuleManager manager;

	public AlarmCard(
			final Activity activity,
			final RuleManager manager,
			final AlarmDeregistrationQueue deregistrationQueue,
			final LevelAlarm alarm,
			final SwipeDismissAnimation dismissAnimation) {

		super(activity, R.layout.alarms_card);
		Assert.assertNotNull(manager, alarm);
		this.manager = manager;
		this.alarm = alarm;

		setSwipeable(true);
		setOnSwipeListener(new OnSwipeListener() {
			@Override
			public void onSwipe(Card card) {
				deregistrationQueue.unregister(alarm);
			}
		});

		setOnUndoSwipeListListener(new OnUndoSwipeListListener() {
			@Override
			public void onUndoSwipe(Card card) {
				deregistrationQueue.cancelDeregistration(alarm);
			}
		});

		// retry registration on tap
		setOnClickListener(new OnCardClickListener() {
			@Override
			public void onClick(Card card, View view) {
				// tap to retry
				if (manager.getRegistrationStatus(alarm.getRule()).equals(GcmStatus.ERROR_REGISTRATION)) {
					manager.registerRule(alarm.getRule());

				// goto station graph
				} else if (manager.getRegistrationStatus(alarm.getRule()).equals(GcmStatus.REGISTERED)) {
					Intent intent = new Intent(activity, StationActivity.class);
					intent.putExtra(StationActivity.EXTRA_WATER_NAME, alarm.getRiver());
					intent.putExtra(StationActivity.EXTRA_STATION_NAME, alarm.getStation());
					activity.startActivity(intent);
					activity.overridePendingTransition(R.anim.slide_enter_from_right, R.anim.slide_exit_to_left);
				}
			}
		});

		// custom header with overflow button
		// header contains all data since this makes creating custom layouts easier
		CardHeader header = new CardHeader(getContext(), R.layout.alarms_card_header) {
			@Override
			public void setupInnerViewElements(ViewGroup parent, View view) {
				TextView text1 = (TextView) parent.findViewById(android.R.id.text1);
				text1.setText(AlarmCard.this.getTitle());

				TextView text2 = (TextView) parent.findViewById(android.R.id.text2);
				String description;
				if (alarm.getAlarmWhenAbove()) {
					description = getContext().getString(R.string.alarms_description_above, alarm.getLevel());
				} else {
					description = getContext().getString(R.string.alarms_description_below, alarm.getLevel());
				}
				text2.setText(description);

				LinearLayout regView = (LinearLayout) parent.findViewById(R.id.registration);
				TextView regStatusView = (TextView) parent.findViewById(R.id.registration_status);

				GcmStatus regStatus = manager.getRegistrationStatus(alarm.getRule());
				if (regStatus.equals(GcmStatus.REGISTERED)) {
					regView.setVisibility(View.GONE);
					return;
				}

				regView.setVisibility(View.VISIBLE);
				switch (regStatus) {
					case PENDING_REGISTRATION:
						parent.findViewById(R.id.registration_pending).setVisibility(View.VISIBLE);
						regStatusView.setText(getContext().getString(R.string.alarms_registration_pending));
						break;
					case ERROR_REGISTRATION:
						parent.findViewById(R.id.registration_error).setVisibility(View.VISIBLE);
						regStatusView.setText(getContext().getString(R.string.alarms_registration_error));
						break;
					default:
						Timber.w("Found alarm with status " + regStatus.toString() + " which doesn't have any view actions");
				}
			}
		};

		header.setPopupMenu(R.menu.alarms_card_menu, new CardHeader.OnClickCardHeaderPopupMenuListener() {
			@Override
			public void onMenuItemClick(BaseCard card, MenuItem item) {
				switch (item.getItemId()) {
					case R.id.delete:
						// this will call onSwipe
						dismissAnimation.animateDismiss(AlarmCard.this);
				}
			}
		});

		addCardHeader(header);
	}


	@Override
	public void setupInnerViewElements(ViewGroup parent, View view) {
		// nothing to do here, layout is in header
	}


	public String getTitle() {
		return StringUtils.toProperCase(alarm.getRiver()) + " - " + StringUtils.toProperCase(alarm.getStation());
	}

}
