package com.victor.iot;

import com.victor.iot.Node;
import com.victor.iot.Endpoint;

interface IGateway
{
	int getNodeNum();
	int refreshNodes();
	Node getNode(in int i);
	Endpoint getEndpoint(in int i);
	int startConnect(String ip);
}