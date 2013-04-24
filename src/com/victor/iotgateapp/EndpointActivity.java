package com.victor.iotgateapp;

import com.victor.iot.IGateway;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;

public class EndpointActivity extends Activity {
	private int nwkaddr;
	private int epindex;
	private IGateway gateway;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_endpoint);
		
		nwkaddr = getIntent().getIntExtra("nwkaddr", 0);
		epindex = getIntent().getIntExtra("index", -1);
		
        Intent intent = new Intent();
        intent.setAction("com.victor.iot.GATEWAY");
        bindService(intent, conn, Service.BIND_AUTO_CREATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_endpoint, menu);
		return true;
	}
	
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
