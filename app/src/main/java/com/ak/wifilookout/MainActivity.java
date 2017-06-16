package com.ak.wifilookout;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import static com.ak.wifilookout.Utils.isSecureNetwork;
import static com.ak.wifilookout.WifiReceiver.EXTRA_BSSID;
import static com.ak.wifilookout.WifiReceiver.EXTRA_SSID;

public class MainActivity extends AppCompatActivity {

    private static final int FINE_LOCATION_PERMISSION = 1;

    private ComponentName mComponent;
    private boolean mEnabled;
    private Switch mainSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mComponent = new ComponentName(this, WifiReceiver.class);

        SharedPreferences sharedPreferences = this.getSharedPreferences(
                getString(R.string.config_file), Context.MODE_PRIVATE);

        mEnabled = sharedPreferences.getBoolean("Enabled", false);

        if (!mEnabled)
            disableMonitorMode();

        mainSwitch = (Switch) findViewById(R.id.mainSwitch);
        mainSwitch.setChecked(mEnabled);
        mainSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    enableMonitorMode();
                else
                    disableMonitorMode();
            }
        });
    }

    public void enableMonitorMode() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        if (!mEnabled) {
            this.getPackageManager()
                    .setComponentEnabledSetting(mComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            mEnabled = true;
            updateConfig();
        }
        checkActiveNetwork();
    }

    public void disableMonitorMode() {
        if (mEnabled) {
            this.getPackageManager()
                    .setComponentEnabledSetting(mComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            mEnabled = false;
            updateConfig();
        }
    }

    public void checkActiveNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;

        if (isWiFi && activeNetwork.isConnected()) {
            Context context = getApplicationContext();
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMonitorMode();
                } else {
                    disableMonitorMode();
                }
                mainSwitch.setChecked(mEnabled);
                return;
            }
        }
    }

    public void updateConfig() {
        SharedPreferences.Editor editor = getSharedPreferences("config", MODE_PRIVATE).edit();
        editor.putBoolean("Enabled", mEnabled);
        editor.commit();
    }

    public void showSavedSSIDList(View view) {
        Intent intent = new Intent(this, SSIDListActivity.class);
        startActivity(intent);
    }
}

