package com.jqh.assist.installedapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;

/**
 * Created by alexlily on 2017/10/1.
 */

public class InstalledAppInfo {
    private int appType;        //
    private String appLabel;    //应用程序标签
    private Drawable appIcon ;  //应用程序图像
    private Intent intent ;     //启动应用程序的Intent ，一般是Action为Main和Category为Lancher的Activity
    private String pkgName ;    //应用程序所对应的包名
    private String pkgPath;
    private String versionName;

    public InstalledAppInfo(){}

    public int getAppType() {
        return appType;
    }
    public void setAppType(int appType) {
        this.appType = appType;
    }
    public String getAppLabel() {
        return appLabel;
    }
    public void setAppLabel(String appName) {
        this.appLabel = appName;
    }
    public Drawable getAppIcon() {
        return appIcon;
    }
    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }
    public Intent getIntent() {
        return intent;
    }
    public void setIntent(Intent intent) {
        this.intent = intent;
    }
    public String getPkgName(){
        return pkgName ;
    }
    public void setPkgName(String pkgName){
        this.pkgName=pkgName ;
    }
    public String getPkgPath() {
        return pkgPath;
    }
    public void setPkgPath(String pkgPath) {
        this.pkgPath = pkgPath;
    }
    public String getVersionName() {
        return versionName;
    }
    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
}
