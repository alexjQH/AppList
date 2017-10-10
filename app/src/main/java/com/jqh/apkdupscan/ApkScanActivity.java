package com.jqh.apkdupscan;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.jqh.assist.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApkScanActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView listview = null;
    private List<AppInfo> mlistAppInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apk_scan);

        listview = (ListView) findViewById(R.id.listviewApp);
        mlistAppInfo = new ArrayList<AppInfo>();

        queryAppInfo(); // 查询所有应用程序信息

        BrowseApplicationInfoAdapter browseAppAdapter = new BrowseApplicationInfoAdapter(this, mlistAppInfo);

        listview.setAdapter(browseAppAdapter);
        listview.setOnItemClickListener(this);
    }

    // 点击跳转至该应用程序
    public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
        Intent intent = mlistAppInfo.get(position).getIntent();
        startActivity(intent);
    }

    // 获得所有启动Activity的信息，类似于Launch界面
    public void queryAppInfo() {
        PackageManager pm = this.getPackageManager(); // 获得PackageManager对象
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        // 通过查询，获得所有ResolveInfo对象.
        List<ResolveInfo> resolveInfos = pm
                .queryIntentActivities(mainIntent, PackageManager.MATCH_DEFAULT_ONLY);

        // 调用系统排序 ， 根据name排序
        // 该排序很重要，否则只能显示系统应用，而不能列出第三方应用程序
        Collections.sort(resolveInfos,new ResolveInfo.DisplayNameComparator(pm));

        if (mlistAppInfo != null) {
            mlistAppInfo.clear();
            for (ResolveInfo reInfo : resolveInfos) {
                String activityName = reInfo.activityInfo.name; // 获得该应用程序的启动Activity的name
                String pkgName = reInfo.activityInfo.packageName; // 获得应用程序的包名
                String appLabel = (String) reInfo.loadLabel(pm); // 获得应用程序的Label
                Drawable icon = reInfo.loadIcon(pm); // 获得应用程序图标

                // 为应用程序的启动Activity 准备Intent
                Intent launchIntent = new Intent();
                launchIntent.setComponent(new ComponentName(pkgName, activityName));

                // 创建一个AppInfo对象，并赋值
                AppInfo appInfo = new AppInfo();
                appInfo.setAppLabel(appLabel);
                appInfo.setPkgName(pkgName);
                appInfo.setAppIcon(icon);
                appInfo.setIntent(launchIntent);

                mlistAppInfo.add(appInfo); // 添加至列表中
                System.out.println(appLabel + " activityName---" + activityName + " pkgName---" + pkgName);
            }
        }
    }
//        //第一步：获取已安装的应用列表
//        List<ApplicationInfo> apps = getPackageManager().getInstalledApplications(0);
//
//        if((applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM)==0)
//        {
//            //非系统应用
//        }
//        else
//        {
//            //系统应用　　　　　　　　
//        }
//
//        //第二步：获取已安装的应用对应的安装文件（apk）
//
}
