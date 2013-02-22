package com.kedzie.vbox.host;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;

import com.kedzie.vbox.R;
import com.kedzie.vbox.api.IHost;
import com.kedzie.vbox.app.BaseActivity;
import com.kedzie.vbox.app.BundleBuilder;
import com.kedzie.vbox.app.FragmentActivity;
import com.kedzie.vbox.app.FragmentElement;
import com.kedzie.vbox.app.Utils;
import com.kedzie.vbox.host.HostSettingsListFragment.OnSelectHostCategoryListener;
import com.kedzie.vbox.task.ActionBarTask;

/**
 * @apiviz.stereotype activity
 */
public class HostSettingsActivity extends BaseActivity implements OnSelectHostCategoryListener {
	
	private class UpdateIconTask extends ActionBarTask<IHost, IHost> {
		
		public UpdateIconTask() {
			super("UpdateIconTask", HostSettingsActivity.this, _host.getAPI());
		}
		
		@Override
		protected IHost work(IHost... params) throws Exception {
			params[0].getOperatingSystem();
			params[0].getOSVersion();
			return params[0];
		}
		
		@Override
		protected void onResult(IHost result) {
			super.onResult(result);
			Utils.toastLong(HostSettingsActivity.this, "Operating System: %1$s v.%2$s", result.getOperatingSystem(), result.getOSVersion());
		}
	}

	/** Is the dual Fragment Layout active? */
	private boolean _dualPane;
	private IHost _host;
	private String currentCategory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_host = BundleBuilder.getProxy(getIntent(), IHost.BUNDLE, IHost.class);
		
		getSupportActionBar().setTitle("Host Settings");
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		new UpdateIconTask().execute(_host);
		
		setContentView(R.layout.fragment_list_layout);
		FrameLayout detailsFrame = (FrameLayout)findViewById(R.id.details);
		_dualPane = detailsFrame != null && detailsFrame.getVisibility()==View.VISIBLE;
		
		if(savedInstanceState==null) {
			HostSettingsListFragment fragment = new HostSettingsListFragment();
			fragment.setArguments(new BundleBuilder().putParcelable(IHost.BUNDLE, _host).create());
			getSupportFragmentManager().beginTransaction().add(R.id.list, fragment, "list").commit();
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		setSupportProgressBarIndeterminateVisibility(false);
	}
	
	@Override
    public void onSelectCategory(FragmentElement category) {
	    if(_dualPane) {
	        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
	        Utils.detachExistingFragment(getSupportFragmentManager(), tx, currentCategory);
            Utils.addOrAttachFragment(this, getSupportFragmentManager(), tx, R.id.details, category);
            tx.commit();
	    } else {
	        startActivity(new Intent(this, FragmentActivity.class).putExtra(FragmentElement.BUNDLE, category));
	    }
	    currentCategory = category.name;
    }
}
