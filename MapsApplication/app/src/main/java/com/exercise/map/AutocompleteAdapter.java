package com.exercise.map;

import android.content.Context;
import android.graphics.Typeface;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.libraries.places.api.model.AutocompletePrediction;

import java.util.List;

/*Custom adapter class for AutocompleteTextView*/
public final class AutocompleteAdapter extends ArrayAdapter<AutocompletePrediction> {
    private Context mContext;
    private int mLayoutResourceId;
    private List<AutocompletePrediction> mList;
    private final StyleSpan STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    /*Constructor*/
    public AutocompleteAdapter(Context context, int resourceId, List list) {
        super(context, resourceId, list);
        this.mContext = context;
        this.mLayoutResourceId = resourceId;
        this.mList = list;
    }

    @Override
    public int getCount() {
        return this.mList.size();
    }

    @Override
    public AutocompletePrediction getItem(int position) {
        return mList.get(position);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View convertView = view;
        if (convertView == null) {
            LayoutInflater layoutInflater = ((MapsActivity) mContext).getLayoutInflater();
            convertView = layoutInflater.inflate(this.mLayoutResourceId, parent, false);
        }

        if (position < mList.size()) {
            AutocompletePrediction item = (AutocompletePrediction) getItem(position);
            TextView textview = (TextView) convertView.findViewById(android.R.id.text1);
            textview.setText((item.getFullText(STYLE_BOLD)));
        }

        return convertView;
    }

}
