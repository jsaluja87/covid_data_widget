package com.saluja_apps.covid_widget.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.saluja_apps.covid_widget.R;
import com.saluja_apps.covid_widget.model.ConfigListViewItem;

import java.util.ArrayList;

public class CustomListViewAdapter extends ArrayAdapter<ConfigListViewItem> implements View.OnClickListener{
    ArrayList<ConfigListViewItem> configListViewItemArray;
    int lastPosition;
    Context mContext;
    public CustomListViewAdapter(ArrayList<ConfigListViewItem> data, Context context) {
        super(context, R.layout.list_row_content, data);
        this.mContext=context;
        configListViewItemArray = data;
        lastPosition = configListViewItemArray.size() - 1;
    }

    @Override
    public void onClick(View view) {
        int clickedPosition = (Integer)view.getTag();
        ConfigListViewItem configListViewItem = getItem(clickedPosition);
        toggleCheckedItemState(configListViewItem);
    }

    private void toggleCheckedItemState(ConfigListViewItem item) {
        item.setStateClickedState(!item.isStateClickedState());
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ConfigListViewItem configListViewItem = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        final View result;
        final int pos = position;

        TextView listviewItemText;
        final CheckBox listviewItemClickedState;
        if (convertView == null) {

            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_row_content, parent, false);

        }
        listviewItemText = (TextView) convertView.findViewById(R.id.config_list_view_text_id);
        listviewItemClickedState = (CheckBox)convertView.findViewById(R.id.config_list_view_check_box_id);

        result=convertView;
        //text clickable
        listviewItemText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listviewItemClickedState.setChecked(!configListViewItemArray.get(pos).isStateClickedState());
                CovidWidgetConfig.updateListViewItemState(!configListViewItemArray.get(pos).isStateClickedState(), pos);
            }
        });
        listviewItemClickedState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listviewItemClickedState.setChecked(!configListViewItemArray.get(pos).isStateClickedState());
                CovidWidgetConfig.updateListViewItemState(!configListViewItemArray.get(pos).isStateClickedState(), pos);
            }
        });

        //Swipe up and down animation
        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.slide_up : R.anim.slide_down);
        result.startAnimation(animation);
        lastPosition = position;

        listviewItemText.setText(configListViewItemArray.get(position).getStateName());
        listviewItemClickedState.setChecked(configListViewItemArray.get(position).isStateClickedState());


        // Return the completed view to render on screen
        return convertView;
    }


}