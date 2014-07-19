package de.bitdroid.flooding.alarms;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.dataselection.Extras;
import de.bitdroid.flooding.utils.StringUtils;


public final class SelectLevelFragment extends Fragment implements Extras {


	public static SelectLevelFragment newInstance(String riverName, String stationName) {
		SelectLevelFragment fragment = new SelectLevelFragment();
		Bundle extras = new Bundle();
		extras.putString(EXTRA_WATER_NAME, riverName);
		extras.putString(EXTRA_STATION_NAME, stationName);
		fragment.setArguments(extras);
		return fragment;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}


	@Override
	public View onCreateView(
			LayoutInflater inflater, 
			ViewGroup container, 
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.alarms_new, container, false);

		TextView riverView = (TextView) view.findViewById(R.id.river);
		TextView stationView = (TextView) view.findViewById(R.id.station);

		String river = getArguments().getString(EXTRA_WATER_NAME);
		String station = getArguments().getString(EXTRA_STATION_NAME);

		riverView.setText(StringUtils.toProperCase(river));
		stationView.setText(StringUtils.toProperCase(station));

		Button okButton = (Button) view.findViewById(R.id.ok);
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				SelectLevelFragment.this.getActivity().getSupportFragmentManager()
					.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
				SelectLevelFragment.this.getActivity().finish();
				Toast.makeText(
					SelectLevelFragment.this.getActivity(), 
					getString(R.string.alarms_new_created), 
					Toast.LENGTH_SHORT)
					.show();
			}
		});

		return view;
	}

}
