package com.ak.wifilookout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

public class SSIDListActivity extends AppCompatActivity {

    private static final int CONTEXT_MENU_SHOW_ID = 1;
    private static final int CONTEXT_MENU_DELETE_ID = 2;

    private static SharedPreferences sharedPref;
    private ListView mListView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ssid_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_list:
                clearList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ssid_list);

        sharedPref = this.getSharedPreferences(
                getString(R.string.saved_ssid_locations), Context.MODE_PRIVATE);

        showSavedNetworks();
    }

    public void showSavedNetworks() {
        mListView = (ListView) findViewById(R.id.SSIDList);

        Map<String, ?> allEntries = sharedPref.getAll();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<String>(allEntries.keySet())
        );
        mListView.setAdapter(arrayAdapter);
        mListView.setLongClickable(true);
        registerForContextMenu(mListView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String SSID = (String) parent.getItemAtPosition(position);
                AlertDialog.Builder SSIDDialog = new AlertDialog.Builder(SSIDListActivity.this);
                SSIDDialog.setTitle(SSID);
                //SSIDDialog.setMessage("Choose an option");
                SSIDDialog.setPositiveButton("Show", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showMap(SSID);
                    }
                });
                SSIDDialog.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Utils.remove(sharedPref, SSID);
                        recreate();
                    }
                });
                SSIDDialog.show();
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view,
                                    ContextMenu.ContextMenuInfo contextMenuInfo) {
       if (view.getId() == R.id.SSIDList) {
           contextMenu.add(Menu.NONE, CONTEXT_MENU_SHOW_ID, Menu.NONE, "Show");
           contextMenu.add(Menu.NONE, CONTEXT_MENU_DELETE_ID, Menu.NONE, "Delete");
       }
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
        String SSID = (String) mListView.getItemAtPosition(info.position);
        switch (menuItem.getItemId()) {
            case CONTEXT_MENU_SHOW_ID:
                showMap(SSID);
            case CONTEXT_MENU_DELETE_ID:
                Utils.remove(sharedPref, SSID);
                recreate();
                return true;
            default:
                return super.onContextItemSelected(menuItem);
        }
    }

    public void showMap(String SSID) {
        HashSet<String> savedLocations = (HashSet<String>) sharedPref.getStringSet(SSID, new HashSet<String>());
        Intent mapIntent = new Intent(getApplicationContext(), MapActivity.class);
        mapIntent.putExtra("locations", savedLocations);
        startActivity(mapIntent);
    }

    public void clearList() {
        AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);
        confirmDialog.setTitle("Clear List");
        confirmDialog.setMessage("Are you sure you want to clear the list?");
        confirmDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Utils.clear(sharedPref);
                recreate();
            }
        });
        confirmDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        confirmDialog.show();
    }
}
