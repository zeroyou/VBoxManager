package com.kedzie.vbox.machine;

import org.virtualbox_4_1.LockType;
import org.virtualbox_4_1.SessionState;
import org.virtualbox_4_1.VBoxEventType;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.kedzie.vbox.api.IConsole;
import com.kedzie.vbox.api.IMachine;
import com.kedzie.vbox.api.WebSessionManager;
import com.kedzie.vbox.event.IEvent;
import com.kedzie.vbox.event.IEventListener;
import com.kedzie.vbox.event.IEventSource;

public class EventService extends Service {
	protected static final String TAG = "vbox."+ EventService.class.getSimpleName();

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		HandlerThread thread = new HandlerThread("vbox", HandlerThread.NORM_PRIORITY);
		thread.start();
		
		Handler h = new Handler(thread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				try {
					WebSessionManager _vmgr = msg.getData().getParcelable("vmgr");
					IMachine _machine = _vmgr.getProxy(IMachine.class, msg.getData().getString("machine"));
					if(_vmgr.getVBox().getSessionObject().getState().equals(SessionState.Unlocked))
						_machine.lockMachine(_vmgr.getVBox().getSessionObject(),LockType.Shared);
					IConsole console = _vmgr.getVBox().getSessionObject().getConsole();
					IEventSource evSource = console.getEventSource();
					IEventListener listener = evSource.createListener();
					evSource.registerListener(listener, new VBoxEventType [] { VBoxEventType.Any }, true);
					for(int i=0; i<10; i++) {
						IEvent event = evSource.getEvent(listener, 2000);
						Log.i(TAG, "EVent: " + event);
						Toast.makeText(getApplicationContext(), "Event: "+event, Toast.LENGTH_LONG).show();
						Thread.sleep(2000);
					}
					evSource.unregisterListener(listener);
				} catch (Exception e) {
					Log.e(TAG, "", e);
				}
				Toast.makeText(getApplicationContext(), "Service is finished", Toast.LENGTH_LONG).show();
				stopSelf();
			}
		};
		Message msg = h.obtainMessage();
		Bundle bundle = new Bundle();
		bundle.putParcelable("vmgr", intent.getParcelableExtra("vmgr"));
		bundle.putString("machine", intent.getStringExtra("machine"));
		msg.setData(bundle);
		h.sendMessage(msg);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}