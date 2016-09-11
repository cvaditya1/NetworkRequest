package com.example.aditya.networkrequest;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;

public class ConnectionService extends Service {

    private final IBinder mBinder = new LocalBinder();

    private WifiReceiver mWifiReceiver;

    private ConnectionService THIS;

    public static final String CONNECTIVITY_ACTION_LOLLIPOP = "com.aditya.connectivity_action_lollipop";

    public ConnectionService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder{
        ConnectionService getService(){
            return ConnectionService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        THIS = this;
        mWifiReceiver = new WifiReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(CONNECTIVITY_ACTION_LOLLIPOP);
        mWifiReceiver.registerReceiver(filter);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mWifiReceiver != null) {
            mWifiReceiver.uRregisterReceiver();
        }
    }
}
