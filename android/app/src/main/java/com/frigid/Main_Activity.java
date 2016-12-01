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
import android.provider.Settings;
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
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
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
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Main_Activity extends AppCompatActivity {

    static ArrayList<Ingredient> inventory = new ArrayList<>();
    static ArrayList<Ingredient> groceries = new ArrayList<>();
    ListView inventoryList;
    ListView groceriesList;
    static IngredientArrayAdapter inventoryAdapter;
    static IngredientArrayAdapter groceryListAdapter;
    private NfcAdapter mNfcAdapter;
    private NFC_Utility nfc_util;
    IngredientArrayAdapter mAdapter;
    ListView mListView;
    public static String ndefString = "";
    public static Ingredient ingForDetail = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfc_util = new NFC_Utility(getApplicationContext(),this);
        // create the TabHost that will contain the Tabs

        System.out.println("!@!@! Token is: " + FirebaseInstanceId.getInstance().getToken());

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
        inventoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ingForDetail = inventory.get(position);
                Intent i = new Intent(getApplicationContext(),Ingredient_Detail.class);
                startActivity(i);
            }
        });
        inventoryList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Main_Activity.this);
                final View layout = getLayoutInflater().inflate(R.layout.remove_item_inventory,null);
                final NumberPicker picker = (NumberPicker) layout.findViewById(R.id.number_picker);
                final Ingredient i = inventoryAdapter.getItem(position);
                picker.setMinValue(1);
                picker.setMaxValue(i.quantity);
                builder.setView(layout).setPositiveButton("Remove", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if(picker.getValue() == i.quantity) {
                            ingredientChange(i.upc,i.quantity*-1);
                            inventory.remove(position);
                            i.quantity = 0;
                            groceries.add(i);
                            refreshAdapters();
                        }
                        else{
                            i.quantity = i.quantity-picker.getValue();
                            ingredientChange(i.upc,picker.getValue()*-1);
                            refreshAdapters();
                        }
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

                return true;
            }
        });
        groceriesList = (ListView) findViewById(R.id.list2);
        groceriesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ingForDetail = groceries.get(position);
                Intent i = new Intent(getApplicationContext(),Ingredient_Detail.class);
                startActivity(i);
            }
        });
        groceriesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Main_Activity.this);
                builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Ingredient i = groceryListAdapter.getItem(position);
                        removeIngredient(i.upc);
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

                return true;
            }
        });

        onNewIntent(getIntent());
    }

    public void loadUI(){
        final ProgressBar waiting = (ProgressBar)findViewById(R.id.waiting);
        final TabHost tabHost = (TabHost)findViewById(R.id.tabhost);

        inventoryAdapter = new IngredientArrayAdapter(getApplicationContext(),this,inventory,R.layout.ingredient_row);
        groceryListAdapter = new IngredientArrayAdapter(getApplicationContext(),this,groceries,R.layout.ingredient_row_grocery);
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

    public static void refreshAdapters(){
        Collections.sort(inventory,new IngredientComparator());
        Collections.sort(groceries,new IngredientComparator());
        inventoryAdapter.notifyDataSetChanged();
        groceryListAdapter.notifyDataSetChanged();
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
        picker.setDisplayedValues(new String[]{"Grocery","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20"});
        spinner.setAdapter(getSpinnerAdapter());
        builder.setView(view).setPositiveButton("Add", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {
                if(spinner.getSelectedItem() != null) {
                    if (picker.getValue() == 0) {
                        Ingredient i = (Ingredient) spinner.getSelectedItem();
                        i.quantity = 0;
                        sanityCheckGroceries(i);
                    } else {
                        Ingredient i = (Ingredient) spinner.getSelectedItem();
                        i.quantity = picker.getValue();
                        sanityCheckInventory(i);
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

    private IngredientArrayAdapter getExpiringAdapter(JSONObject data){
        JSONObject json = null;
        JSONObject json2 = null;
        String jsonString;
        IngredientArrayAdapter adapter = null;
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

            isr = new InputStreamReader(getResources().openRawResource(R.raw.plu));
            bufferedReader = new BufferedReader(isr);
            sb = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            jsonString = sb.toString();
            json2 = new JSONObject(jsonString);
            System.out.println(json2.toString());
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally{
            ArrayList<Ingredient> ingredients = new ArrayList<>();
            Iterator<?> keys = data.keys();

            while( keys.hasNext() ) {
                try {
                    String key = (String) keys.next();
                    if (json.has(key)) {
                        JSONObject object1 = json.getJSONObject(key);
                        String short_name = null;
                        String long_name = null;
                        if(object1.has("short_name"))short_name = object1.getString("short_name");
                        if(object1.has("long_name"))long_name = object1.getString("long_name");
                        ingredients.add(new Ingredient(key,data.getInt(key),short_name,long_name));
                    }
                    else{
                        JSONObject object2 = json2.getJSONObject(key);
                        String short_name = null;
                        String long_name = null;
                        if(object2.has("short_name"))short_name = object2.getString("short_name");
                        if(object2.has("long_name"))long_name = object2.getString("long_name");
                        ingredients.add(new Ingredient(key,data.getInt(key),short_name,long_name));
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            Collections.sort(ingredients,new IngredientComparator());
            adapter = new IngredientArrayAdapter(getBaseContext(),this,ingredients,R.layout.expiring_row);
        }

        return adapter;

    }

    private SpinnerAdapter getSpinnerAdapter(){
        JSONObject json1 = null;
        JSONObject json2 = null;
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
            json1 = new JSONObject(jsonString);


             isr = new InputStreamReader(getResources().openRawResource(R.raw.plu));
             bufferedReader = new BufferedReader(isr);
             sb = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            jsonString = sb.toString();
            json2 = new JSONObject(jsonString);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally{
            ArrayList<Ingredient> ingredients = new ArrayList<>();
            ArrayList<Ingredient> ingredients_base = new ArrayList<>();
            Iterator<?> keys = json1.keys();

            while( keys.hasNext() ) {
                try {
                    String key = (String) keys.next();
                    if (json1.get(key) instanceof JSONObject) {
                        JSONObject object = json1.getJSONObject(key);
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
            keys = json2.keys();
            while( keys.hasNext() ) {
                try {
                    String key = (String) keys.next();
                    if (json2.get(key) instanceof JSONObject) {
                        JSONObject object = json2.getJSONObject(key);
                        String short_name = null;
                        String long_name = null;
                        if(object.has("short_name"))short_name = object.getString("short_name");
                        if(object.has("long_name"))long_name = object.getString("long_name");
                        ingredients_base.add(new Ingredient(key,0,short_name,long_name,true));
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            Collections.sort(ingredients,new IngredientComparator());
            Collections.sort(ingredients_base,new IngredientComparator());
            ingredients.addAll(ingredients_base);
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

    protected void addIngredient(String upc, int quantity){
        JSONObject json = null;
        try {
            FileInputStream fis = openFileInput("ingredients_change.json");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            String jsonString = sb.toString();
            fis.close();
            json = new JSONObject(jsonString);
            JSONObject addBlock = json.getJSONObject("ingredients").getJSONObject("add");
            addBlock.put(upc,quantity);
            JSONArray remove = json.getJSONObject("ingredients").getJSONArray("remove");
            for(int z = 0; z < remove.length(); z++) {
                if(remove.getString(z).equals(upc)){
                    remove.remove(z);
                    break;
                }
            }
            JSONObject changeBlock = json.getJSONObject("ingredients").getJSONObject("change");
            if(changeBlock.has(upc)){changeBlock.remove(upc);}
            FileOutputStream outputStream = openFileOutput("ingredients_change.json", Context.MODE_PRIVATE);
            outputStream.write(json.toString().getBytes());
            outputStream.flush();
            outputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    protected void ingredientChange(String upc, int quantity){
        JSONObject json = null;
        try {
            FileInputStream fis = openFileInput("ingredients_change.json");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            String jsonString = sb.toString();
            fis.close();
            json = new JSONObject(jsonString);
            JSONObject changeBlock = json.getJSONObject("ingredients").getJSONObject("change");
            if(changeBlock.has(upc)){
                int change = changeBlock.getInt(upc);
                changeBlock.put(upc,change + quantity);
            }
            else {
                changeBlock.put(upc, quantity);
            }
            FileOutputStream outputStream = openFileOutput("ingredients_change.json", Context.MODE_PRIVATE);
            outputStream.write(json.toString().getBytes());
            outputStream.flush();
            outputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }


    protected void removeIngredient(String upc){

        JSONObject json = null;
        try {
            FileInputStream fis = openFileInput("ingredients_change.json");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            String jsonString = sb.toString();
            fis.close();
            json = new JSONObject(jsonString);
            JSONArray removeBlock = json.getJSONObject("ingredients").getJSONArray("remove");
            removeBlock.put(upc);
            JSONObject change = json.getJSONObject("ingredients").getJSONObject("change");
            if(change.has(upc))change.remove(upc);
            FileOutputStream outputStream = openFileOutput("ingredients_change.json", Context.MODE_PRIVATE);
            outputStream.write(json.toString().getBytes());
            outputStream.flush();
            outputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
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
        addIngredient(i.upc,i.quantity);
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
                    break;
                }
            }
            inventory.add(i);
            boolean change = false;
            try{
                for(String line : readFile(new File(getFilesDir(), "ndef_result.txt")).split("\\n")) {
                    if(line.split(" ")[0].equals(i.upc))change = true;
                }
                    if(change) ingredientChange(i.upc,i.quantity);
                    else addIngredient(i.upc,i.quantity);
                    refreshAdapters();
            }
            catch(Exception e){}

        }
        else{
            temp.quantity = temp.quantity+i.quantity;
            ingredientChange(temp.upc,i.quantity);
            refreshAdapters();
        }
    }

    @Override
    public void onBackPressed() {

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
        if (id == R.id.other_fucking_app) {
            return true;
        }

        if(id == R.id.notifications){
            AlertDialog.Builder builder = new AlertDialog.Builder(Main_Activity.this);
            LayoutInflater inflater = getLayoutInflater();
            final View view = inflater.inflate(R.layout.notifications_dialog,null);
            final NumberPicker picker = (NumberPicker) view.findViewById(R.id.number_picker);
            picker.setMinValue(0);
            picker.setMaxValue(3);
            picker.setDisplayedValues(new String[]{"Never","1 Week", "2 Weeks", "3 Weeks"});
            builder.setView(view).setPositiveButton("Add", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int id) {
                     getSharedPreferences("frigid",Context.MODE_PRIVATE).edit().putInt("notifications",picker.getValue()).commit();
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setTitle(Html.fromHtml("<font color='#000000'>Notifications</font>"));
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
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
                    System.out.println("Try");
                    break;
                }
            }
        }
        else{
            loadUI();
            try {
                inventory.clear();
                groceries.clear();
                String ndef = updateNdef(readFile(new File(getFilesDir(), "ndef_result.txt")).trim());
                loadUI();
                System.out.println("Reading from file");
                for (String upc_line : ndef.split("\\n")) {
                    if(Integer.parseInt(upc_line.split(" ")[1].trim()) > -1) {
                        NFC_Utility.upcToIngredients task = nfc_util.new upcToIngredients(upc_line);
                        task.execute();
                    }

                }
                refreshAdapters();
            }
            catch (Exception e){e.printStackTrace();}
        }
        String json = null;
        if(intent.getExtras()!=null){
            if(intent.getExtras().getString("data")!=null) {
                try {
                    json = intent.getExtras().getString("data");
                    AlertDialog.Builder builder = new AlertDialog.Builder(Main_Activity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    final View view = inflater.inflate(R.layout.expring_dialog, null);
                    final ListView list = (ListView) view.findViewById(R.id.list1);
                    JSONObject jsonObject = new JSONObject(json);
                    IngredientArrayAdapter adapter = getExpiringAdapter(jsonObject);
                    list.setAdapter(adapter);
                    builder.setView(view).setPositiveButton("Okay", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.setTitle(Html.fromHtml("<font color='#000000'>Expiring Ingredients (Days Old)</font>"));
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    protected void writeTag(Tag tag,String ndef){
        if(nfc_util.writableTag(tag)) {
            ndef = String.format("%s %s %d\r\n",getString(R.string.FCM_TOKEN),FirebaseInstanceId.getInstance().getToken(),getSharedPreferences("frigid",Context.MODE_PRIVATE).getInt("notifications",0)) + ndef;
            NFC_Utility.WriteResponse wr = nfc_util.writeTag(nfc_util.getTagAsNdef(ndef),tag);
            String message = (wr.getStatus() == 1? "Success: " : "Failed: ") + wr.getMessage();
            Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
            try {
                FileOutputStream outputStream = openFileOutput("ingredients_change.json", Context.MODE_PRIVATE);
                outputStream.write("{\"ingredients\":{\"add\":{},\"change\":{},\"remove\":[]}}".getBytes());
                outputStream.flush();
                outputStream.close();
            }
            catch(Exception e){e.printStackTrace();}
        }
    }

    protected String updateNdef(String ndefResult){
        System.out.println(ndefResult);
        System.out.println("Made it here");
        JSONObject json = null;
        String ndefNew = "";
        String[] lines = ndefResult.split("\\n");
        String[] upcs = new String[lines.length];
        ArrayList<String> UPC = new ArrayList<>();
        ArrayList<Integer> Quantity = new ArrayList<>();
        int[] quantities = new int[lines.length];
        for (int i = 0; i<lines.length; i++) {
            upcs[i] = lines[i].split(" ")[0];
            quantities[i] = Integer.parseInt(lines[i].split(" ")[1].trim());
        }

        try {
            FileInputStream fis = openFileInput("ingredients_change.json");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            String jsonString = sb.toString();
            System.out.println(jsonString);
            json = new JSONObject(jsonString);
            if(json!=null){
                JSONObject add = json.getJSONObject("ingredients").getJSONObject("add");
                JSONArray remove = json.getJSONObject("ingredients").getJSONArray("remove");
                JSONObject change = json.getJSONObject("ingredients").getJSONObject("change");
                Iterator<?> keys = add.keys();
                while(keys.hasNext()){
                    String upc = (String) keys.next();
                    int quantity = add.getInt(upc);
                    boolean added = false;
                    for(int i = 0; i<lines.length; i++){
                        if(upcs[i].equals(upc)){
                            if(quantity == 0){
                                quantities[i] = 0;
                            }
                            else {
                                if(quantities[i] == -1) quantities[i] = quantities[i] + quantity + 1;
                                else quantities[i] = quantities[i] + quantity;
                            }
                            added = true;
                        }
                    }
                    if(!added){
                        UPC.add(upc);
                        Quantity.add(quantity);
                    }
                }
                for(int z = 0; z < remove.length(); z++) {
                        String upc = remove.getString(z);
                        for (int i = 0; i < lines.length; i++) {
                            if (upcs[i].equals(upc)) {
                                    quantities[i] = -1;
                            }
                        }
                    }
                keys = change.keys();
                while(keys.hasNext()){
                    String upc = (String) keys.next();
                    int quantity = change.getInt(upc);
                    boolean changed = false;
                    for(int i = 0; i<lines.length; i++){
                        if(upcs[i].equals(upc)){
                            if(quantities[i] == -1) quantities[i] = quantities[i] + quantity + 1;
                            else {quantities[i] = quantities[i] + quantity;
                           }
                            changed = true;
                            break;
                        }
                    }
                    if(!changed){
                        if(quantity >0) {
                            UPC.add(upc);
                            Quantity.add(quantity);
                        }
                    }
                }
            }
            for(int i = 0; i<lines.length; i++){
                    UPC.add(upcs[i]);
                    Quantity.add(quantities[i]);
            }
            Iterator<String> it1 = UPC.iterator();
            Iterator<Integer> it2 = Quantity.iterator();

            while (it1.hasNext() && it2.hasNext()) {
                String upc = it1.next();
                int quantity = it2.next();
                ndefNew += String.format("%s %d\r\n",upc,quantity);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return ndefNew;

    }

    protected String listsToNdef(){
        String s = "";
        for(Ingredient i: inventory){
            s += String.format("%s %d\r\n",i.upc,i.quantity);
        }
        for(Ingredient i: groceries){
            s += String.format("%s %d\r\n",i.upc,i.quantity);
        }
        return s;
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
