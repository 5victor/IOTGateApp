package com.victor.iotgateapp;

import com.victor.iot.Endpoint;
import com.victor.iot.GatewayService;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class EndpointAdapter extends BaseAdapter {
	Endpoint ep;
	GatewayService g;
	LayoutInflater inflater;

	public void init(Endpoint ep, GatewayService g, LayoutInflater i)
	{
		this.ep = ep;
		this.g = g;
		this.inflater = i;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return ep.outclusternum;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}

}
