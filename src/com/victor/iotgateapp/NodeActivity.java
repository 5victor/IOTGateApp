package com.victor.iotgateapp;

import com.victor.iot.Endpoint;
import com.victor.iot.GatewayService;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class NodeActivity extends Activity {
	private static final String LOG_TAG = "IOTGateApp";
	private static GatewayService gateway;
	private int index;
	
	Node node;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_node);
		
        Intent intent = new Intent();
        intent.setAction("com.victor.iot.GATEWAY");
        gateway = IOTGateApp.gateway;
        initEndpoint();
        index = getIntent().getIntExtra("index", -1);
        Log.v(LOG_TAG, "NodeActivity start index=" + index);
        if (index == -1)
        	this.finish();
        
        
	}
	
	private void initActivity()
	{
		ListView listview = (ListView) findViewById(R.id.listView);
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
		
		String[] ep = new String[node.epnum];
		for(int i= 0; i < node.epnum; i++) {
			ep[i] = "端点：" + node.endpoints.get(i).index;
		}
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_list_item_1, ep);
		listview.setAdapter(arrayAdapter);
		listview.setOnItemClickListener(new ListClickListener());
	}
	
	private class ListClickListener implements OnItemClickListener
    {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int index,
				long arg3) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(NodeActivity.this, EndpointActivity.class);
			intent.putExtra("nwkaddr", node.nwkaddr);
			intent.putExtra("endpoint", index);
			startActivity(intent);
		}
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_node, menu);
		return true;
	}
	
	private void initEndpoint()
	{
		node = gateway.getNodeByIndex(index);
		for (int i = 0; i < node.epnum; i++) {
			Endpoint ep = gateway.getEndpoint(node.nwkaddr, i);
			node.endpoints.add(ep);
		}
		initActivity();
	}
	 
	 protected void onDestroy()
	 {
		 super.onDestroy();
	 }
}
