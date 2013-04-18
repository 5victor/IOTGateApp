package com.victor.iot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.victor.iotgateapp.R;

public class GatewayService extends Service {
	private static final int STOPPED = 0x1;
	private static final int RUNNING = 0x2;
	
	private static final int GET_TOKEN = 0x5; //client to server
	private static final int QUERY_NODE_NUM = 0x6;
	private static final int QUERY_NODES = 0x7; //c2s
	private static final int QUERY_NODE_ENDPOINTS = 0x8; //c2s
	private static final int SEND_CLUSTER_DATA = 0x9; //s2c c2s
	private static final int ADD_NODES = 0x10; //s2c
	
	private static final String LOG_TAG = "IOTGateApp";
	//private static final int SOF = 'W';
	
	private int status;
	private int token;
	
	private Vector<Node> nodes;
	private int nodeNum;
	private DataInputStream dataInput;
	private DataOutputStream dataOutput;
	private String ipaddr;
	
	private Handler handler;
	private static final int INIT = 0x1;
	private static final int QUERYNODENUM = 0x2;
	private static final int REFRESHNODES = 0x3;
	private int initRet;
	
	private Thread recvThread;
	
	private void SSLInit() throws Exception
	{
		KeyStore ks = KeyStore.getInstance("BKS");
		InputStream inStore = this.getResources().openRawResource(R.raw.mystore);
		ks.load(inStore, "123456".toCharArray());
		TrustManager[] tms = new TrustManager[]{new BKSX509TrustManager(ks)};
		SSLContext ctx = SSLContext.getInstance("SSLv3");
		ctx.init(null, tms, null);
		SSLSocketFactory socketFactory = ctx.getSocketFactory();
		SSLSocket sslSocket = null;
		try {
			sslSocket = (SSLSocket) socketFactory.createSocket("127.0.0.1", 1013);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	//	try {
	//		sslSocket.startHandshake();
	//	} catch (IOException e) {
			// TODO Auto-generated catch block
	//		e.printStackTrace();
	//	}
		
		OutputStream out = null;
		InputStream in = null;
		try {
			out = sslSocket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			in = sslSocket.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		dataInput = new DataInputStream(in);
		dataOutput = new DataOutputStream(out);
	}
	
	private int init(String ip)
	{
		Socket socket = null;
		OutputStream out = null;
		InputStream in = null;
		
		try {
			socket = new Socket(ip, 1013);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (socket == null)
			return -1;
		
		try {
			out = socket.getOutputStream();
			in = socket.getInputStream();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		dataInput = new DataInputStream(in);
		dataOutput = new DataOutputStream(out);

		status = RUNNING;
		recvThread.start();
		return 0;
	}
	
	private void writeHead(int cmd, int len)
	{
		try {
			//dataOutput.write(SOF);
			dataOutput.write(token);
			dataOutput.write(cmd);
			dataOutput.writeInt(len);
			if (len == 0)
				dataOutput.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void writeData(int len, int array[]) throws IOException
	{

		if (len == 0)
			return;
		
		for (int i = 0; i < len; i++) {
			dataOutput.write(array[i]);
		}

		try {
			dataOutput.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	private void getSOF()
	{
		int i;
		int sof;
		
		i = 0;
		sof = 0;
		do {
			i++;
			try {
				sof = dataInput.readUnsignedByte();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (sof == SOF)
				break;
		} while(true);
		
		if (i > 1) {
			Log.v(LOG_TAG, "Bad frame......");
		}
	}
*/
	private class Head {
		int  cmd;
		int data_len;
	}
	
	private Boolean readHead(Head h)
	{
		int t = 0;
		Boolean ret = false;
		//getSOF();
		
		try {
			t = dataInput.readUnsignedByte();
			h.cmd = dataInput.readUnsignedByte();
			h.data_len = dataInput.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (t == token)
			ret = true;
		
		return ret;
	}
	
	private void flushData(Head hdr)
	{
		int i;
		Log.v(LOG_TAG, "flushData:" + hdr.data_len + "bytes");
		for (i = 0; i < hdr.data_len; i++)
			try {
				dataInput.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	private void getToken()
	{
		writeHead(GET_TOKEN, 0);
	}
	
	private void queryNodeNum()
	{
		writeHead(QUERY_NODE_NUM, 0);
	}
	
	private void handleQueryNodeNum(Head hdr)
	{
		if (hdr.data_len != 4) {
			nodeNum = 0;
			flushData(hdr);
		}
		
		try {
			nodeNum = dataInput.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void queryNodes()
	{
		writeHead(QUERY_NODES, 0);
	}
	
	private void handleQueryNodes(Head hdr)
	{
		if (!nodes.isEmpty())
			nodes.clear();
		
		for (int i = 0; i < nodeNum; i++) {
			Node node = new Node();
			try {
				node.nwkaddr = dataInput.readUnsignedShort();
				node.type = dataInput.readInt();
				node.epnum = dataInput.readInt();
				String ieeeaddr = new String();
				for (int j = 0; j < 8; j++) {
					ieeeaddr = ieeeaddr + String.format("%02d",
							dataInput.readUnsignedByte());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
/*
 * Service ... 这是分割线
 * (non-Javadoc)
 * @see android.app.Service#onCreate()
 */
	private class SendThread implements Runnable
	{

		@Override
		public void run() {
			Looper.prepare();
			handler = new Handler(){
				public void handleMessage (Message msg)
				{
					switch(msg.what) {
					case INIT:
						initRet = init(ipaddr);
						notifyHandler();
						break;
					case QUERYNODENUM:
						queryNodeNum();
						break;
					case REFRESHNODES:
						queryNodes();
						break;
						
					}
				}
				
			};
			Looper.loop();
		}
		
	}
	
	private class RecvThread implements Runnable
	{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Head h = new Head();
			Boolean ret;
			
			do {
				ret = readHead(h);
				if (!ret) {
					flushData(h);
					continue;
				}
				
				switch (h.cmd) {
				case QUERY_NODES:
					handleQueryNodes(h);
					notifyHandler();
					break;
				case QUERY_NODE_NUM:
					handleQueryNodeNum(h);
					notifyHandler();
					break;
				}
			} while (true);
			
		}
		
	}
	
	
	public void onCreate()
	{
		super.onCreate();
		nodes = new Vector<Node>();
		gatewayBinder = new GatewayBinder();
		token = 0;
		status = STOPPED;
		Thread sendThread;
		sendThread = new Thread(new SendThread());
		sendThread.start();
		
		recvThread = new Thread(new RecvThread());		
	}
	
	private void waitHandler()
	{
		synchronized(handler) {
			try {
				handler.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void notifyHandler()
	{
		synchronized(handler) {
			handler.notify();
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return gatewayBinder;
	}
	
	private GatewayBinder gatewayBinder;
	public class GatewayBinder extends IGateway.Stub {

		@Override
		public void getNode(int i, Node node) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getEndpoint(int i, Endpoint endpoint)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int startConnect(String ip) throws RemoteException {
			// TODO Auto-generated method stub
			ipaddr = ip;
			
			handler.sendEmptyMessage(INIT);
			waitHandler();
			
			if (initRet != 0)
				return initRet;
			
			return 0;
		}

		@Override
		public int getNodeNum() throws RemoteException {
			// TODO Auto-generated method stub
			if (status != RUNNING)
				return 0;
			
			handler.sendEmptyMessage(QUERYNODENUM);
			waitHandler();
			return nodeNum;
		}

		@Override
		public int refreshNodes() throws RemoteException {
			// TODO Auto-generated method stub
			if (status != RUNNING)
				return 0;
			
			handler.sendEmptyMessage(REFRESHNODES);
			waitHandler();
			return 0;
		}
	}
}
