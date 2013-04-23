package com.victor.iotgateapp;

import com.victor.iot.IGateway;
import com.victor.iot.Node;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;
import android.widget.TextView;

public class NodeActivity extends Activity {
	private static final String LOG_TAG = "IOTGateApp";
	private IGateway gateway;
	private int index;
	
	Node node;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_node);
		
        Intent intent = new Intent();
        intent.setAction("com.victor.iot.GATEWAY");
        bindService(intent, conn, Service.BIND_AUTO_CREATE);
        
        index = getIntent().getIntExtra("index", -1);
        Log.v(LOG_TAG, "NodeActivity start index=" + index);
        if (index == -1)
        	this.finish();
        
        
	}
	
	private void initActivity()
	{
		ListView listview = (ListView) findViewById(R.id.EndpointList);
		TextView nwkaddr = (TextView) findViewById(R.id.nwkaddr);
		nwkaddr.setText(String.format("%04x", node.nwkaddr));
		
		TextView type = (TextView) findViewById(R.id.type);
		switch(node.type) {
		case Node.ZC:
			type.setText("协调器");
			break;
		case Node.ZR:
			type.setText("路由器");
			break;
		case Node.ZED:
			type.setText("终端设备");
			break;
		}
		
		TextView ieeeaddr = (TextView) findViewById(R.id.ieeeaddr);
		ieeeaddr.setText(node.ieeeaddr);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_node, menu);
		return true;
	}
	
	 private ServiceConnection conn = new ServiceConnection()
	 {

			@Override
			public void onServiceConnected(ComponentName arg0, IBinder arg1) {
				// TODO Auto-generated method stub
				gateway = IGateway.Stub.asInterface(arg1);
				try {
					node = gateway.getNode(index);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				initActivity();
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
				
			}
	 };
	 
	 protected void onDestroy()
	 {
		 super.onDestroy();
		 unbindService(conn);
	 }
}
