package de.bitdroid.flooding.ui;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.ods.BodyOfWater;
import de.bitdroid.flooding.ods.OdsManager;
import de.bitdroid.flooding.utils.StringUtils;
import rx.Observable;
import timber.log.Timber;

public class RiverSelectionActivity extends AbstractSelectionActivity<BodyOfWater> {

	static final String EXTRA_BODY_OF_WATER = "EXTRA_BODY_OF_WATER";

	@Inject private OdsManager odsManager;

	public RiverSelectionActivity() {
		super(R.string.menu_select_water_search_hint, R.layout.item_data);
	}


	@Override
	protected void onMapClicked() {
		Timber.d("on map clicked");
	}


	@Override
	protected Observable<List<BodyOfWater>> loadItems() {
		return odsManager.getBodyOfWaters();
	}


	@Override
	protected void setDataView(final BodyOfWater water, View view) {
		// water name
		TextView text1 = (TextView) view.findViewById(android.R.id.text1);
		text1.setText(StringUtils.toProperCase(water.getName()));
		text1.setTextColor(getResources().getColor(android.R.color.black));

		// station count
		TextView text2 = (TextView) view.findViewById(android.R.id.text2);
		text2.setText(getString(R.string.data_station_count, water.getStationCount()));
		text2.setTextColor(getResources().getColor(R.color.gray));

		// return on click
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra(EXTRA_BODY_OF_WATER, water);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
	}


	@Override
	protected List<BodyOfWater> filterItems(CharSequence constraint, List<BodyOfWater> items) {
		List<BodyOfWater> result = new ArrayList<>(items);
		if (constraint == null || constraint.length() == 0) return result;
		String stringConstraint= constraint.toString().toLowerCase();
		Iterator<BodyOfWater> iter = result.iterator();
		while (iter.hasNext()) {
			if (!iter.next().getName().toLowerCase().contains(stringConstraint)) {
				iter.remove();
			}
		}
		return result;
	}

}
