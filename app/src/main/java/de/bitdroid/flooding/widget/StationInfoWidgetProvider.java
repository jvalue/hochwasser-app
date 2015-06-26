package de.bitdroid.flooding.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.widget.RemoteViews;

import java.util.Date;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.network.DefaultErrorAction;
import de.bitdroid.flooding.network.ErrorActionBuilder;
import de.bitdroid.flooding.network.NetworkUtils;
import de.bitdroid.flooding.ods.OdsManager;
import de.bitdroid.flooding.ods.Station;
import de.bitdroid.flooding.ods.StationMeasurements;
import roboguice.receiver.RoboAppWidgetProvider;
import rx.functions.Action1;

/**
 * {@link android.appwidget.AppWidgetProvider} for a station info
 * widget.
 */
public class StationInfoWidgetProvider extends RoboAppWidgetProvider {

	@Inject private OdsManager odsManager;
	@Inject private NetworkUtils networkUtils;

	@Override
	public void onHandleUpdate(final Context context, final AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		for (final int widgetId: appWidgetIds) {

			// load data and set view
			final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_station_info);
			final Station station = new Station.Builder().setStationName("Regensburg").setGaugeId("10061007").build();
			odsManager.getMeasurements(station)
					.compose(networkUtils.<StationMeasurements>getDefaultTransformer())
					.subscribe(new Action1<StationMeasurements>() {
						@Override
						public void call(StationMeasurements measurements) {
							remoteViews.setTextViewText(R.id.station_name, station.getStationName());
							remoteViews.setTextViewText(R.id.value, measurements.getLevel().getValue() + " " + measurements.getLevel().getUnit());
							remoteViews.setTextViewText(R.id.date, DateFormat.getDateFormat(context).format(new Date(measurements.getLevelTimestamp())));
							appWidgetManager.updateAppWidget(widgetId, remoteViews);
						}
					}, new ErrorActionBuilder()
							.add(new DefaultErrorAction(context, "failed to fetch measurements"))
							.build());

			// setup click to update
			Intent intent = new Intent(context, StationInfoWidgetProvider.class);
			intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.container, pendingIntent);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}

}
