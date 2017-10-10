package com.jqh.assist;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jqh.assist.ApkListActivity.ApkInfo;
import java.util.List;


/**
 * Created by alexlily on 2017/10/2.
 */

public class ApkListAdapter extends BaseAdapter {
    private List<ApkInfo> mlistApkInfo = null;

    LayoutInflater infater = null;

    public ApkListAdapter(Context context, List<ApkInfo> apps) {
        infater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mlistApkInfo = apps ;
    }

    public void setDataSet(List<ApkInfo> apps) {
        mlistApkInfo = apps;
    }

    @Override
    public int getCount() {
        return mlistApkInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return mlistApkInfo.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertview, ViewGroup viewGroup) {
        View view = null;
        ViewHolder holder = null;
        if (convertview == null || convertview.getTag() == null) {
            view = infater.inflate(R.layout.activity_apk_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        else{
            view = convertview ;
            holder = (ViewHolder) convertview.getTag() ;
        }
        ApkInfo appInfo = (ApkInfo) getItem(position);
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
            this.appIcon = (ImageView) view.findViewById(R.id.imgApp);
            this.tvAppLabel = (TextView) view.findViewById(R.id.tvAppLabel);
            this.tvPkgName = (TextView) view.findViewById(R.id.tvPkgName);
            this.tvPkgPath = (TextView) view.findViewById(R.id.tvPkgPath);
            this.tvVersionName = (TextView) view.findViewById(R.id.tvVersionName);
            this.tvPkgStatus = (TextView) view.findViewById(R.id.tvPkgStatus);
        }
        public void setItemInfo(ApkInfo apkInfo) {
            this.appIcon.setImageDrawable(apkInfo.appIcon);
            this.tvAppLabel.setText(apkInfo.appLabel);
            this.tvPkgName.setText(apkInfo.pkgName);
            this.tvPkgPath.setText(apkInfo.pkgPath);
            this.tvVersionName.setText(apkInfo.versionName);

            this.tvPkgStatus.setTextColor(Color.GRAY);
            String apkStatus = "";
            switch (apkInfo.status) {
            case ApkListActivity.APK_NEWVERSION:    apkStatus = "新版本";
                this.tvPkgStatus.setTextColor(Color.rgb(0, 128, 0)); //浅绿色
                break;
            case ApkListActivity.APK_OLDVERSION:    apkStatus = "旧版本";
                break;
            case ApkListActivity.APK_INSTALLED:     apkStatus = "已安装";
                break;
            case ApkListActivity.APK_UNINSTALLED:   apkStatus = "未安装";
                this.tvPkgStatus.setTextColor(Color.BLUE);
                break;
            case ApkListActivity.APK_SIGNERROR:     apkStatus = "签名错误";
                break;
            case ApkListActivity.APK_DUPINSTALLED:  apkStatus = "重复已安装";
                break;
            }
            this.tvPkgStatus.setText(apkStatus);
        }
    }

}
