package com.frigid;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Dylan on 11/12/2016.
 */

public class IngredientArrayAdapter extends ArrayAdapter<Ingredient> {

    Context context;
    AppCompatActivity activity;
    int layout;

    public IngredientArrayAdapter(Context context, AppCompatActivity activity, List<Ingredient> objects, int layout) {
        super(context, R.layout.ingredient_row, objects);
        this.context = context;
        this.activity = activity;
        this.layout = layout;
    }

    static class ViewHolder {
        TextView name;
        TextView quantity;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        final ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(layout, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.quantity = (TextView) convertView.findViewById(R.id.quantity);
            convertView.setTag(holder);
            getItem(position).view = holder;
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Ingredient ingredient = getItem(position);
        if(ingredient.shortName != null) {
            holder.name.setText(ingredient.shortName);
        }
        else{
            if(ingredient.longName != null) holder.name.setText(ingredient.longName);
        }
        if(layout == R.layout.ingredient_row) {
            holder.quantity.setText("x" + ingredient.quantity);
        }
        else if(layout == R.layout.expiring_row){
            holder.quantity.setText(ingredient.quantity + "");
        }
        else{
            //nothing
        }

        notifyDataSetChanged();

        return convertView;

    }
}
