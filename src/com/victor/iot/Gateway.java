package com.victor.iot;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Gateway {
	enum status {STOPPED, RUNNING, NODE_LEFT, NODE_ADDED};
	
	status status;
	
	private SSLSocketFactory socketFactory;
	private SSLSocket sslSocket;
	
	private void init()
	{
		socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		try {
			sslSocket = (SSLSocket) socketFactory.createSocket("127.0.0.1", 1013);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			sslSocket.startHandshake();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void start(){
		new Thread(){
			public void run(){
				init();
				
			}
		}.start();
	}
	
	//Called by JNI when network change
	public void indicate()
	{
		//refresh TreeWindow so doesn't need IOTManage
	}
	
	//status modify by JNI?and use a function indacator
	public native status getStatus();
	public native int getNodeNum();
	
	// return a vector?
	public native Node getNode(int i);
	/* all in one Node
	//public native int getEndpointNum(Node node); //Endpoint
	public native Endpoint getEndpoint(Node node, int i);
	//public native int getClusterNum(Endpoint endpoint);
	public native Cluster getCluster(Endpoint endpoint, int i);
	*/
	
	//ZDO method
	
	//ZCL method
}
