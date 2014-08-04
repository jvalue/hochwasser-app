package de.bitdroid.flooding.levels;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import de.bitdroid.flooding.R;
import it.gmariotti.cardslib.library.internal.Card;

final class StationCharValuesCard extends Card {

	private final CharValue mhw, mw, mnw;
	private final CharValue mthw,mtnw, hthw, ntnw;

	private StationCharValuesCard(
			Context context,
			CharValue mhw,
			CharValue mw,
			CharValue mnw,
			CharValue mthw,
			CharValue mtnw,
			CharValue hthw,
			CharValue ntnw) {

		super(context, R.layout.station_card_char_values);
		this.mhw = mhw;
		this.mw = mw;
		this.mnw = mnw;
		this.mthw = mthw;
		this.mtnw = mtnw;
		this.hthw = hthw;
		this.ntnw = ntnw;
	}


	@Override
	public void setupInnerViewElements(ViewGroup parent, View view) {
		setText(view, R.id.mhw, mhw);
		setText(view, R.id.mw, mw);
		setText(view, R.id.mnw, mnw);
		setText(view, R.id.mthw, mthw);
		setText(view, R.id.mtnw, mtnw);
		setText(view, R.id.hthw, hthw);
		setText(view, R.id.ntnw, ntnw);
	}


	public boolean isEmpty() {
		return mhw.isEmpty() && mw.isEmpty() && mnw.isEmpty()
				&& mthw.isEmpty() && mtnw.isEmpty() && hthw.isEmpty() && ntnw.isEmpty();
	}


	private boolean setText(View view, int resourceId, CharValue charValue) {
		TextView textView = (TextView) view.findViewById(resourceId);
		if (charValue.value != null && charValue.unit != null) {
			textView.setText(charValue.value + " " + charValue.unit);
			return true;
		} else {
			((TableRow) textView.getParent()).setVisibility(View.GONE);
			return false;
		}
	}


	public static final class Builder {

		private final Context context;
		private CharValue mhw, mw, mnw;
		private CharValue mthw,mtnw, hthw, ntnw;


		public Builder(Context context) {
			this.context = context;
		}

		public Builder mhw(Double value, String unit) {
			this.mhw = new CharValue(value, unit);
			return this;
		}

		public Builder mw(Double value, String unit) {
			this.mw = new CharValue(value, unit);
			return this;
		}

		public Builder mnw(Double value, String unit) {
			this.mnw = new CharValue(value, unit);
			return this;
		}

		public Builder mthw(Double value, String unit) {
			this.mthw = new CharValue(value, unit);
			return this;
		}

		public Builder mtnw(Double value, String unit) {
			this.mtnw = new CharValue(value, unit);
			return this;
		}

		public Builder hthw(Double value, String unit) {
			this.hthw = new CharValue(value, unit);
			return this;
		}

		public Builder ntnw(Double value, String unit) {
			this.ntnw = new CharValue(value, unit);
			return this;
		}

		public StationCharValuesCard build() {
			return new StationCharValuesCard(context, mhw, mw, mnw, mthw, mtnw, hthw, ntnw);
		}

	}


	private static final class CharValue {
		public Double value;
		String unit;

		public CharValue(Double value, String unit) {
			this.value = value;
			this.unit = unit;
		}

		public boolean isEmpty() {
			return value == null && unit == null;
		}

	}

}
