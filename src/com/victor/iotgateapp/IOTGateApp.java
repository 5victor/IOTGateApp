package com.victor.iotgateapp;

import com.victor.iot.Gateway;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class IOTGateApp extends Activity {
	private Gateway gateway;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        gateway = new Gateway();
        gateway.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
}
