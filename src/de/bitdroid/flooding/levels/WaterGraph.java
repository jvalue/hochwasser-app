package de.bitdroid.flooding.levels;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.androidplot.Plot;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeriesFormatter;

import de.bitdroid.flooding.utils.Log;


final class WaterGraph implements OnTouchListener {
	
	private final XYPlot graph;
	private final Context context;

	private SeriesManager manager;
	private PointF zoomMinXY, zoomMaxXY, graphMinXY, graphMaxXY;



	public WaterGraph(
			XYPlot graph, 
			String graphName, 
			Context context) {

		this.graph = graph;
		this.context = context;

		graph.setOnTouchListener(this);
		graph.setBorderStyle(Plot.BorderStyle.SQUARE, null, null);
		graph.getLayoutManager().remove(graph.getLegendWidget());
		graph.setTitle(graphName);
		graph.setTicksPerRangeLabel(2);
		graph.setTicksPerDomainLabel(3);
		graph.getGraphWidget().setRangeValueFormat(new DecimalFormat("@@##"));
		graph.getGraphWidget().setDomainValueFormat(new DecimalFormat("@@#"));
		graph.setBorderStyle(Plot.BorderStyle.NONE, null, null);
		graph.setBackgroundColor(Color.BLACK);
		graph.getGraphWidget().getBackgroundPaint().setColor(Color.BLACK);
		graph.getGraphWidget().getGridBackgroundPaint().setColor(Color.BLACK);
	}


	public void setSeries(List<Pair<AbstractSeries, Integer>> series) {
		manager = new SeriesManager();
		for (Pair<AbstractSeries, Integer> s : series) {
			manager.addSeries(s.first, getDefaultFormatter(s.second));
		}
		updateGraph();
	}


	public void setData(Cursor data) {
		manager.reset();
		manager.setData(data);
		graph.redraw();

		graph.calculateMinMaxVals();
		graphMinXY = new PointF(
				graph.getCalculatedMinX().floatValue(),
				graph.getCalculatedMinY().floatValue());
		graphMaxXY = new PointF(
				graph.getCalculatedMaxX().floatValue(),
				graph.getCalculatedMaxY().floatValue());
		if (zoomMinXY == null) zoomMinXY = new PointF(graphMinXY.x, graphMinXY.y);
		if (zoomMaxXY == null) zoomMaxXY = new PointF(graphMaxXY.x, graphMaxXY.y);

		graph.setRangeBoundaries(graphMinXY.y, graphMaxXY.y, BoundaryMode.FIXED);
	}


	public List<String> getSeriesKeys() {
		return manager.getSeriesKeys();
	}


	public boolean isVisible(String seriesKey) {
		return manager.isVisible(seriesKey);
	}


	public void setVisibleSeries(Set<String> seriesKeys) {
		manager.hideAllSeries();
		for (String key : seriesKeys) manager.makeSeriesVisible(key);
		updateGraph();
	}


	private static final String 
			EXTRA_ZOOM_MIN = "zoomMin",
			EXTRA_ZOOM_MAX = "zoomMax";


	public void saveState(Bundle state) {
		manager.saveVisibleSeries(state);
		storePoint(state, EXTRA_ZOOM_MIN, zoomMinXY);
		storePoint(state, EXTRA_ZOOM_MAX, zoomMaxXY);
	}


	public void restoreState(Bundle state) {
		manager.restoreVisibleSeries(state);
		zoomMinXY = restorePoint(state, EXTRA_ZOOM_MIN); 
		zoomMaxXY = restorePoint(state, EXTRA_ZOOM_MAX); 
        graph.setDomainBoundaries(zoomMinXY.x, zoomMaxXY.x, BoundaryMode.FIXED);
		updateGraph();
	}


	private void storePoint(Bundle state, String key, PointF point) {
		state.putFloat(key + "x", point.x);
		state.putFloat(key + "y", point.y);
	}

	private PointF restorePoint(Bundle state, String key) {
		return new PointF(
				state.getFloat(key + "x"),
				state.getFloat(key + "y"));
	}




	private LineAndPointFormatter getDefaultFormatter(int configuration) {
		LineAndPointFormatter formatter = new LineAndPointFormatter();
		formatter.setPointLabelFormatter(null);
		formatter.configure(context, configuration);
		return formatter;
	}


	private void updateGraph() {
		graph.clear();
		for (Pair<AbstractSeries, XYSeriesFormatter<?>> p : manager.getVisibleSeries())
			graph.addSeries(p.first, p.second);
		graph.redraw();
	}



    static final int NONE = 0;
    static final int ONE_FINGER_DRAG = 1;
    static final int TWO_FINGERS_DRAG = 2;
    int mode = NONE;

    private PointF firstFinger;
    private float distBetweenFingers;
	private float scrollingPan;
	private SmoothScrollRunnable scrollRunnable;


    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: // Start gesture
				cancelScrolling();
                firstFinger = new PointF(event.getX(), event.getY());
                mode = ONE_FINGER_DRAG;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
				if (mode == ONE_FINGER_DRAG) {
					scrollRunnable = new SmoothScrollRunnable(scrollingPan);
					new Thread(scrollRunnable).start();
				}
                mode = NONE;
                break;
            case MotionEvent.ACTION_POINTER_DOWN: // second finger
				cancelScrolling();
                distBetweenFingers = spacing(event);
                // the distance check is done to avoid false alarms
                if (distBetweenFingers > 5f) {
                    mode = TWO_FINGERS_DRAG;
				}
                break;
            case MotionEvent.ACTION_MOVE:
				cancelScrolling();
                if (mode == ONE_FINGER_DRAG) {
                    PointF oldFirstFinger = firstFinger;
                    firstFinger = new PointF(event.getX(), event.getY());
					scrollingPan = oldFirstFinger.x - firstFinger.x;
					scroll(scrollingPan);
                    graph.setDomainBoundaries(zoomMinXY.x, zoomMaxXY.x, BoundaryMode.FIXED);
                    graph.redraw();

                } else if (mode == TWO_FINGERS_DRAG) {
                    float oldDist = distBetweenFingers;
                    distBetweenFingers = spacing(event);
                    zoom(oldDist / distBetweenFingers);
                    graph.setDomainBoundaries(zoomMinXY.x, zoomMaxXY.x, BoundaryMode.FIXED);
                    graph.redraw();
                }
                break;
        }
        return true;
    }



    private void zoom(float scale) {
        float domainSpan = zoomMaxXY.x - zoomMinXY.x;
        float domainMidPoint = zoomMaxXY.x - domainSpan / 2.0f;
        float offset = domainSpan * scale / 2.0f;

        zoomMinXY.x = domainMidPoint - offset;
        zoomMaxXY.x = domainMidPoint + offset;

        clampToDomainBounds(false, domainSpan);
    }

    private void scroll(float pan) {
        float domainSpan = zoomMaxXY.x - zoomMinXY.x;
        float step = domainSpan / graph.getWidth();
        float offset = pan * step;
        zoomMinXY.x = zoomMinXY.x + offset;
        zoomMaxXY.x = zoomMaxXY.x + offset;
        clampToDomainBounds(true, domainSpan);
    }

    private void clampToDomainBounds(boolean scroll, float domainSpan) {
		float leftBoundary = graphMinXY.x;
		float rightBoundary = graphMaxXY.x;
        if (zoomMinXY.x < leftBoundary) {
            zoomMinXY.x = leftBoundary;
            if (scroll) zoomMaxXY.x = leftBoundary + domainSpan;
		}
		if (zoomMaxXY.x > rightBoundary) {
            zoomMaxXY.x = rightBoundary;
			if (scroll) zoomMinXY.x = rightBoundary - domainSpan;
        }
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

	private void cancelScrolling() {
		if (scrollRunnable != null) scrollRunnable.stopScrolling();
		scrollRunnable = null;
	}


	private class SmoothScrollRunnable implements Runnable {
		
		private static final long REFRESH_RATE = 5;

		private boolean stopped = false;
		private float pan;

		public SmoothScrollRunnable(float pan) {
			this.pan = pan;
		}

		@Override
		public void run() {
			do {
				if (stopped) return;
				pan *= 0.97;
				scroll(pan);
				graph.setDomainBoundaries(zoomMinXY.x, zoomMaxXY.x, BoundaryMode.FIXED);
				graph.redraw();
				try {
					Thread.sleep(REFRESH_RATE);
				} catch (InterruptedException ie) {
					Log.warning("Thread was interrupted");
				}
			} while (Math.abs(pan) > 0.3);
		}

		public synchronized void stopScrolling() {
			stopped = true;
		}
	}
}
