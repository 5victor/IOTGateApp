package com.victor.iotgateapp;

import java.util.Timer;
import java.util.TimerTask;

import com.victor.iot.ClusterData;
import com.victor.iot.Endpoint;
import com.victor.iot.GatewayService;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class EndpointActivity extends Activity {
	private int nwkaddr;
	private int epindex;
	private GatewayService gateway;
	private Endpoint endpoint;
	String [] outcluster;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_endpoint);
		
		nwkaddr = getIntent().getIntExtra("nwkaddr", 0);
		epindex = getIntent().getIntExtra("endpoint", -1);
		
        Intent intent = new Intent();
        intent.setAction("com.victor.iot.GATEWAY");
        gateway = IOTGateApp.gateway;
        endpoint = gateway.getEndpoint(nwkaddr, epindex);
        
        outcluster = new String[endpoint.outclusternum];
        initActivity();
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
		
		//EndpointAdapter adapter = new EndpointAdapter();
		//adapter.init(endpoint, gateway);
		for (int i = 0; i < outcluster.length; i++) {
			outcluster[i] = "0";
		}
		
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_list_item_1, outcluster);
		list.setAdapter(arrayAdapter);
		
		initTimer();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_endpoint, menu);
		return true;
	}

	 protected void onDestroy()
	 {
		 super.onDestroy();
	 }
	 
	 private final Timer timer = new Timer(); 
	 private TimerTask task;
	 
	 private void initTimer()
	 {
		 final ClusterData [] cd = new ClusterData[endpoint.outclusternum];
		 
		 for (int i = 0; i < endpoint.outclusternum; i++) {
			 cd[i] = new ClusterData();
			 cd[i].nwkaddr = endpoint.nwkaddr;
			 cd[i].cluster = endpoint.outclusterlist[i];
			 cd[i].srcep = 0;
			 cd[i].dstep = endpoint.index;
			 cd[i].data_len = 0;
		 }
		 
		 task = new TimerTask()
		 {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				for (int i = 0; i < endpoint.outclusternum; i++) {
					outcluster[i] = Integer.toString(gateway.getIntClusterData(cd[i]));
				}
			}
			 
		 };
		 timer.schedule(task, 1000); //ms
	 }
}
