package com.kedzie.vbox.machine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.kedzie.vbox.R;
import com.kedzie.vbox.api.IMachine;
import com.kedzie.vbox.app.BaseActivity;
import com.kedzie.vbox.app.BundleBuilder;
import com.kedzie.vbox.app.TabSupport;
import com.kedzie.vbox.app.TabSupportViewPager;
import com.kedzie.vbox.app.Utils;
import com.kedzie.vbox.metrics.MetricPreferencesActivity;
import com.kedzie.vbox.task.ConfigureMetricsTask;

/**
 * 
 * @author Marek Kędzierski
 * @apiviz.stereotype activity
 */
public class MachineFragmentActivity extends BaseActivity {
	private static final int REQUEST_CODE_PREFERENCES = 6;
	
	private TabSupport _tabSupport;
	private IMachine _machine;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(Utils.isLargeLand(getResources().getConfiguration())) 
		    finish();
		
		_machine = BundleBuilder.getProxy(getIntent(), IMachine.BUNDLE, IMachine.class);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);		
		getSupportActionBar().setIcon(getApp().getOSDrawable(_machine.getOSTypeId()));
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		ViewPager pager = new ViewPager(this);
		pager.setId(99);
		setContentView(pager);
		_tabSupport = new TabSupportViewPager(this, pager);
		_tabSupport.addTab("Actions", ActionsFragment.class, getIntent().putExtra("dualPane", false).getExtras());
		_tabSupport.addTab("Details", InfoFragment.class,getIntent().getExtras());
		_tabSupport.addTab("Log", LogFragment.class, getIntent().getExtras());
		_tabSupport.addTab("Snapshots", SnapshotFragment.class, getIntent().getExtras());
		
		if(savedInstanceState!=null)
		    getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", getSupportActionBar().getSelectedNavigationIndex());
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.machine_actions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
		    NavUtils.navigateUpTo(this, new Intent(this, MachineListFragmentActivity.class).putExtras(getIntent()));
			return true;
		case R.id.option_menu_preferences:
			startActivityForResult(new Intent(this, PreferencesActivity.class), REQUEST_CODE_PREFERENCES);
			return true;
		}
		return false;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==REQUEST_CODE_PREFERENCES) {
			new ConfigureMetricsTask(this, _machine.getVBoxAPI()).execute(
					Utils.getIntPreference(this, MetricPreferencesActivity.PERIOD),	
					Utils.getIntPreference(this, MetricPreferencesActivity.COUNT) );
		}
	}
}