package com.example.blue;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.example.bluetoothlibrary.entity.SycnBp;

import java.util.ArrayList;

/**
 * Created by laiyiwen on 2017/5/19.
 */

public class WbpDatalistAdapter extends BaseAdapter {
    private ArrayList<SycnBp> mLeDevices;
    private LayoutInflater mInflator;
    private Activity mContext;



    public WbpDatalistAdapter(Activity c , ArrayList<SycnBp> showDevice) {
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
    public SycnBp getItem(int position) {
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
            convertView=mInflator.inflate(R.layout.wbp_sycn,null);
            viewHolder.sys_sy= (TextView) convertView.findViewById(R.id.sys_sycn);
            viewHolder.dia_sy=(TextView) convertView.findViewById(R.id.dia_sycn);
            viewHolder.hr_sy= (TextView) convertView.findViewById(R.id.hr_sycn);
            viewHolder.time_sy=(TextView) convertView.findViewById(R.id.tiem_sycn_wbp);

            convertView.setTag(viewHolder);
        }else
        {

            viewHolder = (ViewHolder)convertView .getTag();
        }

        SycnBp sycnData=mLeDevices.get(position);
        viewHolder.sys_sy.setText(""+sycnData.getSys());
        viewHolder.dia_sy.setText(""+sycnData.getDia());
        viewHolder.hr_sy.setText(""+sycnData.getHr());
        viewHolder.time_sy.setText(""+sycnData.getTime());
        return convertView;
    }

    class ViewHolder {
        TextView sys_sy;
        TextView dia_sy;
        TextView hr_sy;
        TextView time_sy;
    }
}
