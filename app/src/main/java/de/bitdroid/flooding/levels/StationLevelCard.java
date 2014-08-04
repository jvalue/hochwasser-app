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
import de.bitdroid.flooding.utils.Log;
import it.gmariotti.cardslib.library.internal.Card;

final class StationLevelCard extends Card {

	private static final DateFormat
			dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ"),
			dateFormat = new SimpleDateFormat("dd/MM/yy"),
			timeFormat = new SimpleDateFormat("HH:mm a");


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
		TextView dateView = (TextView) view.findViewById(R.id.timestamp_date);
		TextView timeView = (TextView) view.findViewById(R.id.timestamp_time);
		TextView levelView = (TextView) view.findViewById(R.id.level);

		try {
			Date date = dateParser.parse(timestamp);
			dateView.setText(dateFormat.format(date));
			timeView.setText(timeFormat.format(date));
		} catch (ParseException pe) {
			Log.error("failed to parse timestamp", pe);
		}

		levelView.setText(value + " " + unit);
	}
}