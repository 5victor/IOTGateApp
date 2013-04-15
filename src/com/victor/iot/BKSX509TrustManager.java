package com.victor.iot;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class BKSX509TrustManager implements X509TrustManager {
	X509TrustManager trustManager;
	
	public BKSX509TrustManager(KeyStore ks) throws Exception
	{
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);
		
		TrustManager tms[] = tmf.getTrustManagers();
		
		for (int i = 0; i < tms.length; i++) {
			if (tms[i] instanceof X509TrustManager) {
				trustManager = (X509TrustManager) tms[i];
				return;
			}
		}
		
		throw new Exception("Couldn't initialize");
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chian, String authTye)
			throws CertificateException {
		// TODO Auto-generated method stub
		trustManager.checkClientTrusted(chian, authTye);
	}

	@Override
	public void checkServerTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
		// TODO Auto-generated method stub
		trustManager.checkServerTrusted(arg0, arg1);
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		// TODO Auto-generated method stub
		return trustManager.getAcceptedIssuers();
	}

}
