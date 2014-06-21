package de.bitdroid.flooding.alarms;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.bitdroid.flooding.R;


public final class AlarmsFragment extends Fragment {


	@Override
	public View onCreateView(
			LayoutInflater inflater, 
			ViewGroup container, 
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.alarms, container, false);
		return view;
	}

}
