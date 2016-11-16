package com.frigid;

import android.view.View;

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
}
