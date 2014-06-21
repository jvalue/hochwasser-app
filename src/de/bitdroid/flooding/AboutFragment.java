package de.bitdroid.flooding;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.bitdroid.flooding.ods.OdsSourceManager;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;


public final class AboutFragment extends Fragment {

	@Override
	public View onCreateView(
			LayoutInflater inflater, 
			ViewGroup container, 
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.about, container, false);

		OdsSourceManager manager = OdsSourceManager.getInstance(getActivity().getApplicationContext());

		TextView lastSync = (TextView) view.findViewById(R.id.sync_last_value);
		lastSync.setText(formatTime(
					manager.getLastSync(PegelOnlineSource.INSTANCE)));

		TextView lastSyncSuccess = (TextView) view.findViewById(R.id.sync_last_success_value);
		lastSyncSuccess.setText(formatTime(
					manager.getLastSuccessfulSync(PegelOnlineSource.INSTANCE)));

		TextView lastSyncFailure = (TextView) view.findViewById(R.id.sync_last_failure_value);
		lastSyncFailure.setText(formatTime(
					manager.getLastFailedSync(PegelOnlineSource.INSTANCE)));
		return view;
	}

	private final static SimpleDateFormat dateFormatter 
		= new SimpleDateFormat("dd/M/yyyy hh:mm a");

	private String formatTime(Calendar time) {
		if (time == null) return getString(R.string.sync_never);
		else return dateFormatter.format(time.getTime());
	}

}
