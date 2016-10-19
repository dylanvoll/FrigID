package com.frigid;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
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

    final int TECH_NFC_A = 1;
    final String EXTRA_NFC_A_SAK = "sak";    // short (SAK byte value)
    final String EXTRA_NFC_A_ATQA = "atqa";  // byte[2] (ATQA value)

    final int TECH_NFC_B = 2;
    final String EXTRA_NFC_B_APPDATA = "appdata";    // byte[] (Application Data bytes from ATQB/SENSB_RES)
    final String EXTRA_NFC_B_PROTINFO = "protinfo";  // byte[] (Protocol Info bytes from ATQB/SENSB_RES)

    final int TECH_ISO_DEP = 3;
    final String EXTRA_ISO_DEP_HI_LAYER_RESP = "hiresp";  // byte[] (null for NfcA)
    final String EXTRA_ISO_DEP_HIST_BYTES = "histbytes";  // byte[] (null for NfcB)

    final int TECH_NFC_F = 4;
    final String EXTRA_NFC_F_SC = "systemcode";  // byte[] (system code)
    final String EXTRA_NFC_F_PMM = "pmm";        // byte[] (manufacturer bytes)

    final int TECH_NFC_V = 5;
    final String EXTRA_NFC_V_RESP_FLAGS = "respflags";  // byte (Response Flag)
    final String EXTRA_NFC_V_DSFID = "dsfid";           // byte (DSF ID)

    final int TECH_NDEF = 6;
    final String EXTRA_NDEF_MSG = "ndefmsg";              // NdefMessage (Parcelable)
    final String EXTRA_NDEF_MAXLENGTH = "ndefmaxlength";  // int (result for getMaxSize())
    final String EXTRA_NDEF_CARDSTATE = "ndefcardstate";  // int (1: read-only, 2: read/write, 3: unknown)
    final String EXTRA_NDEF_TYPE = "ndeftype";            // int (1: T1T, 2: T2T, 3: T3T, 4: T4T, 101: MF Classic, 102: ICODE)

    final int TECH_NDEF_FORMATABLE = 7;

    final int TECH_MIFARE_CLASSIC = 8;

    final int TECH_MIFARE_ULTRALIGHT = 9;
    final String EXTRA_MIFARE_ULTRALIGHT_IS_UL_C = "isulc";  // boolean (true: Ultralight C)

    final int TECH_NFC_BARCODE = 10;
    final String EXTRA_NFC_BARCODE_BARCODE_TYPE = "barcodetype";  // int (1: Kovio/ThinFilm)

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        setContentView(R.layout.activity_login);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mTextView = (TextView)findViewById(R.id.textView);
        button = (Button)findViewById(R.id.button);

        if (mNfcAdapter != null) {
            mTextView.setText("Read an NFC tag");
        } else {
            mTextView.setText("This phone is not NFC enabled.");
        }

        // create an intent with tag data and deliver to this activity
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // set an intent filter for all MIME data
        IntentFilter ndefIntent = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefIntent.addDataType("*/*");
            mIntentFilters = new IntentFilter[] { ndefIntent };
        } catch (Exception e) {
            Log.e("TagDispatch", e.toString());
        }

        mNFCTechLists = new String[][] { new String[] { NfcF.class.getName() } };

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Class tagClass = Tag.class;
                Method createMockTagMethod = null;
                try {
                    createMockTagMethod = tagClass.getMethod("createMockTag", byte[].class, int[].class, Bundle[].class);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }

                Bundle nfcaBundle = new Bundle();
                nfcaBundle.putByteArray(EXTRA_NFC_A_ATQA, new byte[]{ (byte)0x44, (byte)0x00 }); //ATQA for Type 2 tag
                nfcaBundle.putShort(EXTRA_NFC_A_SAK , (short)0x00); //SAK for Type 2 tag

                Bundle ndefBundle = new Bundle();
                ndefBundle.putInt(EXTRA_NDEF_MAXLENGTH, 48); // maximum message length: 48 bytes
                ndefBundle.putInt(EXTRA_NDEF_CARDSTATE, 1); // read-only
                ndefBundle.putInt(EXTRA_NDEF_TYPE, 2); // Type 2 tag
                NdefMessage myNdefMessage = new NdefMessage(createTextRecord("This is a message",Locale.ENGLISH,true));
                ndefBundle.putParcelable(EXTRA_NDEF_MSG, myNdefMessage);  // add an NDEF message
                byte[] tagId = new byte[] { (byte)0x3F, (byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78, (byte)0x90, (byte)0xAB };
                Tag mockTag = null;
                try {
                    mockTag = (Tag)createMockTagMethod.invoke(null,
                            tagId,                                     // tag UID/anti-collision identifier (see Tag.getId() method)
                            new int[] { TECH_NFC_A, TECH_NDEF },       // tech-list
                            new Bundle[] { nfcaBundle, ndefBundle });  // array of tech-extra bundles, each entry maps to an entry in the tech-list
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent("android.nfc.action.NDEF_DISCOVERED");
                intent.setType("text/plain");
                intent.putExtra(NfcAdapter.EXTRA_ID, tagId);
                intent.putExtra(NfcAdapter.EXTRA_TAG, mockTag);
                intent.putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, new NdefMessage[]{ myNdefMessage });
                startActivity(intent);

            }
        });
    }

    public NdefRecord createTextRecord(String payload, Locale locale, boolean encodeInUtf8) {
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = payload.getBytes(utfEncoding);
        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);
        return record;
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
