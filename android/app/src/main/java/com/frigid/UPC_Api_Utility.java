package com.frigid;

import android.content.Context;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by Dylan on 11/12/2016.
 */

public class UPC_Api_Utility {

    Context context;

    String base_url;

    public UPC_Api_Utility(Context context){
        this.context = context;
        base_url = String.format("http://eandata.com/feed/?v=3&keycode=%s&mode=json&find=",context.getResources().getString(R.string.upc_api_token).toString());
    }
    protected String getRequest(String upc) throws IOException {
        InputStream is = null;
        int len = 100000;
        int response = -1;
        String responseString = null;

        try {
            URL url = new URL(base_url + upc);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(25000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            response = conn.getResponseCode();
            is = conn.getInputStream();

            // Convert the InputStream into a string
            responseString = readIt(is,len);

        }
        catch (ProtocolException e) {
            e.printStackTrace();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (is != null) {
                is.close();
            }
        }
        if(response==200)return responseString;
        else return null;
    }

    public String getNameFromUpc(String upc){
        String longName = null;
        try {
            String response = getRequest(upc);
            if(response!= null){
                JSONObject json = new JSONObject(response);
                longName = json.getJSONObject("product").getJSONObject("attributes").getString("product");
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
        return longName;
    }

    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        String line;
        while((line = reader.readLine()) != null){
            builder.append(line);
        }

        return builder.toString();
    }
}
