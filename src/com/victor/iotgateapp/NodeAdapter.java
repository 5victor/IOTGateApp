package com.victor.iotgateapp;

import com.victor.iot.GatewayService;
import com.victor.iot.Node;

import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class NodeAdapter extends BaseAdapter {
	private static final String LOG_TAG = "IOTGateApp";
	
	LayoutInflater inflater;
	GatewayService gateway;
	int nodeNum;
	
	public NodeAdapter(LayoutInflater i, GatewayService g)
	{
		inflater = i;
		gateway = g;
	}
	
	public void refresh()
	{
		Log.v(LOG_TAG, "refresh called");
		nodeNum = gateway.getNodeNum();
		gateway.refreshNodes();
	}

	@Override
	//是不是count变了listview就会刷新
	public int getCount() {
		// TODO Auto-generated method stub
		Log.v(LOG_TAG, "getCount called:nodeNum=" + nodeNum);

		
		return nodeNum;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.node_info, null);
		}
		
		Node node;// = new Node();
		Log.v(LOG_TAG, "getView: position=" + position);
		node = gateway.getNodeByIndex(position);
		
		fillNodeInfo(convertView, node);
		
		return convertView;
	}
	
	private void fillNodeInfo(View view, Node node) {
		TextView nwkaddr;
		TextView type;
		
		nwkaddr = (TextView) view.findViewById(R.id.nwkaddr);
		type = (TextView) view.findViewById(R.id.type);
		nwkaddr.setText(String.format("%04x", node.nwkaddr));
		switch (node.type) {
		case Node.ZC:
			type.setText("ZC");
			break;
		case Node.ZR:
			type.setText("ZR");
			break;
		case Node.ZED:
			type.setText("ZED");
			break;
		}
	}
	
	class NewNodeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub

		}
	}

}
