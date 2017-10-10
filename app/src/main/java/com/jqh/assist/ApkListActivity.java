package com.jqh.assist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jqh.apkdupscan.ApkUtils;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ApkListActivity extends AppCompatActivity {
    private final String TAG = "ApkListActivity";

    public static final int APK_DEFAULT     = 0; //
    public static final int APK_NEWVERSION  = 1; // 新版本
    public static final int APK_OLDVERSION  = 2; // 旧版本
    public static final int APK_INSTALLED   = 3; // 已安装
    public static final int APK_UNINSTALLED = 4; // 未安装
    public static final int APK_SIGNERROR   = 5; // 签名错误
    public static final int APK_DUPINSTALLED= 6; // 重复已安装

    private List<ApkInfo>   mlistApkInfo;
    private PackageManager  pm;

    private ListView listView;
    private ApkListAdapter adapter;

    private Thread threadScan = null;
    private TextView tvScanTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apk_list);

//        initButtonView();

        pm = this.getPackageManager();

        mlistApkInfo = new ArrayList<ApkInfo>();

        tvScanTips = (TextView) findViewById(R.id.tvScanTips);
        listView = (ListView) findViewById(R.id.apkListView);
        adapter = new ApkListAdapter(this.getBaseContext(), mlistApkInfo);
        listView.setAdapter(adapter);

//        使其他线程访问Ui线程，并委托后者更新Ui
//        Activity.runOnUiThread(Runnable)，View.post(runnable)，View.postDelayed(runnable)
//        About_Activity.this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                newVersion.setVisibility(View.VISIBLE);
//                newVersion.setText(responseRes);
//                // 是否更新版本，必须放这里才能保证方法里面的得到的newVersion不为空
//                yesOrNoUpdataApk();
//            }
//        });


//        ApkListActivity.this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    ApkUtils.SpaceInfo info = ApkUtils.getInnerSDCardInfo();
//                    FindAllAPKFile(ApkListActivity.this, new File(info.strPath));
//
//                    mlistApkInfo = getMyFiles();
//                    Collections.sort(mlistApkInfo, new Comparator<ApkInfo>() {
//                        public int compare(ApkInfo a, ApkInfo b) {
//                            return a.appLabel.compareToIgnoreCase(b.appLabel);
////                            Collator.getInstance().compare(sa.toString(), sb.toString());
//                        }
//                    });
//
//                    adapter.setDataSet(mlistApkInfo);
//                    adapter.notifyDataSetChanged();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });

        startScanApk();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                final ApkInfo apkInfo = mlistApkInfo.get(position);
                final int pos = position;
                if (apkInfo != null) {
                    //
                    AlertDialog.Builder alert = new AlertDialog.Builder(ApkListActivity.this, R.style.Theme_AppCompat_Dialog_Alert)
                            .setTitle("删除程序包")
                            .setIcon(R.drawable.ic_launcher_background)
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    File file = new File(apkInfo.pkgPath);
                                    if (file.isFile() && file.exists()) {
                                        if (file.delete()) {
                                            Toast.makeText(ApkListActivity.this, "成功删除" + apkInfo.pkgPath, Toast.LENGTH_LONG);
                                            mlistApkInfo.remove(pos);
                                            updateApkListView();
                                            return;
                                        }
                                    }
                                    Toast.makeText(ApkListActivity.this, "删除失败" + apkInfo.pkgPath, Toast.LENGTH_LONG);
                                }
                            });
                    AlertDialog alertDialog=alert.create();
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();
                }
            }
        });
    }
    private void startScanApk() {
        threadScan = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ApkUtils.SpaceInfo info = ApkUtils.getInnerSDCardInfo();
                    FindAllAPKFile(ApkListActivity.this, new File(info.strPath));

                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = "over";
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        threadScan.start();
    }
    private void updateApkListView() {
        adapter.setDataSet(mlistApkInfo);
        adapter.notifyDataSetInvalidated();

        String strTitle = new String();
        strTitle = "安装包管理(" + mlistApkInfo.size() +")";
        this.setTitle(strTitle);
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case 0:
                mlistApkInfo.add((ApkInfo)msg.obj);
                updateApkListView();
//                adapter.setDataSet(mlistApkInfo);
//                adapter.notifyDataSetChanged();
                listView.setSelection(listView.getBottom());
                break;

            case 1:
                //mlistApkInfo = getMyFiles();
                tvScanTips.setVisibility(View.GONE);
                sorted();
                updateApkListView();
                listView.setSelection(listView.getTop());
                break;

            case 2:
                String path = (String)(msg.obj);
                tvScanTips.setText(path);
                break;
            }
        }
    };
    public void sorted() {
        Collections.sort(mlistApkInfo, new Comparator<ApkInfo>() {
            public int compare(ApkInfo a, ApkInfo b) {
                //return a.appLabel.compareToIgnoreCase(b.appLabel);
                return Collator.getInstance().compare(a.appLabel, b.appLabel);
            }
        });
   }

//    public void initButtonView() {
//        final Button btnScanApk = (Button)findViewById(R.id.btnScanApk);
//        final Button btnInstallApk = (Button)findViewById(R.id.btnInstallApk);
//        final Button btnDeleteApk = (Button)findViewById(R.id.btnDeleteApk);
//
//        View.OnClickListener obj = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                switch (v.getId()) {
//                case R.id.btnScanApk:
//                    if (threadScan != null) {
//                        threadScan.stop();
//                        adapter.setDataSet(mlistApkInfo);
//                        adapter.notifyDataSetInvalidated();
//                        //btnScanApk.setText("开始扫描");
//                        btnInstallApk.setVisibility(View.VISIBLE);
//                        btnDeleteApk.setVisibility(View.VISIBLE);
//                        btnScanApk.setVisibility(View.GONE);
//                    } else {
//                        btnScanApk.setText("停止扫描");
//                        mlistApkInfo.clear();
//                        adapter.setDataSet(mlistApkInfo);
//                        adapter.notifyDataSetInvalidated();
//                        startScanApk();
//                    }
//                    break;
//                case R.id.btnInstallApk:
//                    break;
//                case R.id.btnDeleteApk:
//                    break;
//                default:
//                    return;
//                }
//            }
//        };
//        btnScanApk.setOnClickListener(obj);
//        btnInstallApk.setOnClickListener(obj);
//        btnDeleteApk.setOnClickListener(obj);
//    }


    public class ApkInfo {
        public int isSysApp;
        public int status; //已安装、未安装、旧版本、新版本、签名错误
        public String appLabel;    //应用程序标签
        public Drawable appIcon ;  //应用程序图像
        public String pkgName ;    //应用程序所对应的包名
        public String pkgPath;
        public String versionName;
        public long fileSize;

        public ApkInfo() {}
    }

    //递归去找每个目录下面的apk文件
    private List<ApkInfo> myApkInfos = new ArrayList<ApkInfo>();
    public List<ApkInfo> getMyFiles() {
        return myApkInfos;
    }
    public void setMyFiles(List<ApkInfo> myFiles) {
        this.myApkInfos = myFiles;
    }

    public void FindAllAPKFile(Context context, File file) {
        if (file.isFile()) {
            String name_s = file.getName();

            if (name_s.toLowerCase().endsWith(".apk")) {
                String apkFilePath = file.getAbsolutePath();// apk文件的绝对路劲

                Message  tips = new Message();
                tips.what = 2;
                tips.obj = apkFilePath;
                mHandler.sendMessage(tips);

                PackageInfo info = pm.getPackageArchiveInfo(apkFilePath, 0);
                if (info != null) {
                    info.applicationInfo.sourceDir = apkFilePath;
                    info.applicationInfo.publicSourceDir = apkFilePath;

                    ApkInfo apkInfo = new ApkInfo();
                    apkInfo.versionName = info.versionName;
                    apkInfo.appLabel = (String) info.applicationInfo.loadLabel(pm);
                    apkInfo.appIcon = info.applicationInfo.loadIcon(pm);
                    apkInfo.pkgName = info.packageName;//info.applicationInfo.packageName;
                    apkInfo.pkgPath = apkFilePath;
                    apkInfo.fileSize = file.length();

                    PackageInfo installedInfo = null;
                    try {
                        installedInfo = pm.getPackageInfo(info.packageName, 0);
                    } catch (PackageManager.NameNotFoundException ignored) {
                        // L.d("isPackageExist:" + ignored);
                    }
                    if (installedInfo != null) {
                        if (info.versionCode == installedInfo.versionCode) {
                            apkInfo.status = APK_INSTALLED;
                        } else if (info.versionCode > installedInfo.versionCode) {
                            apkInfo.status = APK_NEWVERSION;
                        } else {
                            apkInfo.status = APK_OLDVERSION;
                        }
                    } else {
                        apkInfo.status = APK_UNINSTALLED;
                    }

                    //myApkInfos.add(apkInfo);
                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = apkInfo;
                    mHandler.sendMessage(msg);
                }
            }
        } else {
            File[] files = file.listFiles();
            if (files != null) {
                for (File file_str : files) {
                    FindAllAPKFile(context, file_str);
                }
            }
        }
    }

}
