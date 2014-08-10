package de.bitdroid.flooding.levels;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.bitdroid.flooding.R;
import de.bitdroid.utils.Log;
import it.gmariotti.cardslib.library.internal.Card;

final class StationLevelCard extends Card {

	private static final DateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ");


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

		try {
			Date date = dateParser.parse(timestamp);
			timeView.setText(
					android.text.format.DateFormat.getDateFormat(getContext()).format(date)
					+ " " + android.text.format.DateFormat.getTimeFormat(getContext()).format(date));
		} catch (ParseException pe) {
			Log.error("failed to parse timestamp", pe);
		}

		levelView.setText(value + " " + unit);
	}
}
