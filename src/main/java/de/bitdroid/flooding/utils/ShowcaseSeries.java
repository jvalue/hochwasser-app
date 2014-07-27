package de.bitdroid.flooding.utils;

import android.view.View;

import com.github.amlcurran.showcaseview.ShowcaseView;


public abstract class ShowcaseSeries implements View.OnClickListener {

	private int currentViewCount = 0;
	private ShowcaseView currentView = null;

	private void showShowcase() {
		if (currentView != null) currentView.hide();

		currentView = getShowcase(currentViewCount);
		if (currentView == null) return;

		currentView.overrideButtonClick(this);
		currentViewCount++;
	}


	@Override
	public void onClick(View view) {
		showShowcase();
	}


	public void start() {
		showShowcase();
	}


	public abstract ShowcaseView getShowcase(int id);

}
