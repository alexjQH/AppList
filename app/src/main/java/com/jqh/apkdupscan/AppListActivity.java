package com.jqh.apkdupscan;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.jqh.assist.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by alexlily on 2017/10/1.
 */

public class AppListActivity extends AppCompatActivity {

    public static final int FILTER_ALL_APP = 0; // 所有应用程序
    public static final int FILTER_SYSTEM_APP = 1; // 系统程序
    public static final int FILTER_THIRD_APP = 2; // 第三方应用程序
    public static final int FILTER_SDCARD_APP = 3; // 安装在SDCard的应用程序

    private ListView listview = null;

    private PackageManager pm;
    private int filter = FILTER_ALL_APP;
    private List<AppInfo> mlistAppInfo ;
    private BrowseApplicationInfoAdapter browseAppAdapter = null ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.browse_app_list);
        listview = (ListView) findViewById(R.id.listviewApp);
        if(getIntent()!=null){
            filter = getIntent().getIntExtra("filter", 0) ;
        }
        mlistAppInfo = queryFilterAppInfo(filter); // 查询所有应用程序信息
        // 构建适配器，并且注册到listView
        browseAppAdapter = new BrowseApplicationInfoAdapter(this, mlistAppInfo);
        listview.setAdapter(browseAppAdapter);
    }

    // 根据查询条件，查询特定的ApplicationInfo
    private List<AppInfo> queryFilterAppInfo(int filter) {
        pm = this.getPackageManager();

        // 查询所有已经安装的应用程序
        List<ApplicationInfo> listAppcations = pm
                .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        Collections.sort(listAppcations, new ApplicationInfo.DisplayNameComparator(pm));// 排序

        List<AppInfo> appInfos = new ArrayList<AppInfo>(); // 保存过滤查到的AppInfo

        // 根据条件来过滤
        switch (filter) {
            case FILTER_ALL_APP: // 所有应用程序
                appInfos.clear();
                for (ApplicationInfo app : listAppcations) {
                    appInfos.add(getAppInfo(app));
                }
                return appInfos;

            case FILTER_SYSTEM_APP: // 系统程序
                appInfos.clear();
                for (ApplicationInfo app : listAppcations) {
                    if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        appInfos.add(getAppInfo(app));
                    }
                }
                return appInfos;

            case FILTER_THIRD_APP: // 第三方应用程序
                appInfos.clear();
                for (ApplicationInfo app : listAppcations) {
                    //非系统程序
                    if ((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                        appInfos.add(getAppInfo(app));
                    }
                    //本来是系统程序，被用户手动更新后，该系统程序也成为第三方应用程序了
                    else if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0){
                        appInfos.add(getAppInfo(app));
                    }
                }
                return appInfos;

            case FILTER_SDCARD_APP: // 安装在SDCard的应用程序
                appInfos.clear();
                for (ApplicationInfo app : listAppcations) {
                    if ((app.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                        appInfos.add(getAppInfo(app));
                    }
                }
                return appInfos;

            default:
                return null;
        }
        //return null;
    }

    // 构造一个AppInfo对象 ，并赋值
    private AppInfo getAppInfo(ApplicationInfo app) {
        AppInfo appInfo = new AppInfo();
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
