package com.ak.wifilookout;

import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utils {

    public static void saveSet(SharedPreferences sharedPref, String key, String value) {
        Set<String> oldSet = sharedPref.getStringSet(key, new HashSet<String>());
        Set<String> newSet = new HashSet<String>(oldSet);
        newSet.add(value);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet(key, newSet);
        editor.commit();
    }

    public static void remove(SharedPreferences sharedPref, String key) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(key);
        editor.commit();
    }

    public static void clear(SharedPreferences sharedPref) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();
    }

    public static boolean isSecureNetwork(WifiManager wifiManager, String ssid) {
        List<ScanResult> networkList = wifiManager.getScanResults();
        if (networkList != null) {
            for (ScanResult network : networkList) {
                if (ssid.equals(network.SSID)) {
                    String Capabilities = network.capabilities;
                    if (Capabilities.contains("WEP")
                            || Capabilities.contains("PSK")
                            || Capabilities.contains("EAP")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
