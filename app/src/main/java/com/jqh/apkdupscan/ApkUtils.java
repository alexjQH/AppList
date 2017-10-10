package com.jqh.apkdupscan;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexlily on 2017/10/1.
 */

public class ApkUtils {

    /**
      * 获取Android开机启动列表
    */
    private static final String RECEIVE_BOOT_COMPLETED = "android.permission.RECEIVE_BOOT_COMPLETED";
    private static final String ACTION_BOOT_COMPLETED = "android.permission.ACTION_BOOT_COMPLETED";
    public static List<AppInfo> fetchInstalledApps(Context mContext) {
        PackageManager pm = mContext.getPackageManager();
        List<ApplicationInfo> appInfos = pm.getInstalledApplications(0);
        List<AppInfo> appList = new ArrayList<AppInfo>(appInfos.size());

        for (ApplicationInfo app : appInfos) {
            int flag = pm.checkPermission(RECEIVE_BOOT_COMPLETED, app.packageName);

            if (flag == PackageManager.PERMISSION_GRANTED) {
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
                appInfo.isSysApp = isSysApp(app.flags);

                appList.add(appInfo);
            }
        }
        return appList;
    }

    public static int isSysApp(int flags) {
        if ((flags & ApplicationInfo.FLAG_SYSTEM) != 0) {

            //本来是系统程序，被用户手动更新后，该系统程序也成为第三方应用程序了
            if ((flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                return 1;//APP_TYPE_SYSUPG;
            }
            return 1;//APP_TYPE_SYSTEM;
        } else if ((flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
            return 0;//APP_TYPE_SDCARD; // 安装在SDCard的应用程序
        } else {
            if ((flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                return 0;//APP_TYPE_THIRD;
            }
            return 0;//APP_TYPE_UNKNOWN;
        }
    }

    /**
     * 获取自启应用
     *
     * @param mContext
     * @return
     */

    public static List<AppInfo> fetchAutoApps(Context mContext) {
        PackageManager pm = mContext.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_BOOT_COMPLETED);

        List<ResolveInfo> resolveInfoList = pm.queryBroadcastReceivers(intent,
                PackageManager.GET_DISABLED_COMPONENTS);
        List<AppInfo> appList = new ArrayList<AppInfo>();

        // 得到的参数
        String appName = null;
        String packageReceiver = null;
        Drawable icon = null;
        boolean isSystem = false;
        boolean isenable = true;
        String packageName = null;
        boolean isAutoStart = false;
        boolean isBackStart = false;
        /**
         * 通过 PackageInfo 获取具体信息方法：
         *
         * 包名获取方法：packageInfo.packageName
         * icon获取获取方法：packageManager.getApplicationIcon(applicationInfo)
         * 应用名称获取方法：packageManager.getApplicationLabel(applicationInfo)
         * 使用权限获取方法：packageManager.getPackageInfo(packageName,PackageManager.
         * GET_PERMISSIONS) .requestedPermissions
         *
         * 通过 ResolveInfo 获取具体信息方法：
         *
         * 包名获取方法：resolve.activityInfo.packageName
         * icon获取获取方法：resolve.loadIcon(packageManager)
         * 应用名称获取方法：resolve.loadLabel(packageManager).toString()
         *
         */
        for (ResolveInfo rInfo : resolveInfoList) {
            isAutoStart = false;
            isBackStart = false;
            /**
             * // 查找安装的package是否有开机启动权限 if(PackageManager.PERMISSION_GRANTED
             * == context.getPackageManager().checkPermission(BOOT_START_PERMISSION,app.packageName))
             *
             * BOOT_COMPLETED BOOT_START_PERMISSION RECEIVE_BOOT_COMPLETED
             * ACTION_BOOT_COMPLETED
             */
            String pkgName = rInfo.activityInfo.packageName;
            if (pm.checkPermission(RECEIVE_BOOT_COMPLETED, pkgName) == PackageManager.PERMISSION_GRANTED) {
                isAutoStart = true;
            }
            if (pm.checkPermission(ACTION_BOOT_COMPLETED, pkgName) == PackageManager.PERMISSION_GRANTED) {
                isBackStart = true;
            }

            appName = rInfo.loadLabel(pm).toString();
            packageName = rInfo.activityInfo.packageName;
            packageReceiver = rInfo.activityInfo.packageName + "/" + rInfo.activityInfo.name;
            icon = rInfo.loadIcon(pm);
            ComponentName mComponentName2 = new ComponentName(
                    rInfo.activityInfo.packageName, rInfo.activityInfo.name);

            if (pm.getComponentEnabledSetting(mComponentName2) == 2) {
                isenable = false;
            } else {
                isenable = true;
            }

            if ((rInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                isSystem = true;
            } else {
                isSystem = false;
            }

            AppInfo appInfo = new AppInfo();
            appInfo.setAppLabel(appName);
            appInfo.setAppIcon(icon);
            appInfo.isSysApp = isSysApp(rInfo.activityInfo.applicationInfo.flags);
            appInfo.setPkgName(packageName);
            appInfo.setPkgPath(rInfo.activityInfo.applicationInfo.sourceDir);

//            //
//            AutoStartInfo mAutoStartInfo = new AutoStartInfo();
//            mAutoStartInfo.setLabel(appName);
//            mAutoStartInfo.setSystem(isSystem);
//            mAutoStartInfo.setEnable(isenable);
//            mAutoStartInfo.setIcon(icon);
//            mAutoStartInfo.setPackageName(packageName);
//            mAutoStartInfo.setPackageReceiver(packageReceiver);
//            mAutoStartInfo.setAutoStart(isAutoStart);
//            mAutoStartInfo.setBackStart(isBackStart);

            boolean isAdd = true;
            if (appList != null) {
                for (int j = 0; appList.size() > j; j++) {
                    if (appList.get(j).getPkgName().equals(packageName)) {
                        isAdd = false;
                    }
                }
            }
            if (isAdd) {
                appList.add(appInfo);
            }
        }

        return appList;
    }

    /**
     * 单位转换
     *
     * @param length
     * @return
     */

    public static String toSize(double length) {
        long kb = 1024;
        long mb = 1024 * kb;
        long gb = 1024 * mb;
        if (length < kb) {
            return String.format("%d B", (int) length);
        } else if (length < mb) {
            return String.format("%.2f KB", length / kb);

        } else if (length < gb) {
            return String.format("%.2f MB", length / mb);

        } else {
            return String.format("%.2f GB", length / gb);

        }

    }

    // storage, G M K B
    public static String convertStorage(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else
        return String.format("%d B", size);
    }

//    public static StorageSize convertStorageSize(long size) {
//        long kb = 1024;
//        long mb = kb * 1024;
//        long gb = mb * 1024;
//        StorageSize sto = new StorageSize();
//        if (size >= gb) {
//
//            sto.suffix = "GB";
//            sto.value = (float) size / gb;
//            return sto;
//        } else if (size >= mb) {
//
//            sto.suffix = "MB";
//            sto.value = (float) size / mb;
//
//            return sto;
//        } else if (size >= kb) {
//
//            sto.suffix = "KB";
//            sto.value = (float) size / kb;
//
//            return sto;
//        } else {
//            sto.suffix = "B";
//            sto.value = (float) size;
//
//            return sto;
//        }
//
//    }

    public static class SpaceInfo {
        public long total;
        public long free;
        public String strPath;

        public void SpaceInfo() {
            total = 0;
            free = 0;
            strPath = "";
        }
    }

    public static SpaceInfo getInnerSDCardInfo() {
        File pathFile = Environment.getExternalStorageDirectory();

        StatFs statfs = new StatFs(pathFile.getPath());

        // 获取SDCard上BLOCK总数
        long nTotalBlocks = statfs.getBlockCount();

        // 获取SDCard上每个block的SIZE
        long nBlocSize = statfs.getBlockSize();

        // 获取可供程序使用的Block的数量
        long nAvailaBlock = statfs.getAvailableBlocks();

        // 获取剩下的所有Block的数量(包括预留的一般程序无法使用的块)
        long nFreeBlock = statfs.getFreeBlocks();

        SpaceInfo info = new SpaceInfo();
        // 计算SDCard 总容量大小MB
        info.total = nTotalBlocks * nBlocSize;

        // 计算 SDCard 剩余大小MB
        info.free = nAvailaBlock * nBlocSize;

        info.strPath = pathFile.getAbsolutePath();
        return info;
    }

    public static SpaceInfo getSDCardInfo() {
        // String sDcString = Environment.getExternalStorageState();

        if (Environment.isExternalStorageRemovable()) {
            String sDcString = Environment.getExternalStorageState();
            if (sDcString != null && sDcString.equals(Environment.MEDIA_MOUNTED)) {
                File pathFile = Environment.getExternalStorageDirectory();

                try {
                    StatFs statfs = new StatFs(pathFile.getPath());

                    // 获取SDCard上BLOCK总数
                    long nTotalBlocks = statfs.getBlockCount();

                    // 获取SDCard上每个block的SIZE
                    long nBlocSize = statfs.getBlockSize();

                    // 获取可供程序使用的Block的数量
                    long nAvailaBlock = statfs.getAvailableBlocks();

                    // 获取剩下的所有Block的数量(包括预留的一般程序无法使用的块)
                    long nFreeBlock = statfs.getFreeBlocks();

                    SpaceInfo info = new SpaceInfo();
                    // 计算SDCard 总容量大小MB
                    info.total = nTotalBlocks * nBlocSize;

                    // 计算 SDCard 剩余大小MB
                    info.free = nAvailaBlock * nBlocSize;

                    info.strPath = pathFile.getAbsolutePath();
                    return info;
                } catch (IllegalArgumentException e) {

                }
            }
        }
        return null;
    }
    /**
     * data 目录 getDataDirectory（）
     *
     * @param context
     * @return
     */
    public static SpaceInfo getSystemSpaceInfo(Context context) {
        File path = Environment.getDataDirectory();
        // File path = context.getCacheDir().getAbsoluteFile();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        long availableBlocks = stat.getAvailableBlocks();

        long totalSize = blockSize * totalBlocks;
        long availSize = availableBlocks * blockSize;
        SpaceInfo info = new SpaceInfo();
        info.total = totalSize;
        info.free = availSize;

        info.strPath = path.getAbsolutePath();
        return info;

    }

    public static SpaceInfo getRootSpaceInfo() {
        File path = Environment.getRootDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        long availableBlocks = stat.getAvailableBlocks();

        long totalSize = blockSize * totalBlocks;
        long availSize = availableBlocks * blockSize;
        // 获取每个block的SIZE
        long nBlocSize = stat.getBlockSize();

        SpaceInfo info = new SpaceInfo();
        // 计算 总容量大小MB
        info.total = totalSize;

        // 计算 剩余大小MB
        info.free = availSize;

        info.strPath = path.getAbsolutePath();
        return info;

    }

    //沉浸状态栏
    public void sethah(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 透明状态栏
            // getWindow.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 透明导航栏
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    /**
     * 安装APK
     *
     * @param context
     * @param packageName
     */
    public void update(Context context, String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(Environment
                        .getExternalStorageDirectory(), packageName)),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 判断某个程序是否安装在手机中
     *
     * @param context
     *            上下文
     * @param packageName
     *            需要验证的包名
     * @return 是否安装
     */
    public static boolean isPackageExist(Context context, String packageName) {
        if (packageName == null)
            return false;
        boolean packageExist = false;
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, 0);
            packageExist = true;
        } catch (PackageManager.NameNotFoundException ignored) {
            // L.d("isPackageExist:" + ignored);
        }
        return packageExist;
    }

    /**
     * 判断APK是否已安装
     *
     * @param pm
     * @param packageName
     * @param versionCode
     * @return
     */
    public int doType(PackageManager pm, String packageName, int versionCode) {
        List<PackageInfo> pakageinfos = pm
                .getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (PackageInfo pi : pakageinfos) {
            String pi_packageName = pi.packageName;
            int pi_versionCode = pi.versionCode;
            // 如果这个包名在系统已经安装过的应用中存在
            if (packageName.endsWith(pi_packageName)) {
                // Log.i("test","此应用安装过了");
                if (versionCode == pi_versionCode) {
                    Log.i("test", "已经安装，不用更新，可以卸载该应用");
                    return 1000;
                } else if (versionCode > pi_versionCode) {
                    Log.i("test", "已经安装，有更新");
                    return 2000;
                }
            }
        }
        Log.i("test", "未安装该应用，可以安装");
        return 3000;
    }

    /**
     * 获取当前版本的版本名字
     *
     * @param context
     * @return
     */
    public String getVersion(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
//            return context.getResources().getString(R.string.unknown_versionnum);
        }
    }

    /**
     * 获取当前版本的版本号
     *
     * @return
     */
    public int getVersiontheCode(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
//            return R.string.unknown_versionnum;
        }
    }

    /**
     * 判断WIFI是否连接
     *
     * @param context
     * @return true为已连接
    */
    boolean isWifi = false;

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    /**
     * 删除单个文件
     *
     * @param filePath
     *            被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public boolean deleteFile(String filePath, Context context) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            boolean isOK = file.delete();
            scanFile(file, context);
            return isOK;
        } else {
            Toast.makeText(context, "null", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    /**
     * 扫描文件
     *
     * @param file
     * @param context
     */

    public void scanFile(File file, Context context) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }
    /**
     * get,set
     */
    private List<AppInfo> myFiles = new ArrayList<AppInfo>();

    public List<AppInfo> getMyFiles() {
        return myFiles;
    }

    public void setMyFiles(List<AppInfo> myFiles) {
        this.myFiles = myFiles;
    }

    /**
     * 运用递归的思想，递归去找每个目录下面的apk文件
     *
     * @param file
     */
    public void FindAllAPKFile(File file, Context context) {

        // 手机上的文件,目前只判断SD卡上的APK文件
        // file = Environment.getDataDirectory();
        // SD卡上的文件目录
        if (file.isFile()) {
            String name_s = file.getName();
            AppInfo myFile = new AppInfo();
            String apk_path = null;
            // MimeTypeMap.getSingleton()
            if (name_s.toLowerCase().endsWith(".apk")) {
                apk_path = file.getAbsolutePath();// apk文件的绝对路劲
                // System.out.println("----" + file.getAbsolutePath() + "" +
                // name_s);
                PackageManager pm = context.getPackageManager();
                PackageInfo packageInfo = pm.getPackageArchiveInfo(apk_path,
                        PackageManager.GET_ACTIVITIES);
                ApplicationInfo appInfo = packageInfo.applicationInfo;

                /** 获取apk的图标 */
//                appInfo.sourceDir = apk_path;
//                appInfo.publicSourceDir = apk_path;
//                Drawable apk_icon = appInfo.loadIcon(pm);
//                myFile.setIcon(apk_icon);
//                /** 得到包名 */
//                String packageName = packageInfo.packageName;
//                myFile.setName(packageName);
//                /** apk的绝对路劲 */
//                myFile.setPath(file.getAbsolutePath());
//                /** apk的版本名称 String */
//                String versionName = packageInfo.versionName;
//                myFile.setVersionName(versionName);
//                /** apk的版本号码 int */
//                int versionCode = packageInfo.versionCode;
//                myFile.setVersionCode(versionCode);
//                /** 安装处理类型 */
//                int type = doType(pm, packageName, versionCode);
//                myFile.setType(type);
//
//                Log.i("ok", "处理类型:" + String.valueOf(type) + "\n"
//                        + "------------------我是纯洁的分割线-------------------");
                myFiles.add(myFile);
            }
            // String apk_app = name_s.substring(name_s.lastIndexOf("."));
        } else {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File file_str : files) {
                    FindAllAPKFile(file_str, context);
                }
            }
        }
    }

    /**
     * 获得所有的应用程序信息
     *
     * @return
     */

    public static List<AppInfo> getAllApps(Context context) {

        PackageManager pm = context.getPackageManager();

        List<PackageInfo> packages = pm.getInstalledPackages(0);

        List<AppInfo> list = new ArrayList<AppInfo>();

        for (PackageInfo info : packages) {

            ApplicationInfo applicationInfo = info.applicationInfo;
            String name = applicationInfo.loadLabel(pm).toString();
            Drawable icon = applicationInfo.loadIcon(pm);

            String sourceDir = applicationInfo.sourceDir;
            File file = new File(sourceDir);

            int flags = applicationInfo.flags;
            boolean isInstallSD = false;
            if ((flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == ApplicationInfo.FLAG_EXTERNAL_STORAGE) {
                isInstallSD = true;
            }

            boolean isSystem = false;
            if ((flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                isSystem = true;
            }

            AppInfo bean = new AppInfo();
//            bean.icon = icon;
//            bean.name = name;
//            bean.size = file.length();
//            bean.isInstallSD = isInstallSD;
//            bean.isSystem = isSystem;
//            bean.packageName = info.packageName;

            list.add(bean);
        }

        return list;

    }

    public static List<AppInfo> getAllLaunchApps(Context context) {

        PackageManager pm = context.getPackageManager();

        List<PackageInfo> packages = pm.getInstalledPackages(0);

        List<AppInfo> list = new ArrayList<AppInfo>();

        for (PackageInfo info : packages) {

            Intent intent = pm.getLaunchIntentForPackage(info.packageName);

            if (intent == null) {

                continue;
            }

            ApplicationInfo applicationInfo = info.applicationInfo;
            String name = applicationInfo.loadLabel(pm).toString();
            Drawable icon = applicationInfo.loadIcon(pm);

            String sourceDir = applicationInfo.sourceDir;
            File file = new File(sourceDir);

            int flags = applicationInfo.flags;
            boolean isInstallSD = false;
            if ((flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == ApplicationInfo.FLAG_EXTERNAL_STORAGE) {
                isInstallSD = true;
            }

            boolean isSystem = false;
            if ((flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                isSystem = true;
            }

            AppInfo bean = new AppInfo();
//            bean.icon = icon;
//            bean.name = name;
//            bean.size = file.length();
//            bean.isInstallSD = isInstallSD;
//            bean.isSystem = isSystem;
//            bean.packageName = info.packageName;

            list.add(bean);
        }

        return list;

    }

    
}
