package com.victor.iot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.victor.iotgateapp.R;

public class GatewayService extends Service {
	enum status {STOPPED, RUNNING, NODE_LEFT, NODE_ADDED};
	private static final int GET_TOKEN = 0x5; //client to server
	private static final int QUERY_NODES = 0x6; //c2s
	private static final int QUERY_NODE_ENDPOINTS = 0x7; //c2s
	private static final int SEND_CLUSTER_DATA = 0x8; //s2c c2s
	private static final int ADD_NODES = 0x9; //s2c
	
	private static final String LOG_TAG = "IOTGateApp";
	//private static final int SOF = 'W';
	final Lock lock = new ReentrantLock();
	final Condition wake = lock.newCondition();
	
	status status;
	private int token;
	
	private SSLSocket sslSocket;
	private Vector<Node> nodes;
	private DataInputStream dataInput;
	private DataOutputStream dataOutput;
	
	private void init() throws Exception
	{
		KeyStore ks = KeyStore.getInstance("BKS");
		InputStream inStore = this.getResources().openRawResource(R.raw.mystore);
		ks.load(inStore, "123456".toCharArray());
		TrustManager[] tms = new TrustManager[]{new BKSX509TrustManager(ks)};
		SSLContext ctx = SSLContext.getInstance("SSLv3");
		ctx.init(null, tms, null);
		SSLSocketFactory socketFactory = ctx.getSocketFactory();
		
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
	
	private Boolean readHead(Head h) throws IOException
	{
		int t;
		
		//getSOF();
		
		t = dataInput.readUnsignedByte();
		h.cmd = dataInput.readUnsignedByte();
		h.data_len = dataInput.readInt();
		if (t != token)
			return false;
		
		return true;
	}
	
	private void getToken() throws IOException
	{
		writeHead(GET_TOKEN, 0);
		
		//getSOF();
		token = dataInput.readUnsignedByte();
		if (dataInput.readUnsignedByte() != GET_TOKEN) {
			Log.v(LOG_TAG, "GET_TOKEN fail");
			token = -1;
		}
		Log.v(LOG_TAG, "token = " + token);
		dataInput.readUnsignedByte();
	}
/*
 * Service ... 这是分割线
 * (non-Javadoc)
 * @see android.app.Service#onCreate()
 */
	public void onCreate()
	{
		super.onCreate();
		nodes = new Vector<Node>();
		gatewayBinder = new GatewayBinder();
		token = 0;
		
		new Thread(new Runnable(){

			@Override
			public void run() {
				lock.lock();
				try {
					wake.await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
				       lock.unlock();
				}
				Log.v(LOG_TAG, "Do SSL connect");
				
				try {
					init();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					getToken();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// TODO Auto-generated method stub

				do {
					
				} while(true);
			}
			
		}).start();
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return gatewayBinder;
	}
	
	private GatewayBinder gatewayBinder;
	public class GatewayBinder extends IGateway.Stub {

		@Override
		public int getNodeNum() throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

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
		public void startConnect() throws RemoteException {
			// TODO Auto-generated method stub
			lock.lock();
			try {
				wake.signal();
			} finally {
		       lock.unlock();
			}
		}
	}
}
