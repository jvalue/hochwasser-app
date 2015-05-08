package de.bitdroid.flooding.utils;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import de.bitdroid.flooding.R;

public abstract class BaseActivity extends Activity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);

    }


	@Override
	public void onBackPressed() {
		super.onBackPressed();
		showExitAnimation();
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch(menuItem.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(menuItem);
	}

	
	protected void showExitAnimation() {
		overridePendingTransition(R.anim.slide_enter_from_left, R.anim.slide_exit_to_right);
	}

}
