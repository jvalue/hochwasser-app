package de.bitdroid.flooding.levels;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.utils.Assert;
import de.bitdroid.flooding.utils.StringUtils;
import it.gmariotti.cardslib.library.internal.Card;

final class StationInfoCard extends Card {

	private String station, river;
	private Double riverKm;
	private Double lat, lon;
	private Double zeroValue;
	private String zeroUnit;

	private StationInfoCard(
			Context context,
			String station,
			String river,
			Double riverKm,
			Double lat,
			Double lon,
			Double zeroValue,
			String zeroUnit) {

		super(context, R.layout.station_card_meta_data);
		Assert.assertNotNull(station, river);
		this.station = station;
		this.river = river;
		this.riverKm = riverKm;
		this.lat = lat;
		this.lon = lon;
		this.zeroValue = zeroValue;
		this.zeroUnit = zeroUnit;
	}


	@Override
	public void setupInnerViewElements(ViewGroup parent, View view) {
		setText(view, R.id.station, StringUtils.toProperCase(station));
		setText(view, R.id.water, StringUtils.toProperCase(river));
		if (riverKm != null) setText(view, R.id.riverKm, String.valueOf(riverKm));
		if (lat != null && lon != null) setText(view, R.id.coordinates, lat + ", " + lon);
		if (zeroValue != null && zeroUnit != null) setText(view, R.id.zero, zeroValue + " " + zeroUnit);
	}


	private void setText(View view, int resourceId, String text) {
		TextView textView = (TextView) view.findViewById(resourceId);
		textView.setText(text);
	}


	public static final class Builder {

		private final Context context;
		private String station, river;
		private Double riverKm;
		private Double lat, lon;
		private Double zeroValue;
		private String zeroUnit;


		public Builder(Context context) {
			this.context = context;
		}

		public Builder station(String station) {
			this.station = station;
			return this;
		}

		public Builder river(String river) {
			this.river = river;
			return this;
		}

		public Builder riverKm(Double riverKm) {
			this.riverKm = riverKm;
			return this;
		}

		public Builder lat(Double lat) {
			this.lat = lat;
			return this;
		}

		public Builder lon(Double lon) {
			this.lon = lon;
			return this;
		}

		public Builder zeroValue(Double zeroValue) {
			this.zeroValue = zeroValue;
			return this;
		}

		public Builder zeroUnit(String zeroUnit) {
			this.zeroUnit = zeroUnit;
			return this;
		}

		public StationInfoCard build() {
			return new StationInfoCard(context, station, river, riverKm, lat, lon, zeroValue, zeroUnit);
		}
	}

}
