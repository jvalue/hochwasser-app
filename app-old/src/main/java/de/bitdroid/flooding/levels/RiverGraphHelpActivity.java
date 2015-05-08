package de.bitdroid.flooding.levels;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.utils.BaseActivity;

public class RiverGraphHelpActivity extends BaseActivity {


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.data_river_help);
		getActionBar().setTitle(getString(R.string.help));

		TextView textView = (TextView) findViewById(R.id.intro);
		textView.setText(Html.fromHtml(getString(R.string.help_graph)));

		textView = (TextView) findViewById(R.id.series_water);
		textView.setText(Html.fromHtml(getString(R.string.help_graph_series_water)));

		textView = (TextView) findViewById(R.id.series_mw);
		textView.setText(Html.fromHtml(getString(R.string.help_graph_series_mw)));

		textView = (TextView) findViewById(R.id.series_average_tw);
		textView.setText(Html.fromHtml(getString(R.string.help_graph_series_average_tw)));

		textView = (TextView) findViewById(R.id.series_extreme_tw);
		textView.setText(Html.fromHtml(getString(R.string.help_graph_series_extreme_tw)));


		textView = (TextView) findViewById(R.id.timestamps);
		textView.setText(Html.fromHtml(getString(R.string.help_graph_action_timestamps)));

		textView = (TextView) findViewById(R.id.map);
		textView.setText(Html.fromHtml(getString(R.string.help_graph_action_map)));

		textView = (TextView) findViewById(R.id.normalize);
		textView.setText(Html.fromHtml(getString(R.string.help_graph_action_normalize)));
	}

}
