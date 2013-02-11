package com.kedzie.vbox.server;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.kedzie.vbox.R;
import com.kedzie.vbox.VBoxApplication;
import com.kedzie.vbox.task.ActionBarTask;

/**
 * Show list of VirtualBox servers
 * @apiviz.stereotype fragment
 */
public class ServerListFragment extends SherlockFragment {
    static final int REQUEST_CODE_ADD = 0xF000;
    static final int REQUEST_CODE_EDIT = 0x0F00;
    static final int RESULT_CODE_SAVE = 0x00F0;
    static final int RESULT_CODE_DELETE = 0x000F;
    private static final String FIRST_RUN_PREFERENCE = "first_run";
    
    /**
     * Handle Server selection
     */
    public static interface OnSelectServerListener {
    	
        /**
         * @param server	the selected {@link Server}
         */
        public void onSelectServer(Server server);
    }
    
    /**
     * Load Servers from DB
     */
    class LoadServersTask extends ActionBarTask<Void, List<Server>> {

        public LoadServersTask() {
            super("LoadServersTask", getSherlockActivity(),  null); 
        }
        @Override 
        protected List<Server> work(Void... params) throws Exception { 
            return _db.query(); 
        }
        @Override 
        protected void onResult(List<Server> result)    {
            _listView.setAdapter(new ServerListAdapter(getSherlockActivity(), result));
            if(result.isEmpty())
            	showAddNewServerPrompt();
        }
    }

    /**
     * Server list adapter
     */
    class ServerListAdapter extends ArrayAdapter<Server> {

    	private LayoutInflater _inflater;
    	
        public ServerListAdapter(Context context, List<Server> servers) {
            super(context, 0, servers);
            _inflater = LayoutInflater.from(context);
        }
        
        @Override
        public boolean hasStableIds() {
        	return true;
        }
        
        @Override
        public long getItemId(int position) {
        	return getItem(position).getId();
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	if(convertView==null) {
            	convertView = _inflater.inflate(R.layout.simple_selectable_list_item, parent, false);
            	convertView.setTag((TextView)convertView.findViewById(android.R.id.text1));
            }
            TextView text1 = (TextView)convertView.getTag();
            text1.setText(getItem(position).toString());
            text1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_list_vbox, 0, 0, 0);
            return convertView;
        }
    }
    
    private OnSelectServerListener _listener;
    private ServerSQlite _db;
    private ListView _listView;
    private boolean _dualPane;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof OnSelectServerListener) 
            _listener = (OnSelectServerListener)activity;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _listView = new ListView(getActivity());
        _dualPane = getActivity().findViewById(R.id.details)!=null;
        _listView.setChoiceMode(_dualPane ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
        _listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	if(_dualPane)
            		_listView.setItemChecked(position, true);
                _listener.onSelectServer(getAdapter().getItem(position));
            }
        });
        registerForContextMenu(_listView);
        return _listView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        _db = new ServerSQlite(getActivity());
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	new LoadServersTask().execute();
    }
    
    @Override 
    public void onDestroy() {
        super.onDestroy();
        _db.close();
    }

    protected void checkIfFirstRun(Server s) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if(!prefs.contains(FIRST_RUN_PREFERENCE)) {
            Editor editor = prefs.edit();
            editor.putBoolean(FIRST_RUN_PREFERENCE, false);
            editor.commit();
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.firstrun_welcome)
                    .setMessage(getString(R.string.firstrun_message, s.getHost(), s.getPort()))
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton("OK", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
            .show();
        }
    }
    
    protected void showAddNewServerPrompt() {
    	new AlertDialog.Builder(getActivity())
        .setTitle(R.string.add_server_title)
        .setMessage(R.string.add_server_question)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setPositiveButton("OK", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	dialog.dismiss();
            		addServer();
            }
        })
        .show();
    }

    protected ServerListAdapter getAdapter() {
        return (ServerListAdapter)_listView.getAdapter();
    }
    
    @Override
    public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, MenuInflater inflater) {
    	if(!_dualPane)
    		inflater.inflate(R.menu.server_list_actions, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.server_list_option_menu_add:
                addServer();
                return true;
            }
        return false;
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        menu.add(Menu.NONE, R.id.server_list_context_menu_select, Menu.NONE, R.string.server_connect);
        menu.add(Menu.NONE, R.id.server_list_context_menu_edit, Menu.NONE, R.string.edit);
        menu.add(Menu.NONE, R.id.server_list_context_menu_delete, Menu.NONE, R.string.delete);
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
    	int position = ((AdapterContextMenuInfo)item.getMenuInfo()).position;
    	final Server s = getAdapter().getItem(position);
    	switch (item.getItemId()) {
    		case R.id.server_list_context_menu_select:
    			_listener.onSelectServer(getAdapter().getItem(position));
    			return true;
    		case R.id.server_list_context_menu_edit:
    			VBoxApplication.launchActivity(getActivity(), new Intent(getActivity(), EditServerActivity.class).putExtra(EditServerActivity.INTENT_SERVER, (Parcelable)s));
    			return true;
    		case R.id.server_list_context_menu_delete:
    			_db.delete(s.getId());
    			getAdapter().remove(s);
    			getAdapter().notifyDataSetChanged();
    			return true;
    	}
    	return false;
    }

    /**
     * Launch activity to create a new Server
     */
    private void addServer() {
    		VBoxApplication.launchActivity(getActivity(), new Intent(getActivity(), EditServerActivity.class).putExtra(EditServerActivity.INTENT_SERVER, (Parcelable)new Server()));
    }
}
