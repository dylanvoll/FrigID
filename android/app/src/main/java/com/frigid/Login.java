package com.frigid;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcF;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

public class Login extends AppCompatActivity {
    private TextView mTextView;
    private Button button;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mIntentFilters;
    private String[][] mNFCTechLists;


    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        SharedPreferences sharedPref = getSharedPreferences("frigid", Context.MODE_PRIVATE);
        if(sharedPref.getString("nfc_tag", null) != null){
            Intent intent = new Intent(this,Main_Activity.class);
            startActivity(intent);
        }
        else {
            setContentView(R.layout.activity_login);
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
            mTextView = (TextView) findViewById(R.id.textView);

            if (mNfcAdapter != null) {
                mTextView.setText("Read an NFC tag");
            } else {
                mTextView.setText("This phone is not NFC enabled.");
            }

            // create an intent with tag data and deliver to this activity
            mPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

            // set an intent filter for all MIME data
            IntentFilter ndefIntent = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
            try {
                ndefIntent.addDataType("text/plain");
                mIntentFilters = new IntentFilter[]{ndefIntent};
            } catch (Exception e) {
                Log.e("TagDispatch", e.toString());
            }

            mNFCTechLists = new String[][]{new String[]{NfcF.class.getName()}};
            try {
                File ndef = new File(getFilesDir(), "ndef_result.txt");
                ndef.createNewFile();
                File jsonChange = new File(getFilesDir(), "ingredients_change.json");
                if(!jsonChange.exists()) {
                    jsonChange.createNewFile();
                    FileOutputStream outputStream = openFileOutput("ingredients_change.json", Context.MODE_PRIVATE);
                    outputStream.write("{\"ingredients\":{\"add\":{},\"change\":{},\"remove\":[]}}".getBytes());
                    outputStream.flush();
                    outputStream.close();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }


    }


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
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);


        if(tag != null){
            Intent i = new Intent(this,Main_Activity.class);
            i.putExtras(intent);
            startActivity(i);
        }

    }
}
