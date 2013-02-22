package com.kedzie.vbox.server;

import java.security.cert.X509Certificate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.kedzie.vbox.VBoxApplication;
import com.kedzie.vbox.api.IVirtualBox;
import com.kedzie.vbox.app.BaseActivity;
import com.kedzie.vbox.app.Utils;
import com.kedzie.vbox.machine.MachineListActivity;
import com.kedzie.vbox.server.ServerListFragment.OnSelectServerListener;
import com.kedzie.vbox.soap.VBoxSvc;
import com.kedzie.vbox.soap.ssl.SSLUtil.AddCertificateToKeystoreTask;
import com.kedzie.vbox.task.DialogTask;

/**
 * VirtualBox server list
 * @author Marek Kędzierski
 * @apiviz.stereotype activity
 * @apiviz.owns com.kedzie.vbox.server.ServerListFragment
 */
public class ServerListFragmentActivity extends BaseActivity implements OnSelectServerListener {

    /**
     * Log on to VirtualBox webservice
     */
    class LogonTask extends DialogTask<Server, IVirtualBox> {
        public LogonTask() { 
            super( "LogonTask", ServerListFragmentActivity.this, null, "Connecting", true);
        }

        @Override
        protected IVirtualBox work(Server... params) throws Exception {
            _vmgr = new VBoxSvc(params[0]);
            _vmgr.logon();
            _vmgr.getVBox().getVersion();
            return _vmgr.getVBox();
        }

        @Override 
        protected void onResult(IVirtualBox vbox) {
        	super.onResult(vbox);
            Utils.toastLong(ServerListFragmentActivity.this, "Connected to VirtualBox v." + vbox.getVersion());
            VBoxApplication.launchActivity(ServerListFragmentActivity.this, new Intent(ServerListFragmentActivity.this, MachineListActivity.class).putExtra(VBoxSvc.BUNDLE, (Parcelable)_vmgr));
        }
    }
    
    /**  
     * For Untrusted certificates prompt user to save certificate to keystore
     */
    private Handler _sslHandler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		final Server server = (Server)msg.getData().getParcelable(Server.BUNDLE);
    		if(msg.getData().getBoolean("isTrusted")) {
    			new LogonTask().execute(server);
    		} else {
    			final X509Certificate[] chain = (X509Certificate[]) msg.getData().getSerializable("certs");
    			
    			X509Certificate root = chain[chain.length-1];
    			String text = String.format("Issuer: %1$s\nSubject: %2$s", root.getIssuerDN().getName(), root.getSubjectDN().getName());
    			
    			new AlertDialog.Builder(ServerListFragmentActivity.this)
	    			.setIcon(android.R.drawable.ic_dialog_alert)
	    			.setTitle("Unrecognized Certificate")
	    			.setMessage("Do you trust this certificate?\n" + text)
	    			.setPositiveButton("Trust", new OnClickListener() {
	    				@Override
	    				public void onClick(DialogInterface dialog, int which) {
	    					new AddCertificateToKeystoreTask(ServerListFragmentActivity.this, server) {
	    						@Override
	    						protected void onResult(Boolean result) {
	    							super.onResult(result);
	    							Utils.toastLong(getContext(), "Successfully updated keystore");
	    							new LogonTask().execute(server);
	    						};
	    					}.execute(chain);
	    					dialog.dismiss();
	    				}
	    			}).setNegativeButton("Cancel", new OnClickListener() {
	    				@Override
	    				public void onClick(DialogInterface dialog, int which) {
	    					dialog.dismiss();
	    				}
	    			}).show();
    		}
    	}
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        if(savedInstanceState==null) {
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            tx.add(android.R.id.content, new ServerListFragment(), "server_list");
            tx.commit();
        }
    }
    
    @Override
    public void onSelectServer(final Server server) {
    	if(server.isSSL())
    	new Thread() {
        	public void run() {
        		try {
        				new VBoxSvc(server).ping(_sslHandler);
				} catch (Exception e) {
					Log.e("ServerListFragment", "error ping", e);
				} 
        	}
        }.start();
        else 
			new LogonTask().execute(server);
    }
}
