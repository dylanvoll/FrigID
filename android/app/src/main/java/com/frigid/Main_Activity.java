package com.frigid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class Main_Activity extends AppCompatActivity {

    ArrayList<String> inventory = new ArrayList<String>(Arrays.asList("Lettuce","Milk","Tomatoes","Cheese","Lettuce","Milk","Tomatoes","Cheese","Lettuce","Milk","Tomatoes","Cheese"));
    ArrayList<String> groceries = new ArrayList<String>(Arrays.asList("Limes","Bananas","Flour","Hot Sauce","Limes","Bananas","Flour","Hot Sauce","Limes","Bananas","Flour","Hot Sauce"));
    ListView inventoryList;
    ListView groceriesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // create the TabHost that will contain the Tabs
        final TabHost tabHost = (TabHost)findViewById(R.id.tabhost);

        tabHost.setup();

        TabHost.TabSpec tab1 = tabHost.newTabSpec("Inventory");
        TabHost.TabSpec tab2 = tabHost.newTabSpec("Grocery List");

        // Set the Tab name and Activity
        // that will be opened when particular Tab will be selected
        tab1.setIndicator("Inventory");
        tab1.setContent(R.id.tab1);
        tab2.setIndicator("Grocery List");
        tab2.setContent(R.id.tab2);

        /** Add the tabs  to the TabHost to display. */
        tabHost.addTab(tab1);
        tabHost.addTab(tab2);

        inventoryList = (ListView) findViewById(R.id.list1);
        inventoryList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Main_Activity.this);
                builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        inventory.remove(position);
                        ((ArrayAdapter) inventoryList.getAdapter()).notifyDataSetChanged();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setTitle(Html.fromHtml("<font color='#000000'>Remove Item?</font>"));
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                return false;
            }
        });
        groceriesList = (ListView) findViewById(R.id.list2);
        groceriesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Main_Activity.this);
                builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        groceries.remove(position);
                        ((ArrayAdapter) groceriesList.getAdapter()).notifyDataSetChanged();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setTitle(Html.fromHtml("<font color='#000000'>Remove Item?</font>"));
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                return false;
            }
        });

        inventoryList.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1,inventory));
        groceriesList.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1,groceries));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.plus);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem(tabHost.getCurrentTab());
            }
        });
        onNewIntent(getIntent());
    }

    public void addItem(final int tab){


        AlertDialog.Builder builder = new AlertDialog.Builder(Main_Activity.this);
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.add_item_dialog,null);
        final EditText item = (EditText)view.findViewById(R.id.item_to_add);
        builder.setView(view).setPositiveButton("Add", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {
                if(item.getText().toString().trim().length()>0) {
                    switch (tab) {
                        case 0:
                            inventory.add(item.getText().toString());
                            ((ArrayAdapter) inventoryList.getAdapter()).notifyDataSetChanged();
                            break;
                        case 1:
                            groceries.add(item.getText().toString());
                            ((ArrayAdapter) groceriesList.getAdapter()).notifyDataSetChanged();
                            break;
                        default:
                            break;
                    }
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setTitle(Html.fromHtml("<font color='#000000'>Add Item to List</font>"));
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String action = intent.getAction();
        Toast.makeText(getApplicationContext(),action,Toast.LENGTH_LONG).show();
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        String s = action + "\n\n" + tag.toString();

        // parse through all NDEF messages and their records and pick text type only
        Parcelable[] data = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (data != null) {
            try {
                for (int i = 0; i < data.length; i++) {
                    NdefRecord[] recs = ((NdefMessage)data[i]).getRecords();
                    for (int j = 0; j < recs.length; j++) {
                        if (recs[j].getTnf() == NdefRecord.TNF_WELL_KNOWN &&
                                Arrays.equals(recs[j].getType(), NdefRecord.RTD_TEXT)) {
                            byte[] payload = recs[j].getPayload();
                            String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
                            int langCodeLen = payload[0] & 0077;

                            s += ("\n\nNdefMessage[" + i + "], NdefRecord[" + j + "]:\n\"" +
                                    new String(payload, langCodeLen + 1, payload.length - langCodeLen - 1,
                                            textEncoding) + "\"");
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("TagDispatch", e.toString());
            }
        }

        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG);
    }
}
