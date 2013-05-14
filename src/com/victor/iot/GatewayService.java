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

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import com.victor.iotgateapp.R;

public class GatewayService {
	private static final int MAX_CLUSTER = 32;
	
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
	//private DataInputStream dataInput;
	//private DataOutputStream dataOutput;
	LittleEndianDataInputStream dataInput;
	LittleEndianDataOutputStream dataOutput;
	private String ipaddr;
	private ClusterData clusterData;
	private ClusterData clusterResult;
	
	private Handler handler;
	private static final int INIT = 0x1;
	private static final int QUERYNODENUM = 0x2;
	private static final int REFRESHNODES = 0x3;
	private static final int CLUSTERDATA = 0x4;
	private int initRet;
	
	private Thread recvThread;
	
	private void SSLInit(InputStream inStore) throws Exception
	{
		KeyStore ks = KeyStore.getInstance("BKS");
//		InputStream inStore = this.getResources().openRawResource(R.raw.mystore);
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
		
		dataInput = new LittleEndianDataInputStream(in);
		dataOutput = new LittleEndianDataOutputStream(out);
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
		
		dataInput = new LittleEndianDataInputStream(in);
		dataOutput = new LittleEndianDataOutputStream(out);

		status = RUNNING;
		recvThread.start();
		getToken();
		return 0;
	}
	
	private void writeHead(int cmd, int len)
	{
		try {
			//dataOutput.write(SOF);
			Log.v(LOG_TAG, "writeHead token=" + token + "cmd=" + cmd + "len=" + len);
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
	
	private void writeData(int len, int array[])
	{

		if (len == 0)
			return;
		
		for (int i = 0; i < len; i++) {
			try {
				dataOutput.write(array[i]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			dataOutput.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void readData(int len, int array[])
	{
		if (len == 0)
			return;
		
		for (int i = 0; i < len; i++) {
			try {
				array[i] = dataInput.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		
		if (h.cmd == GET_TOKEN) {
			token = t;
			return true;
		}

		Log.v(LOG_TAG, "readHead recv token=" + token + "cmd=" +
						h.cmd + "len=" + h.data_len);
		Log.v(LOG_TAG, "token=" + token);
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
	
	private void queryEndpoints()
	{
		int i;
		int nwkaddr[] = new int[2];
		
		
		for (i = 0; i < nodes.size(); i++) {
			writeHead(QUERY_NODE_ENDPOINTS, 2);
			int addr = nodes.get(i).nwkaddr;
			Log.v(LOG_TAG, "queryEndpoint nwkaddr=" + String.format("%04x", addr));
			nwkaddr[0] = addr & 0xFF;
			nwkaddr[1] = (addr >> 8) & 0xFF;
			writeData(2, nwkaddr);
		}
	}
	
	private void sendClusterData()
	{
		//int [] clusterdata = new int[7];
		writeHead(SEND_CLUSTER_DATA, 0);
		
		try {
			dataOutput.writeShort(clusterData.nwkaddr);
			dataOutput.writeShort(clusterData.cluster);
			dataOutput.writeByte(clusterData.srcep);
			dataOutput.writeByte(clusterData.dstep);
			dataOutput.writeByte(clusterData.data_len);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (clusterData.data_len > 0) {
			writeData(clusterData.data_len, clusterData.data);
		}
	}
	
	private void handleQueryNodes(Head hdr)
	{
		if (!nodes.isEmpty())
			nodes.clear();
		
		Log.v(LOG_TAG, "handleQueryNodes");
		for (int i = 0; i < nodeNum; i++) {
			Node node = new Node();
			try {
				node.nwkaddr = dataInput.readUnsignedShort();
				node.type = dataInput.readInt();
				node.epnum = dataInput.readInt();
				String ieeeaddr = new String();
				for (int j = 0; j < 8; j++) {
					int b = dataInput.readUnsignedByte();
					ieeeaddr = ieeeaddr + String.format("%02x", b);
				}
				node.ieeeaddr = ieeeaddr;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			node.endpoints = new Vector<Endpoint>();
			nodes.add(node);
		}
		
		queryEndpoints();
	}
	
	private void handleQueryEndpoints(Head hdr) throws IOException
	{
		int i;
		Endpoint ep = new Endpoint();
		
		ep.index = dataInput.readUnsignedByte();
		ep.nwkaddr = dataInput.readUnsignedShort();
		ep.profileid = dataInput.readUnsignedShort();
		ep.deviceid = dataInput.readUnsignedShort();
		
		ep.inclusternum = dataInput.readUnsignedByte();
		ep.inclusterlist = new int[MAX_CLUSTER];
		for (i = 0; i < MAX_CLUSTER; i++) {
			ep.inclusterlist[i] = dataInput.readUnsignedShort();
		}
		
		ep.outclusternum = dataInput.readUnsignedByte();
		ep.outclusterlist = new int[MAX_CLUSTER];
		for (i = 0; i < MAX_CLUSTER; i++) {
			ep.outclusterlist[i] = dataInput.readUnsignedShort();
		}
			
		Node node = getNode(ep.nwkaddr);
		if (node == null) {
			Log.v(LOG_TAG, "Endpoint nwkaddr not fount, nwkaddr=" +
					String.format("%04x", ep.nwkaddr));
		}
		node.endpoints.add(ep);
	}
	
	private void handleSendClusterData(Head h)
	{
		Log.v(LOG_TAG, "handleSendClusterData called");
		clusterResult = new ClusterData();
		
		try {
			clusterResult.nwkaddr = dataInput.readUnsignedShort();
			clusterResult.cluster = dataInput.readUnsignedShort();
			clusterResult.srcep = dataInput.readUnsignedByte();
			clusterResult.dstep = dataInput.readUnsignedByte();
			clusterResult.data_len = dataInput.readUnsignedByte();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (clusterResult.data_len > 0) {
			clusterResult.data = new int[clusterResult.data_len];
			readData(clusterResult.data_len, clusterResult.data);
		}
		
		notifyHandler();
	}
	
	private Node getNode(int nwkaddr)
	{
		for (int i = 0; i < nodes.size(); i++) {
			Node node = nodes.get(i);
			if (node.nwkaddr == nwkaddr)
				return node;
		}
		
		return null;
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
						break;
					case QUERYNODENUM:
						queryNodeNum();
						break;
					case REFRESHNODES:
						queryNodes();
						//queryEndpoints(); at the end of handleQueryNodes()
						break;
					case CLUSTERDATA:
						sendClusterData();
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
				case GET_TOKEN:
					//already handle by readHead
					Log.v(LOG_TAG, "Get Token token=" + token);
					notifyHandler();
					break;
				case QUERY_NODES:
					handleQueryNodes(h);
					//notifyHandler(); wake refreshNode
					break;
				case QUERY_NODE_NUM:
					handleQueryNodeNum(h);
					notifyHandler();
					break;
				case QUERY_NODE_ENDPOINTS:
					try {
						handleQueryEndpoints(h);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Node lastNode = nodes.get(nodes.size() - 1);
					if (lastNode.epnum == lastNode.endpoints.size()) {
						notifyHandler(); //wake refreshNode
						Log.v(LOG_TAG, "wake refreshNode");
					}
					break;
				case SEND_CLUSTER_DATA:
					handleSendClusterData(h);
					break;
				}
			} while (true);
		}
	}
	
	
	public void start()
	{
		nodes = new Vector<Node>();
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

	public Endpoint getEndpoint(int nwkaddr, int i)
	{
		// TODO Auto-generated method stub
		int j;
		for (j = 0; j < nodes.size(); j++) {
			Node node = nodes.get(j);
			if (node.nwkaddr == nwkaddr)
				return node.endpoints.get(i);
		}
		return null;
	}
	
	public int startConnect(String ip)
	{
		// TODO Auto-generated method stub
		ipaddr = ip;
		
		handler.sendEmptyMessage(INIT);
		waitHandler();
		
		if (initRet != 0)
			return initRet;
		
		return 0;
	}

	public int getNodeNum(){
		// TODO Auto-generated method stub
		if (status != RUNNING)
			return 0;
			
		handler.sendEmptyMessage(QUERYNODENUM);
		waitHandler();
		return nodeNum;
	}

	public int refreshNodes()
	{
		// TODO Auto-generated method stub
		if (status != RUNNING)
			return 0;
			
		handler.sendEmptyMessage(REFRESHNODES);
		waitHandler();
		return 0;
	}
	
	public Node getNodeByIndex(int index)
	{
		return nodes.get(index);
	}
	
	public int getIntClusterData(ClusterData cd)
	{
		handler.sendEmptyMessage(CLUSTERDATA);
		clusterData = cd;
		waitHandler();
		return clusterResult.data[0];
	}
}
