package com.shanvi.android.shanvi;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.shanvi.android.shanvi.tools.BaseUserActivity;

import java.util.ArrayList;

/**
 * Created by Debashis on 3/18/2018.
 */

public class TableAdapter extends ArrayAdapter<TableRow> implements View.OnClickListener {
    private static final String TAG = TableAdapter.class.getSimpleName();

    private ArrayList<TableRow> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        TextView txtType;
        TextView txtVersion;
        TextView heading;
        ImageView info;
    }

    public TableAdapter(ArrayList<TableRow> data, Context context) {
        super(context, R.layout.table_row_item, data);
        this.dataSet = data;
        this.mContext=context;

    }

    @Override
    public void onClick(View v) {

        int position=(Integer) v.getTag();
        Object object= getItem(position);
        TableRow dataModel=(TableRow)object;

        Log.d(TAG, " ID : " + v.getId() + " item_info:" + R.id.item_info);
        switch (v.getId())
        {
            case R.id.item_info:
                ((BaseUserActivity) mContext).onClick(dataModel, this);
                break;
        }
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        TableRow dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.table_row_item, parent, false);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.name);
            viewHolder.txtType = (TextView) convertView.findViewById(R.id.type);
            viewHolder.txtVersion = (TextView) convertView.findViewById(R.id.number);
            viewHolder.heading = (TextView) convertView.findViewById(R.id.heading);
            viewHolder.info = (ImageView) convertView.findViewById(R.id.item_info);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;

        viewHolder.txtName.setText(dataModel.name);
        viewHolder.txtType.setText(dataModel.type);
        viewHolder.txtVersion.setText(dataModel.number);
        viewHolder.heading.setText(dataModel.feature);
        //viewHolder.info.setOnClickListener(this);
        viewHolder.info.setTag(position);
        // Return the completed view to render on screen
        return convertView;
    }
}