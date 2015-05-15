package de.bitdroid.flooding.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.bitdroid.flooding.R;

public class DummyFragment extends AbstractFragment {

    @Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_select_list, container, false);
    }

}

