package de.bitdroid.flooding.levels;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.bitdroid.flooding.R;

public final class DataFragment extends Fragment {
	
    @Override
	public View onCreateView(
			LayoutInflater inflater,
			ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.data, container, false);
		ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
		pager.setAdapter(new DataAdapter(getActivity().getSupportFragmentManager()));

		return view;
    }


	private static final class DataAdapter extends FragmentPagerAdapter {

		public DataAdapter(FragmentManager manager) {
			super(manager);
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public Fragment getItem(int position) {
			if (position == 0) return new RiverSelectionFragment();
			else if (position == 1) return new StationSelectionFragment();
			return null;
		}

	}

}
