package com.frigid;

import android.view.View;

import org.json.JSONObject;

import java.util.Comparator;

/**
 * Created by Dylan on 11/14/2016.
 */

public class Ingredient {
    public String upc;
    public String longName;
    public String shortName;
    public int quantity;
    public boolean isPlu = false;
    IngredientArrayAdapter.ViewHolder view = null;

    public Ingredient(String upc,int quantity){
        this.upc = upc;
        this.quantity = quantity;
        this.longName = null;
        this.shortName = null;
    }

    public Ingredient(String upc,int quantity, String shortName){
        this.upc = upc;
        this.quantity = quantity;
        this.longName = null;
        this.shortName = shortName;
    }

    public Ingredient(String upc,int quantity, String shortName, String longName){
        this.upc = upc;
        this.quantity = quantity;
        this.longName = longName;
        this.shortName = shortName;
    }

    public Ingredient(String upc,int quantity, String shortName, String longName, boolean plu){
        this.upc = upc;
        this.quantity = quantity;
        this.longName = longName;
        this.shortName = shortName;
        this.isPlu = plu;
    }

    public Ingredient(String upc, int quantity, JSONObject object){
        this.upc = upc;
        this.quantity = quantity;
        this.longName = null;
        this.shortName = null;
        try {
            if(object.has("long_name")) this.longName = object.getString("long_name");
            if(object.has("short_name")) this.shortName = object.getString("short_name");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public Ingredient(String upc, int quantity, JSONObject object, boolean plu){
        this.upc = upc;
        this.quantity = quantity;
        this.longName = null;
        this.shortName = null;
        this.isPlu = plu;
        try {
            if(object.has("long_name")) this.longName = object.getString("long_name");
            if(object.has("short_name")) this.shortName = object.getString("short_name");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public JSONObject toJSON(){
        JSONObject object = new JSONObject();
        try {
            object.put("quantity", quantity);
            object.put("short_name",shortName);
            object.put("long_name",longName);

        }
        catch (Exception e){
            e.printStackTrace();
        }
        return object;
    }
}

class IngredientComparator implements Comparator<Ingredient>{
    @Override
    public int compare(Ingredient i1, Ingredient i2) {
        String one = i1.longName;
        String two = i2.longName;
        if(i1.shortName!=null)one = i1.shortName;
        if(i2.shortName!=null)two = i2.shortName;
        return one.compareTo(two);
    }
}
