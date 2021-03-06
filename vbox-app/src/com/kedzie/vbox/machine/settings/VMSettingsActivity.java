package com.kedzie.vbox.machine.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.kedzie.vbox.R;
import com.kedzie.vbox.api.IMachine;
import com.kedzie.vbox.api.ISession;
import com.kedzie.vbox.api.jaxb.LockType;
import com.kedzie.vbox.app.BaseActivity;
import com.kedzie.vbox.app.BundleBuilder;
import com.kedzie.vbox.app.FragmentActivity;
import com.kedzie.vbox.app.FragmentElement;
import com.kedzie.vbox.app.Utils;
import com.kedzie.vbox.machine.settings.CategoryFragment.OnSelectCategoryListener;
import com.kedzie.vbox.soap.VBoxSvc;
import com.kedzie.vbox.task.ActionBarTask;

/**
 * Obtain a write-lock and then edit virtual machine settings
 * 
 * @apiviz.stereotype activity
 */
public class VMSettingsActivity extends BaseActivity implements OnSelectCategoryListener {
    public static final String MUTABLE_KEY = "mutable";

    /**
     * Get Write Lock on machine 
     */
    class LockMachineTask extends ActionBarTask<IMachine, IMachine> {
    	
        public LockMachineTask() { 
        	super(VMSettingsActivity.this, VMSettingsActivity.this._vmgr);
        
        }
        @Override 
        protected IMachine work(IMachine... m) throws Exception {
            ISession session = _vmgr.getVBox().getSessionObject();
            m[0].lockMachine(session, LockType.WRITE);
            return session.getMachine();
        }
        
        @Override
        protected void onSuccess(IMachine result) {
            super.onSuccess(result);
            _mutable = result;
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            tx.add(R.id.list, Fragment.instantiate(VMSettingsActivity.this, CategoryFragment.class.getName(), new BundleBuilder().putProxy(IMachine.BUNDLE, _mutable).create()));
            tx.commit();
        }
    }
    
    /**
     * Save settings
     */
    class SaveSettingsTask extends ActionBarTask<IMachine, Integer> {
    	
        public SaveSettingsTask() {
        	super(VMSettingsActivity.this, VMSettingsActivity.this._vmgr);
        }

        @Override 
        protected Integer work(IMachine... m) throws Exception {
            m[0].saveSettings();
            _vmgr.getVBox().getSessionObject().unlockMachine();
            return 1;
        }
        
        @Override
        protected void onSuccess(Integer result) {
            super.onSuccess(result);
            Utils.toastLong(VMSettingsActivity.this, VMSettingsActivity.this.getString(R.string.toast_saved_settings));
            finish();
        }
    }
    
    /**
     * Discard settings
     */
    class DiscardSettingsTask extends ActionBarTask<IMachine, Integer> {
    	
        public DiscardSettingsTask() { 
        	super(VMSettingsActivity.this, VMSettingsActivity.this._vmgr);
        }
        
        @Override 
        protected Integer work(IMachine... m) throws Exception {
            m[0].discardSettings();
            _vmgr.getVBox().getSessionObject().unlockMachine();
            return 1;
        }
        @Override
        protected void onSuccess(Integer result) {
            super.onSuccess(result);
            Utils.toastLong(VMSettingsActivity.this, getString(R.string.toast_discarding_settings));
            finish();
        }
    }

	/** Is the dual Fragment Layout active? */
	private boolean _dualPane;
	
	/** VirtualBox API */
	private VBoxSvc _vmgr;
	
	/** Immutable vm reference */
	private IMachine _machine;
	
	/** Mutable vm reference.  Initialized when machine is successfully WRITE-Locked */
	private IMachine _mutable;
	
	/** Currently selected settings category */
	private String currentCategory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		_vmgr = BundleBuilder.getVBoxSvc(getIntent());
		_machine = BundleBuilder.getProxy(getIntent(), IMachine.BUNDLE, IMachine.class);
		
		getSupportActionBar().setTitle(_machine.getName() + " Settings");
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setIcon(getApp().getOSDrawable(_machine.getOSTypeId()));
		
		setContentView(R.layout.fragment_list_layout);
		FrameLayout detailsFrame = (FrameLayout)findViewById(R.id.details);
		_dualPane = detailsFrame != null && detailsFrame.getVisibility()==View.VISIBLE;
		
		if(savedInstanceState==null) 
		    new LockMachineTask().execute(_machine);
		else {
	        _mutable = savedInstanceState.getParcelable(MUTABLE_KEY);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    if(_mutable!=null)
	        BundleBuilder.putProxy(outState, MUTABLE_KEY, _mutable);
	}
	
	@Override
    public void onSelectCategory(FragmentElement category) {
	    if(_dualPane) {
	        FragmentTransaction tx = Utils.setCustomAnimations(getSupportFragmentManager().beginTransaction());
	        Utils.detachFragment(getSupportFragmentManager(), tx, currentCategory);
            Utils.addOrAttachFragment(this, getSupportFragmentManager(), tx, R.id.details, category);
            tx.commit();
	    } else {
	        Utils.startActivity(this, new Intent(this, FragmentActivity.class).putExtra(FragmentElement.BUNDLE, category));
	    }
	    currentCategory = category.name;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.machine_settings, menu);
        return true;
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.option_menu_save:
			saveSettings();
			return true;
		case R.id.option_menu_discard:
		    discardSettings();
		    return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return false;
	}

	@Override 
	public void onBackPressed() {
		new AlertDialog.Builder(this)
	        .setTitle("Save Changes?")
	        .setMessage("Do you wish to save changes?")
	        .setIcon(android.R.drawable.ic_dialog_info)
	        .setPositiveButton("OK", new OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                dialog.dismiss();
	                saveSettings();
	                finish();
	            }
	        })
	        .setNegativeButton("Discard", new OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                dialog.dismiss();
	                discardSettings();
	                finish();
	            }
	        })
	        .show();
	}
	
	private void saveSettings() {
		new SaveSettingsTask().execute(_mutable);
	}
	
	private void discardSettings() {
		if(_mutable!=null)
			new DiscardSettingsTask().execute(_mutable);
	}
	
	@Override
    public void finish() {
        super.finish();
        Utils.overrideBackTransition(this);
    }
}
