package de.bitdroid.flooding.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.ods.BodyOfWater;
import de.bitdroid.flooding.ods.OdsManager;
import de.bitdroid.flooding.utils.StringUtils;
import rx.Observable;
import timber.log.Timber;

public class RiverSelectionFragment extends AbstractSelectionFragment<BodyOfWater> {

	@Inject private OdsManager odsManager;

	public RiverSelectionFragment() {
		super(R.string.menu_select_water_search_hint);
	}


	@Override
	protected ArrayAdapter<BodyOfWater> getAdapter() {
		return new ArrayAdapter<BodyOfWater>(
				getActivity().getApplicationContext(), 
				R.layout.data_item,
				android.R.id.text1) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				BodyOfWater water = getItem(position);
				View view = super.getView(position, convertView, parent);

				TextView text1 = (TextView) view.findViewById(android.R.id.text1);
				text1.setText(StringUtils.toProperCase(water.getName()));
				text1.setTextColor(getResources().getColor(android.R.color.black));

				// TODO
				TextView text2 = (TextView) view.findViewById(android.R.id.text2);
				text2.setText(getString(R.string.data_station_count, 42));
				text2.setTextColor(getResources().getColor(R.color.gray));

				return view;
			}
			
		};
	}


	@Override
	protected void onItemClicked(BodyOfWater item) {
		Timber.d("on item clicked");
	}

	@Override
	protected void onMapClicked() {
		Timber.d("on map clicked");
	}


	@Override
	protected Observable<List<BodyOfWater>> loadItems() {
		return odsManager.getBodyOfWaters();
	}

}
