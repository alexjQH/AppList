package com.jqh.assist.installedapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jqh.assist.R;

import java.util.List;

/**
 * Created by alexlily on 2017/10/1.
 */

//自定义适配器类，提供给listView的自定义view
public class InstalledAppListAdapter extends BaseAdapter {

    private List<InstalledAppInfo> mlistAppInfo = null;

    LayoutInflater infater = null;

    public InstalledAppListAdapter(Context context, List<InstalledAppInfo> apps) {
        infater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mlistAppInfo = apps;
    }
    public void setDataSet(List<InstalledAppInfo> apps) {
        mlistAppInfo = apps;
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        System.out.println("size" + mlistAppInfo.size());
        return mlistAppInfo.size();
    }
    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mlistAppInfo.get(position);
    }
    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public View getView(int position, View convertview, ViewGroup arg2) {
        System.out.println("getView at " + position);
        View view = null;
        ViewHolder holder = null;
        if (convertview == null || convertview.getTag() == null) {
            view = infater.inflate(R.layout.activity_installed_app_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        else{
            view = convertview ;
            holder = (ViewHolder) convertview.getTag() ;
        }
        InstalledAppInfo appInfo = (InstalledAppInfo) getItem(position);
        holder.setItemInfo(appInfo);
        return view;
    }

    class ViewHolder {
        ImageView appIcon;
        TextView tvAppLabel;
        TextView tvPkgName;
        TextView tvPkgPath;
        TextView tvVersionName;
        TextView tvPkgStatus;

        public ViewHolder(View view) {
            this.appIcon = (ImageView) view.findViewById(R.id.imgAppIcon);
            this.tvAppLabel = (TextView) view.findViewById(R.id.tvAppLabel);
            this.tvPkgName = (TextView) view.findViewById(R.id.tvPkgName);
            this.tvPkgPath = (TextView) view.findViewById(R.id.tvPkgPath);
            this.tvVersionName = (TextView) view.findViewById(R.id.tvVersionName);
            this.tvPkgStatus = (TextView) view.findViewById(R.id.tvPkgStatus);
        }
        public void setItemInfo(InstalledAppInfo appInfo) {
            this.appIcon.setImageDrawable(appInfo.getAppIcon());
            this.tvAppLabel.setText(appInfo.getAppLabel());
            this.tvPkgName.setText(appInfo.getPkgName());
            this.tvPkgPath.setText(appInfo.getPkgPath());
            this.tvVersionName.setText(appInfo.getVersionName());
            this.tvVersionName.setTextColor(Color.GRAY);

            String appType = "";
            switch(appInfo.getAppType()) {
                case InstalledAppListActivity.APP_TYPE_SYSTEM:
                    appType = "系统";
                    break;
                case InstalledAppListActivity.APP_TYPE_SYSUPG:
                    appType = "系统（升级）";
                    break;
                case InstalledAppListActivity.APP_TYPE_THIRD:
                    this.tvVersionName.setTextColor(Color.BLUE);
                    appType = "";
                    break;
                case InstalledAppListActivity.APP_TYPE_SDCARD:
                    this.tvVersionName.setTextColor(Color.BLUE);
                    appType = "SD卡";
                    break;
                default:
                    break;
            }
            this.tvPkgStatus.setText(appType);

        }
    }
}
