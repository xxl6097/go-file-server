package com.example.blue;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.example.bluetoothlibrary.entity.SycnData;

import java.util.ArrayList;

/**
 * Created by laiyiwen on 2017/5/12.
 */

public class DatalistAdapter extends BaseAdapter {
    private ArrayList<SycnData> mLeDevices;
    private LayoutInflater mInflator;
    private Activity mContext;



    public DatalistAdapter(Activity c ,ArrayList<SycnData> showDevice) {
        super();
        mContext = c;
        mLeDevices = showDevice;
        mInflator = mContext.getLayoutInflater();
    }
    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public SycnData getItem(int position) {
        return mLeDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        if (convertView==null){
            viewHolder=new ViewHolder();
            convertView=mInflator.inflate(R.layout.data_list,null);
            viewHolder.tempid= (TextView) convertView.findViewById(R.id.temp_id);
            viewHolder.temp=(TextView) convertView.findViewById(R.id.temp_sycn_value);
            convertView.setTag(viewHolder);
        }else
       {

           viewHolder = (ViewHolder)convertView .getTag();
       }

        SycnData sycnData=mLeDevices.get(position);
        viewHolder.temp.setText(sycnData.getTemp());
        viewHolder.tempid.setText(sycnData.getTempID());
        return convertView;
    }

    class ViewHolder {
        TextView tempid;
        TextView temp;
    }
}
