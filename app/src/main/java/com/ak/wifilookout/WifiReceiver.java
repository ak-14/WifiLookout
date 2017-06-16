package com.ak.wifilookout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import static com.ak.wifilookout.Utils.isSecureNetwork;

public class WifiReceiver extends BroadcastReceiver {

    public static final String EXTRA_SSID = "com.ak.wifilookout.extra.SSID";
    public static final String EXTRA_BSSID = "com.ak.wifilookout.extra.BSSID";

    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (info != null && info.isConnected()) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();
            ssid = ssid.replaceAll("^\"|\"$", "");
            String bssid = wifiInfo.getBSSID();

            if(isSecureNetwork(wifiManager, ssid)) return;

            Intent locationServiceIntent = new Intent(context, LocationService.class);
            locationServiceIntent.putExtra(EXTRA_SSID, ssid);
            locationServiceIntent.putExtra(EXTRA_BSSID, bssid);
            context.startService(locationServiceIntent);

        }
    }
}
