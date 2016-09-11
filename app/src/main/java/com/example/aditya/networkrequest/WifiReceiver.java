package com.example.aditya.networkrequest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

/**
 * Created by Aditya on 9/11/2016.
 */

public class WifiReceiver {

    private Context mContext;

    private WifiBroadCastReceiver mWifiReceiver;

    private boolean isRegistered = false;

    private ConnectivityManager mConnectivityManager;

    private WifiManager mWifiManager;

    public WifiReceiver(Context context) {
        mContext = context;
    }

    public void registerReceiver(IntentFilter filter) {
        if(filter != null) {
            if (mWifiReceiver == null) {
                mWifiReceiver = new WifiBroadCastReceiver();
            }
            if (mContext != null) {
                if(!isRegistered) {
                    mContext.registerReceiver(mWifiReceiver, filter);
                }
                isRegistered = true;
                registerConnectivityActionOnLollipop();
            }
        }
    }

    public void uRregisterReceiver(){
        if(mWifiReceiver != null) {
            mContext.unregisterReceiver(mWifiReceiver);
        }
        unRegisterConnectivityActionOnLollipop();
        mWifiReceiver = null;
        isRegistered = false;
    }

    private class WifiBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mConnectivityManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && intent.getAction().equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)){

                if(intent.getExtras() != null) {

                    NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
                    if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        getCurrentNetworkInfo(networkInfo);
                    }

                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.d("WifiBroadCastReceiver", "LOLLIPOP 0 ");
                if (mConnectivityManager != null) {
                    NetworkInfo networkInfo;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Network activeNetwork = mConnectivityManager.getActiveNetwork();
                        if (activeNetwork != null) {
                            networkInfo = mConnectivityManager.getNetworkInfo(activeNetwork);
                            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                                getCurrentNetworkInfo(activeNetwork, networkInfo);
                            }
                        }
                    } else {
                        Network[] networks = mConnectivityManager.getAllNetworks();
                        for (Network network : networks) {
                            if (network != null) {
                                networkInfo = mConnectivityManager.getNetworkInfo(network);
                                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                                    getCurrentNetworkInfo(network, networkInfo);
                                }
                            }
                        }
                    }
                }
            }

            if(intent.getExtras() != null) {
                boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                if (noConnectivity) {
                    Log.d("WifiBroadCastReceiver", "EXTRA_NO_CONNECTIVITY ");
                }
            }
        }
    }

    private void getCurrentNetworkInfo(NetworkInfo networkInfo) {
        switch (networkInfo.getState()){
            case CONNECTED:
                Log.d("WifiBroadCastReceiver", "CONNECTED to " + getCurrentSSID(mConnectivityManager));
                break;

            case CONNECTING:
                Log.d("WifiBroadCastReceiver", "CONNECTING to " + getCurrentSSID(mConnectivityManager));
                break;

            case DISCONNECTED:
                Log.d("WifiBroadCastReceiver", "DISCONNECTED from " + getCurrentSSID(mConnectivityManager));
                break;

            default:
                break;
        }
    }

    private void getCurrentNetworkInfo(Network network, NetworkInfo networkInfo) {
        switch (networkInfo.getState()){
            case CONNECTED:
                Log.d("WifiBroadCastReceiver", "CONNECTED to " + getCurrentSSID(mConnectivityManager));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    boolean status = mConnectivityManager.bindProcessToNetwork(network);
                    Log.d("WifiBroadCastReceiver", "M.bindProcessToNetwork: " + status);
                } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    boolean status = ConnectivityManager.setProcessDefaultNetwork(network);
                    Log.d("WifiBroadCastReceiver", "L.bindProcessToNetwork: " + status);
                }
                break;

            case CONNECTING:
                Log.d("WifiBroadCastReceiver", "CONNECTING to " + getCurrentSSID(mConnectivityManager));
                break;

            case DISCONNECTED:
                Log.d("WifiBroadCastReceiver", "DISCONNECTED from " + getCurrentSSID(mConnectivityManager));
                break;

            default:
                break;
        }
    }

    private String getCurrentSSID(ConnectivityManager manager) {
        if(manager == null || manager.getActiveNetworkInfo() == null) {
            return null;
        }
        try {
            if (mWifiManager == null)
                mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            if(wifiInfo != null || !wifiInfo.getSSID().isEmpty()) {
                return wifiInfo.getSSID();
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void registerConnectivityActionOnLollipop() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(mConnectivityManager != null) {
            NetworkRequest.Builder networkRequest_Builder = new NetworkRequest.Builder();
            networkRequest_Builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            networkRequest_Builder.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
            networkRequest_Builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);

            mConnectivityManager.requestNetwork(networkRequest_Builder.build(), MyNetworkCallback);
            mConnectivityManager.registerNetworkCallback(networkRequest_Builder.build(), MyNetworkCallback);
            mConnectivityManager.addDefaultNetworkActiveListener(networkActiveListener);
        }
    }

    private ConnectivityManager.NetworkCallback MyNetworkCallback = new ConnectivityManager.NetworkCallback(){
        @Override
        public void onAvailable(Network network) {
            Log.d("NetworkCallback", "onAvailable ");
            Intent intent = new Intent(ConnectionService.CONNECTIVITY_ACTION_LOLLIPOP);
            intent.putExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

            mContext.sendBroadcast(intent);
        }

        @Override
        public void onLost(Network network) {
            Log.d("NetworkCallback", "onLost ");
            Intent intent = new Intent(ConnectionService.CONNECTIVITY_ACTION_LOLLIPOP);
            intent.putExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, true);

            mContext.sendBroadcast(intent);
        }
    };

    private ConnectivityManager.OnNetworkActiveListener networkActiveListener = new ConnectivityManager.OnNetworkActiveListener() {
        @Override
        public void onNetworkActive() {
            if(mConnectivityManager != null) {
                boolean isDefaultNetworkActive = mConnectivityManager.isDefaultNetworkActive();
                Log.d("NetworkCallback", "OnNetworkActiveListener.onNetworkActive: " + isDefaultNetworkActive);
            }
        }
    };

    private void unRegisterConnectivityActionOnLollipop() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        if(mConnectivityManager != null) {
            mConnectivityManager.unregisterNetworkCallback(MyNetworkCallback);
        }
    }

    public boolean isRegistered() {
        return isRegistered;
    }
}
