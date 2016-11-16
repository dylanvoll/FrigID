package com.frigid;

import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Dylan on 10/27/2016.
 */

public class NFC_Utility {

    public final Context context;
    Main_Activity activity;

    public NFC_Utility(Context context, Main_Activity activity){
        this.context = context;
        this.activity = activity;
    }

    class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        Tag tag;

        @Override
        protected String doInBackground(Tag... params) {
            tag = params[0];

            Ndef ndef = Ndef.get(tag);
            String ndefString = null;
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);

                    } catch (UnsupportedEncodingException e) {

                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException, UnsupportedEncodingException {

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            Main_Activity.ndefString = result;
            new upcToIngredients(result).execute();
        }
    }

    class upcToIngredients extends AsyncTask<String, Void, String> {

        String ndefString;
        ArrayList<Ingredient> inventory = new ArrayList<>();
        ArrayList<Ingredient> groceries = new ArrayList<>();
        UPC_Api_Utility api = new UPC_Api_Utility(context);

        public upcToIngredients(String ndefString){
            this.ndefString = ndefString;
        }

        @Override
        protected String doInBackground(String... params) {

            System.out.println("Starting network");

            String[] upcs = ndefString.split("\\n");
            for(String upcLine : upcs){
                String upc = upcLine.split(" ")[0];
                int quantity = Integer.parseInt(upcLine.split(" ")[1]);
                String longName = api.getNameFromUpc(upc);
                if(quantity == 0){
                    groceries.add(new Ingredient(upc,quantity,null,longName));
                }
                else{
                    inventory.add(new Ingredient(upc,quantity,null,longName));
                }
            }

            return "Finished";
        }


        @Override
        protected void onPostExecute(String result) {
            activity.inventory = inventory;
            activity.groceries = groceries;
            activity.loadUI();
            System.out.println("Loading UI");

        }
    }

    public WriteResponse writeTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;
        String mess = "";
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    return new WriteResponse(0,"Tag is read-only");
                }
                if (ndef.getMaxSize() < size) {
                    mess = "Tag capacity is " + ndef.getMaxSize() + " bytes, message is " + size
                            + " bytes.";
                    return new WriteResponse(0,mess);
                }
                ndef.writeNdefMessage(message);
                mess = "Wrote message to pre-formatted tag.";
                return new WriteResponse(1,mess);
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        mess = "Formatted tag and wrote message";
                        return new WriteResponse(1,mess);
                    } catch (IOException e) {
                        mess = "Failed to format tag.";
                        return new WriteResponse(0,mess);
                    }
                } else {
                    mess = "Tag doesn't support NDEF.";
                    return new WriteResponse(0,mess);
                }
            }
        } catch (Exception e) {
            mess = "Failed to write tag";
            return new WriteResponse(0,mess);
        }
    }
    public class WriteResponse {
        int status;
        String message;
        WriteResponse(int Status, String Message) {
            this.status = Status;
            this.message = Message;
        }
        public int getStatus() {
            return status;
        }
        public String getMessage() {
            return message;
        }
    }

    protected boolean writableTag(Tag tag) {
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    Toast.makeText(context,"Tag is read-only.",Toast.LENGTH_SHORT).show();
                    ndef.close();
                    return false;
                }
                ndef.close();
                return true;
            }
        } catch (Exception e) {
            Toast.makeText(context,"Failed to read tag",Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    protected NdefMessage getTagAsNdef(String payloadMessage) {

        NdefRecord record = null;
        try {
            String lang = "en";
            byte[] textBytes = payloadMessage.getBytes();
            byte[] langBytes = lang.getBytes("US-ASCII");
            int langLength = langBytes.length;
            int textLength = textBytes.length;
            byte[] payload = new byte[1 + langLength + textLength];

            // set status byte (see NDEF spec for actual bits)
            payload[0] = (byte) langLength;

            // copy langbytes and textbytes into payload
            System.arraycopy(langBytes, 0, payload, 1, langLength);
            System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);
            record = new NdefRecord(
                    NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
        /*if(addAAR) {
            // note: returns AAR for different app (nfcreadtag)
            return new NdefMessage(new NdefRecord[] {
                    rtdUriRecord, NdefRecord.createApplicationRecord("com.frigid")
            });
        } else {
            return new NdefMessage(new NdefRecord[] {
                    rtdUriRecord});
        }*/
        }
        catch (Exception e){
            e.printStackTrace();
        }
        System.out.println(new String(record.getPayload()));
        return new NdefMessage(record);
    }

    public void ndefToFile(String ndefString){
        try {
            File file = new File(context.getExternalCacheDir().getPath()+"/frigid.txt");
            file.createNewFile(); // if file already exists will do nothing
            FileOutputStream oFile = new FileOutputStream(file, false);
            PrintWriter out = new PrintWriter(file);
            out.println(ndefString);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
