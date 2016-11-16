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

    public IngredientArrayAdapter(Context context, AppCompatActivity activity, List<Ingredient> objects) {
        super(context, R.layout.ingredient_row, objects);
        this.context = context;
        this.activity = activity;
    }

    static class ViewHolder {
        TextView name;
        TextView quantity;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        final ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.ingredient_row, parent, false);
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
            holder.name.setText(ingredient.longName);
        }
        holder.quantity.setText("x" + ingredient.quantity);

        notifyDataSetChanged();

        return convertView;

    }
}
