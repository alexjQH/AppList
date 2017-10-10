package com.jqh.assist.installedapp;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.jqh.assist.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InstalledAppListActivity extends AppCompatActivity {

    public static final int APP_TYPE_UNKNOWN = 0; // 所有应用程序
    public static final int APP_TYPE_SYSTEM = 1; // 系统程序
    public static final int APP_TYPE_SYSUPG = 2; // 系统程序
    public static final int APP_TYPE_THIRD = 3; // 第三方应用程序
    public static final int APP_TYPE_SDCARD = 4; // 安装在SDCard的应用程序

    private ListView listview = null;

    private PackageManager pm;
    private List<InstalledAppInfo> mlistAppInfo;
    private List<InstalledAppInfo> mdatasetAdapter;
    private InstalledAppListAdapter browseAppAdapter = null ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installed_app_list);

        listview = (ListView) findViewById(R.id.installedAppListView);
        mlistAppInfo = queryInstalledAppInfo(); // 查询所有应用程序信息

        // 构建适配器，并且注册到listView
        mdatasetAdapter = getAppInfos(APP_TYPE_UNKNOWN);
        browseAppAdapter = new InstalledAppListAdapter(this, mdatasetAdapter);
        listview.setAdapter(browseAppAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                InstalledAppInfo appInfo = mdatasetAdapter.get(position);
                if (appInfo != null) {
                    InstalledAppDetailDlg dlg = new InstalledAppDetailDlg(appInfo, InstalledAppListActivity.this);

                    dlg.show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //四个参数的含义。1，group的id,2,item的id,3,是否排序，4，将要显示的内容
        menu.add(0, 1, 0, "所有应用");
        menu.add(0, 2, 0, "系统应用");
        menu.add(0, 3, 0, "系统升级");
        menu.add(0, 4, 0, "自装应用");
        menu.add(0, 5, 0, "SD卡中应用");
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int filter = APP_TYPE_UNKNOWN;
        switch (item.getItemId()){
            case 1: filter = APP_TYPE_UNKNOWN; break;
            case 2: filter = APP_TYPE_SYSTEM; break;
            case 3: filter = APP_TYPE_SYSUPG; break;
            case 4: filter = APP_TYPE_THIRD; break;
            case 5: filter = APP_TYPE_SDCARD; break;
        }
        mdatasetAdapter = getAppInfos(filter);
        browseAppAdapter.setDataSet(mdatasetAdapter);
        System.out.println("type="+filter +"; size="+mdatasetAdapter.size());
        browseAppAdapter.notifyDataSetInvalidated();
        return true;
    }
    private List<InstalledAppInfo> getAppInfos(int type) {
        List<InstalledAppInfo> appInfos = new ArrayList<InstalledAppInfo>(); // 保存过滤查到的AppInfo
        for (InstalledAppInfo info: mlistAppInfo) {
            if (info.getAppType() == type || type == APP_TYPE_UNKNOWN) {
                appInfos.add(info);
            }
        }
        return appInfos;
    }

    // 根据查询条件，查询特定的ApplicationInfo
    private List<InstalledAppInfo> queryInstalledAppInfo() {
        pm = this.getPackageManager();

        // 查询所有已经安装的应用程序
        List<ApplicationInfo> listAppcations = pm
                .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        Collections.sort(listAppcations, new ApplicationInfo.DisplayNameComparator(pm));// 排序

        List<InstalledAppInfo> appInfos = new ArrayList<InstalledAppInfo>(); // 保存过滤查到的AppInfo

        for (ApplicationInfo app : listAppcations) {
            appInfos.add(getAppInfo(app));
        }
        return appInfos;
    }

    public int getAppType(ApplicationInfo app) {
        if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {

            //本来是系统程序，被用户手动更新后，该系统程序也成为第三方应用程序了
            if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                return APP_TYPE_SYSUPG;
            }
            return APP_TYPE_SYSTEM;
        } else if ((app.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
            return APP_TYPE_SDCARD; // 安装在SDCard的应用程序
        } else {
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                return APP_TYPE_THIRD;
            }
            return APP_TYPE_UNKNOWN;
        }
    }

    // 构造一个AppInfo对象 ，并赋值
    private InstalledAppInfo getAppInfo(ApplicationInfo app) {
        InstalledAppInfo appInfo = new InstalledAppInfo();
        appInfo.setAppType(getAppType(app));
        appInfo.setAppLabel((String) app.loadLabel(pm));
        appInfo.setAppIcon(app.loadIcon(pm));
        appInfo.setPkgName(app.packageName);
        appInfo.setPkgPath(app.sourceDir);
        try {
            String strName = pm.getPackageInfo(app.packageName, 0).versionName;
            appInfo.setVersionName(strName);
        } catch (Exception e) {
            appInfo.setVersionName("Unknown");
        }
        return appInfo;
    }

}
