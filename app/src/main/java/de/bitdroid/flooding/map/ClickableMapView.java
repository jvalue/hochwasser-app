package de.bitdroid.flooding.map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import org.osmdroid.views.MapView;


public class ClickableMapView extends MapView {

	private GestureDetector gestureDetector;

	public ClickableMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (gestureDetector != null) gestureDetector.onTouchEvent(ev);
		return true;
	}


	@Override
	public void setOnClickListener(final View.OnClickListener clickListener) {
		GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onSingleTapUp(MotionEvent event) {
				clickListener.onClick(ClickableMapView.this);
				return true;
			}
		};
		this.gestureDetector = new GestureDetector(listener);
	}

}
