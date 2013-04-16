package com.victor.iotgateapp;

import com.victor.iot.GatewayService;
import com.victor.iot.IGateway;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

public class IOTGateApp extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Intent intent = new Intent();
        intent.setAction("com.victor.iot.GATEWAY");
        bindService(intent, conn, Service.BIND_AUTO_CREATE);
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
    
    private IGateway gateway;
    private ServiceConnection conn = new ServiceConnection()
    {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			// TODO Auto-generated method stub
			gateway = IGateway.Stub.asInterface(arg1);
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
