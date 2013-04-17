package com.victor.iot;

import com.victor.iot.Node;
import com.victor.iot.Endpoint;

interface IGateway
{
	int getNodeNum();
	void getNode(in int i, out Node node);
	void getEndpoint(in int i, out Endpoint endpoint);
	int startConnect(String ip);
}