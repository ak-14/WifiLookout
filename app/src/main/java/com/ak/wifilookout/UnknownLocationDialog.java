package com.ak.wifilookout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class UnknownLocationDialog extends DialogFragment {

    public UnknownLocationDialog() {}

    public static UnknownLocationDialog newInstance(String SSID, String location) {
        UnknownLocationDialog dialog = new UnknownLocationDialog();
        Bundle bundle = new Bundle();
        bundle.putString("SSID", SSID);
        bundle.putString("LOCATION", location);
        dialog.setArguments(bundle);
        return dialog;
    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String SSID = getArguments().getString("SSID");
        final String location = getArguments().getString("LOCATION");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(SSID);
        String content = "You are connected from an unknown location. " +
                "This could be a malicious network. " +
                "If you recognize this location, you can save it to avoid this warning in the future. " +
                "If you don't, you should disable your Wifi now!";
        builder.setMessage(content);
        builder.setPositiveButton("Save",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPref = getContext().getSharedPreferences(
                                getString(R.string.saved_ssid_locations), Context.MODE_PRIVATE);
                        Utils.saveSet(sharedPref, SSID, location);
                        Intent ssidList = new Intent(getContext(), SSIDListActivity.class);
                        startActivity(ssidList);
                        getActivity().finish();
                    }
                });
        builder.setNegativeButton("Ignore",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getActivity().finish();
                    }
                });
        builder.setNeutralButton("Disable Wifi",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Context context = getActivity().getApplicationContext();
                        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                        wifi.setWifiEnabled(false);
                        getActivity().finish();
                    }
                });
        return builder.create();
    }
}
