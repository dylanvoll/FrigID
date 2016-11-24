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
            SharedPreferences.Editor edit = sharedPref.edit();


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


    @Override
    public void onResume() {
        super.onResume();

        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mIntentFilters, mNFCTechLists);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }
}
