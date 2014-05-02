package de.bitdroid.flooding;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


final class VolleyManager {

	private static VolleyManager manager;
	public static VolleyManager getInstance(Context context) {
		if (context == null) throw new NullPointerException("context cannot be null");
		if (manager == null) manager = new VolleyManager(context);
		return manager;
	}


	private final RequestQueue requestQueue;

	private VolleyManager(Context context) {
		this.requestQueue = Volley.newRequestQueue(context);
	}

	public RequestQueue getRequestQueue() {
		return requestQueue;
	}

}
