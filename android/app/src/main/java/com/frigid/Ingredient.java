package com.frigid;

import android.view.View;

import org.json.JSONObject;

/**
 * Created by Dylan on 11/14/2016.
 */

public class Ingredient {
    public String upc;
    public String longName;
    public String shortName;
    public int quantity;
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
