package com.victor.iotgateapp;

import com.victor.iot.GatewayService;
import com.victor.iot.IGateway;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.ListView;

public class IOTGateApp extends Activity {
	private static final String perf_host_ip = "perf_host_ip";
	private static final String perf_service_switch = "perf_service_switch";
	private static final String perf_auto_start = "perf_auto_start";
	
	SharedPreferences sharePref;
    private IGateway gateway;
    private ListView listView;

	
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Intent intent = new Intent();
        intent.setAction("com.victor.iot.GATEWAY");
        bindService(intent, conn, Service.BIND_AUTO_CREATE);
        
        sharePref = PreferenceManager.getDefaultSharedPreferences(this);
        sharePref.registerOnSharedPreferenceChangeListener(new IOTOnSharedPreferenceChangeListener());
        
        listView = (ListView) findViewById(R.id.listView);        
    }

    public void startService()
    {
    	int ret = -1;
    	
    	SharedPreferences.Editor editor;
    	editor = sharePref.edit();
    	String ip = sharePref.getString(perf_host_ip, "");
    	try {
			ret = gateway.startConnect(ip);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	if (ret == 0)
    		editor.putBoolean(perf_service_switch, true);
    	else
    		editor.putBoolean(perf_service_switch, false);
    	
    	editor.commit();
    }
    
    private void endService()
    {
    	
    }
    
    private class IOTOnSharedPreferenceChangeListener implements OnSharedPreferenceChangeListener{

		@Override
		public void onSharedPreferenceChanged(SharedPreferences pref,
				String key) {
			// TODO Auto-generated method stub
			if (key.equals(perf_service_switch)) {
				if (pref.getBoolean(key, false))
					startService();
				else
					endService();
			}
			return;
		}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        MenuItem setting = menu.getItem(0);
        setting.setOnMenuItemClickListener(new OnMenuItemClickListener(){

			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(IOTGateApp.this, SettingsActivity.class);
				startActivity(intent);
				return false;
			}
        	
        });
        
        MenuItem close = menu.getItem(1);
        close.setOnMenuItemClickListener(new OnMenuItemClickListener(){

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// TODO Auto-generated method stub
				System.exit(0);
				return false;
			}
        	
        });
        return true;
    }
    
    private ServiceConnection conn = new ServiceConnection()
    {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			// TODO Auto-generated method stub
			gateway = IGateway.Stub.asInterface(arg1);
	        NodeAdapter adapter = new NodeAdapter(LayoutInflater.from(IOTGateApp.this),gateway);
	        listView.setAdapter(adapter);
	        
	        if (sharePref.getBoolean(perf_auto_start, false)) {
	        	startService();
	        	adapter.refresh();
	        }

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			// TODO Auto-generated method stub
			
		}
    };
    
    protected void onDestroy()
    {
    	super.onDestroy();
    	unbindService(conn);
    }
}
