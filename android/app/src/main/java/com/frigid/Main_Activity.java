package com.frigid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main_Activity extends AppCompatActivity {

    ArrayList<Ingredient> inventory = new ArrayList<>();
    ArrayList<Ingredient> groceries = new ArrayList<>();
    ListView inventoryList;
    ListView groceriesList;
    IngredientArrayAdapter inventoryAdapter;
    IngredientArrayAdapter groceryListAdapter;
    private NfcAdapter mNfcAdapter;
    private NFC_Utility nfc_util;
    IngredientArrayAdapter mAdapter;
    ListView mListView;
    public static String ndefString = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfc_util = new NFC_Utility(getApplicationContext(),this);
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
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener(){
            @Override
            public void onTabChanged(String tabId) {
                if("Inventory".equals(tabId)) {
                    mListView = inventoryList;
                }
                if("Grocery List".equals(tabId)) {
                    mListView = groceriesList;
                }
            }});
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
        Ingredient coke = new Ingredient("0496340",2, "Coca-Cola");
        Ingredient cheerios = new Ingredient("016000451377", 5, "Cheerios");

        inventory.add(coke);
        inventory.add(cheerios);
        groceries.add(coke);
        groceries.add(cheerios);



        onNewIntent(getIntent());
    }

    public void loadUI(){
        final ProgressBar waiting = (ProgressBar)findViewById(R.id.waiting);
        final TabHost tabHost = (TabHost)findViewById(R.id.tabhost);

        inventoryAdapter = new IngredientArrayAdapter(getApplicationContext(),this,inventory);
        groceryListAdapter = new IngredientArrayAdapter(getApplicationContext(),this,groceries);
        inventoryList.setAdapter(inventoryAdapter);
        groceriesList.setAdapter(groceryListAdapter);
        mListView = inventoryList;
        mAdapter = inventoryAdapter;
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.plus);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem(tabHost.getCurrentTab());
            }
        });
        /*CountDownTimer timer = new CountDownTimer(1000,2000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

            }
        };
        timer.start();*/
        waiting.setVisibility(View.INVISIBLE);
        tabHost.setVisibility(View.VISIBLE);
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
                            //inventory.add(item.getText().toString());
                            ((ArrayAdapter) inventoryList.getAdapter()).notifyDataSetChanged();
                            break;
                        case 1:
                            //groceries.add(item.getText().toString());
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
    protected void onResume() {
        super.onResume();

        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, mNfcAdapter);

        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        String s = action + "\n\n" + tag.getId() + "\n" + tag.getTechList()[1];
        // parse through all NDEF messages and their records and pick text type only
        String[] techList = tag.getTechList();
        String searchedTech = Ndef.class.getName();

        for (String tech : techList) {
            if (searchedTech.equals(tech)) {
                NFC_Utility.NdefReaderTask reader = nfc_util.new NdefReaderTask();
                reader.execute(tag);
                break;
            }
        }
        /*if(nfc_util.writableTag(tag)) {
            //writeTag here
            NFC_Utility.WriteResponse wr = nfc_util.writeTag(nfc_util.getTagAsNdef(ingredientsToPayload()), tag);
            String message = (wr.getStatus() == 1? "Success: " : "Failed: ") + wr.getMessage();
            Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
        }*/
    }

    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }


    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    /*private String ingredientsToPayload(){
        String payloadString = "";
        for(String ingredient:inv){
            payloadString += ingredient + " " + Collections.frequency(inventory,ingredient) + "\r\n";
        }
        return payloadString;
    }*/





}
