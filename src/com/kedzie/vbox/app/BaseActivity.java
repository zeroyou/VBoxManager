package com.kedzie.vbox.app;

import android.os.Build;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;
import com.kedzie.vbox.VBoxApplication;

/**
 * Base Activity for all application activities.  Enables indeterminate progress bar and disables it.
 * @author Marek Kędzierski
 */
public class BaseActivity extends SherlockFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setProgressBarIndeterminateVisibility(false);
		setProgressBarVisibility(false);
	}

	@Override
	protected void onStart() {
		super.onStart();
		setProgressBarIndeterminateVisibility(false);
	}
	
	public VBoxApplication getApp() {
	    return (VBoxApplication)getApplication();
	}
	
	@Override
	public void finish() {
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN)
			overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		super.finish();
	}
}
