package de.bitdroid.flooding.levels;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.pegelonline.PegelOnlineUtils;
import it.gmariotti.cardslib.library.internal.Card;

final class StationLevelCard extends Card {

	private final String timestamp;
	private final double value;
	private final String unit;

	public StationLevelCard(Context context, String timestamp, double value, String unit) {
		super(context, R.layout.station_card_level);
		this.timestamp = timestamp;
		this.value = value;
		this.unit = unit;
	}


	@Override
	public void setupInnerViewElements(ViewGroup parent, View view) {
		TextView timeView = (TextView) view.findViewById(R.id.timestamp);
		TextView levelView = (TextView) view.findViewById(R.id.level);

		Date date = new Date(PegelOnlineUtils.parseStringTimestamp(timestamp));
		timeView.setText(
				android.text.format.DateFormat.getDateFormat(getContext()).format(date)
				+ " " + android.text.format.DateFormat.getTimeFormat(getContext()).format(date));

		levelView.setText(value + " " + unit);
	}
}
