package com.victor.iotgateapp;

import com.victor.iot.Endpoint;
import com.victor.iot.IGateway;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.widget.ListView;
import android.widget.TextView;

public class EndpointActivity extends Activity {
	private int nwkaddr;
	private int epindex;
	private IGateway gateway;
	private Endpoint endpoint;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_endpoint);
		
		nwkaddr = getIntent().getIntExtra("nwkaddr", 0);
		epindex = getIntent().getIntExtra("endpoint", -1);
		
        Intent intent = new Intent();
        intent.setAction("com.victor.iot.GATEWAY");
        bindService(intent, conn, Service.BIND_AUTO_CREATE);
	}

	private void initActivity()
	{
		TextView nwkaddr = (TextView) findViewById(R.id.nwkaddr);
		TextView pid = (TextView) findViewById(R.id.profileid);
		TextView did = (TextView) findViewById(R.id.deviceid);
		ListView list = (ListView) findViewById(R.id.listView);
		
		nwkaddr.setText(String.format("%04x", endpoint.nwkaddr));
		pid.setText(String.format("%d", endpoint.profileid));
		did.setText(String.format("%d", endpoint.deviceid));
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
			try {
				endpoint = gateway.getEndpoint(nwkaddr, epindex);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			initActivity();
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
