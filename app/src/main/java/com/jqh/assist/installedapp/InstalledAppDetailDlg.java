package com.jqh.assist.installedapp;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;


import com.jqh.assist.R;

import java.lang.reflect.Method;

/**
 * Created by alexlily on 2017/10/1.
 */

public class InstalledAppDetailDlg extends Dialog {
    private static String TAG = "InstalledAppDetailDlg";

    InstalledAppInfo installedAppInfo;

    //全局变量，保存当前查询包得信息
    private long cachesize ; //缓存大小
    private long datasize  ;  //数据大小
    private long codesize  ;  //应用程序大小
    private long totalsize ; //总大小

    public InstalledAppDetailDlg(@NonNull Context context) {
        super(context);
    }

    public InstalledAppDetailDlg(InstalledAppInfo appInfo, @NonNull Context context) {
        super(context);
        installedAppInfo = appInfo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_installed_app_detail);

        ImageView imgIcon = (ImageView) findViewById(R.id.imgAppIcon);
        imgIcon.setImageDrawable(installedAppInfo.getAppIcon());

        TextView tvAppName = (TextView) findViewById(R.id.tvAppLabel);
        tvAppName.setText(installedAppInfo.getAppLabel());

        TextView tvVersionName = (TextView) findViewById(R.id.tvVersionName);
        tvVersionName.setText(installedAppInfo.getVersionName());

        TextView tvPkgName = (TextView) findViewById(R.id.tvPkgName);
        tvPkgName.setText(installedAppInfo.getPkgName());

        TextView tvPkgPath = (TextView) findViewById(R.id.tvPkgPath);
        tvPkgPath.setText(installedAppInfo.getPkgPath());

        this.setTitle(installedAppInfo.getAppLabel());

        tvAppName.setTextColor(Color.GRAY);
        int type = installedAppInfo.getAppType();
        if (type == InstalledAppListActivity.APP_TYPE_SYSTEM || type == InstalledAppListActivity.APP_TYPE_SYSUPG)
            tvAppName.setTextColor(Color.rgb(0, 128, 0));
        else
            tvVersionName.setTextColor(Color.BLUE);

        //

        //更新显示当前包得大小信息
        queryPacakgeSize(installedAppInfo.getPkgName());

        TextView tvPkgSize = (TextView) findViewById(R.id.tvPkgSize);
        tvPkgSize.setText(formateFileSize(codesize));

        TextView tvDataSize = (TextView) findViewById(R.id.tvDataSize);
        tvDataSize.setText(formateFileSize(datasize));

        TextView tvCacheSize = (TextView) findViewById(R.id.tvCacheSize);
        tvCacheSize.setText(formateFileSize(cachesize));
    }

    //系统函数，字符串转换 long -String (kb)
    private String formateFileSize(long size){
        return Formatter.formatFileSize(this.getContext(), size);
    }

    public void  queryPacakgeSize(String pkgName){
        if (pkgName != null) {
            //使用放射机制得到PackageManager类的隐藏函数getPackageSizeInfo
            PackageManager pm = this.getContext().getPackageManager();  //得到pm对象
            try {
                Log.i(TAG, "queryPackageSize");
                //通过反射机制获得该隐藏函数
                Method getPackageSizeInfo = pm.getClass().getDeclaredMethod(
                        "getPackageSizeInfo", String.class, IPackageStatsObserver.class);

                //调用该函数，并且给其分配参数 ，待调用流程完成后会回调PkgSizeObserver类的函数
                getPackageSizeInfo.invoke(pm, pkgName, new PkgSizeObserver());

            } catch(Exception ex) {
                Log.e(TAG, "NoSuchMethodException") ;
                ex.printStackTrace() ;
            }
        }
    }

    //aidl文件形成的Bindler机制服务类
    public class PkgSizeObserver extends IPackageStatsObserver.Stub {
        /*** 回调函数，
         * @param pStats ,返回数据封装在PackageStats对象中
         * @param succeeded  代表回调成功
         */
        @Override
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
                throws RemoteException {
            // TODO Auto-generated method stub
            cachesize = pStats.cacheSize  ; //缓存大小
            datasize = pStats.dataSize  ;  //数据大小
            codesize = pStats.codeSize  ;  //应用程序大小
            totalsize = cachesize + datasize + codesize ;
            Log.i(TAG, "cachesize="+cachesize+" datasize="+datasize+ " codeSize="+codesize);
        }
    }
}
