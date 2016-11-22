package com.frigid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
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
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
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

    public void refreshAdapters(){
        this.inventoryAdapter.notifyDataSetChanged();
        this.groceryListAdapter.notifyDataSetChanged();
    }

    public void saveToFile(String ndef_result){
        File file = new File(getFilesDir(),"ndef_result.txt");
        FileOutputStream outputStream;
        try{
            file.createNewFile();
            outputStream = openFileOutput("ndef_result.txt", Context.MODE_PRIVATE);
            outputStream.write(ndef_result.getBytes());
            outputStream.flush();
            outputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public void addIngredientToFile(Ingredient i){

        File file = new File(getFilesDir(),"ingredients.json");
        FileOutputStream outputStream;
        System.out.println(file.exists());
        try{
                if(!file.exists()){
                    outputStream = openFileOutput("ingredients.json", Context.MODE_PRIVATE);
                    outputStream.write("{}".getBytes());
                    outputStream.flush();
                    outputStream.close();
                }

                    FileInputStream fis = openFileInput("ingredients.json");
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader bufferedReader = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                    String myJson = sb.toString();
                    System.out.println(myJson);
                    JSONObject object = new JSONObject(myJson);
                    if (!object.has(i.upc)) {
                        object.put(i.upc, i.toJSON());
                        String newJSON = object.toString();
                        outputStream = openFileOutput("ingredients.json", Context.MODE_PRIVATE);
                        outputStream.write(newJSON.getBytes());
                        outputStream.flush();
                        outputStream.close();
                    }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }


    public void addItem(final int tab){


        AlertDialog.Builder builder = new AlertDialog.Builder(Main_Activity.this);
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.add_item_dialog,null);
        final Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        final NumberPicker picker = (NumberPicker) view.findViewById(R.id.number_picker);
        picker.setMinValue(0);
        picker.setMaxValue(20);
        spinner.setAdapter(getSpinnerAdapter());
        builder.setView(view).setPositiveButton("Add", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {
                if(picker.getValue() == 0) {
                    Ingredient i = (Ingredient) spinner.getSelectedItem();
                    i.quantity = 0;
                    sanityCheckGroceries(i);
                }
                else{
                    Ingredient i = (Ingredient) spinner.getSelectedItem();
                    i.quantity = picker.getValue();
                    sanityCheckInventory(i);
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

    private SpinnerAdapter getSpinnerAdapter(){
        JSONObject json = null;
        String jsonString;
        CustomSpinnerAdapter adapter = null;
        try {
            FileInputStream fis = openFileInput("ingredients.json");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            jsonString = sb.toString();
            json = new JSONObject(jsonString);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally{
            ArrayList<Ingredient> ingredients = new ArrayList<>();
            Iterator<?> keys = json.keys();

            while( keys.hasNext() ) {
                try {
                    String key = (String) keys.next();
                    if (json.get(key) instanceof JSONObject) {
                        JSONObject object = json.getJSONObject(key);
                        String short_name = null;
                        String long_name = null;
                        if(object.has("short_name"))short_name = object.getString("short_name");
                        if(object.has("long_name"))long_name = object.getString("long_name");
                        ingredients.add(new Ingredient(key,0,short_name,long_name));
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            adapter = new CustomSpinnerAdapter(this,ingredients);
        }

        return adapter;

    }

    public class CustomSpinnerAdapter extends ArrayAdapter<Ingredient>{

        private Main_Activity activity;
        private ArrayList<Ingredient> ingredients;
        LayoutInflater inflater;

        /*************  CustomAdapter Constructor *****************/
        public CustomSpinnerAdapter(Main_Activity activity, ArrayList<Ingredient> objects)
        {
            super(activity.getBaseContext(), R.layout.spinner_item, objects);

            /********** Take passed values **********/
            this.activity = activity;
            ingredients = objects;
            /***********  Layout inflator to call external xml layout () **********************/
            inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public View getDropDownView(int position, View convertView,ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        // This funtion called for each row ( Called data.size() times )
        public View getCustomView(int position, View convertView, ViewGroup parent) {

            /********** Inflate spinner_rows.xml file for each row ( Defined below ) ************/
            View row = inflater.inflate(R.layout.spinner_item, parent, false);
            Ingredient i = ingredients.get(position);
            /***** Get each Model object from Arraylist ********/
            TextView item = (TextView)row.findViewById(R.id.spinner_item);
            if(i.shortName != null)item.setText(i.shortName);
            else item.setText(i.longName);


            return row;
        }
    }

    protected void sanityCheckGroceries(Ingredient i){
        boolean add = true;
        for(Ingredient grocery : groceries){
            if(grocery.upc.equals(i.upc)){
                add = false;
            }
        }
        if(add) {
            for (Ingredient ing : inventory) {
                if (ing.upc.equals(i.upc)) {
                    inventory.remove(ing);
                    break;
                }
            }
            groceries.add(i);
            refreshAdapters();
        }
    }

    protected void sanityCheckInventory(Ingredient i){
        boolean add = true;
        Ingredient temp = null;
        for(Ingredient ing : inventory){
            if(ing.upc.equals(i.upc)){
                add = false;
                temp = ing;
            }
        }
        if(add) {
            for (Ingredient grocery : groceries) {
                if (grocery.upc.equals(i.upc)) {
                    groceries.remove(grocery);
                }
            }
            inventory.add(i);
            refreshAdapters();
        }
        else{
            temp.quantity = temp.quantity+i.quantity;
            refreshAdapters();
        }
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


        if(tag != null){
            SharedPreferences.Editor edit = getSharedPreferences("frigid", Context.MODE_PRIVATE).edit();
            edit.putString("nfc_tag",tag.toString());
            edit.commit();
        }

        if(tag != null) {
            System.out.println("Reading from tag");
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
        else{
            loadUI();
            try {
                String[] upcs = readFile(new File(getFilesDir(), "ndef_result.txt")).split("\\n");
                System.out.println("Reading from file");
                for (String upc_line : upcs) {
                    NFC_Utility.upcToIngredients task = nfc_util.new upcToIngredients(upc_line);
                    task.execute();

                }
            }
            catch (Exception e){e.printStackTrace();}
        }
    }

    protected String readFile(File file) throws IOException {
        StringBuilder fileContents = new StringBuilder((int)file.length());
        Scanner scanner = new Scanner(file);
        String lineSeparator = System.getProperty("line.separator");

        try {
            while(scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine() + lineSeparator);
            }
            return fileContents.toString();
        } finally {
            scanner.close();
        }
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
