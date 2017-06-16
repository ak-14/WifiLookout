package com.ak.wifilookout;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

public class ShowDialogActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_dialog);

        showDiaog();
    }

    public void showDiaog() {
        String SSID = null;
        String location = null;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("SSID"))
                SSID = extras.getString("SSID");
            if (extras.containsKey("LOCATION"))
                location = extras.getString("LOCATION");
        }
        DialogFragment dialogFragment = UnknownLocationDialog.newInstance(SSID, location);
        dialogFragment.setCancelable(false);
        dialogFragment.show(getSupportFragmentManager(), "dialog");
    }
}
