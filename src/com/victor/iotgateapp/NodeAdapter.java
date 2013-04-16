package com.victor.iotgateapp;

import com.victor.iot.IGateway;

import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

class NewNodeReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub

	}

}




public class NodeAdapter extends BaseAdapter {
	LayoutInflater inflater;
	IGateway gateway;
	
	public NodeAdapter(LayoutInflater i, IGateway g)
	{
		inflater = i;
		gateway = g;
	}

	@Override
	//是不是count变了listview就会刷新
	public int getCount() {
		// TODO Auto-generated method stub
		int count = 0;
		try {
			count = gateway.getNodeNum();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return count;
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
		return null;
	}
	
	class NewNodeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub

		}

	}

}
