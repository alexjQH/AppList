package com.jqh.assist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.jqh.assist.installedapp.InstalledAppListActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btnInstalledAppList = (Button)findViewById(R.id.btnInstalledAppList);
        final Button btnBootStartAppList = (Button)findViewById(R.id.btnBootStartAppList);
        final Button btnAutoStartAppList = (Button)findViewById(R.id.btnAutoStartAppList);
        final Button btnSdcardInfo = (Button)findViewById(R.id.btnSdcardInfo);
        final Button btnApkList = (Button) findViewById(R.id.btnApkList);

        View.OnClickListener obj = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                String strActivityName = "";
                switch (v.getId()) {
                    case R.id.btnInstalledAppList: strActivityName = ".InstalledAppListActivity";
                        intent.setClass(getApplicationContext(), InstalledAppListActivity.class);
                        break;
                    case R.id.btnBootStartAppList: strActivityName = ".AppListActivity";
                        intent.setClass(getApplicationContext(), AppListActivity.class);
                        intent.putExtra("filter", 1);
                        break;
                    case R.id.btnAutoStartAppList: strActivityName = "";
                        intent.setClass(getApplicationContext(), AppListActivity.class);
                        intent.putExtra("filter", 2);
                        break;
                    case R.id.btnSdcardInfo:       strActivityName = "";
                        intent.setClass(getApplicationContext(), SpaceInfoActivity.class);
                        break;
                    case R.id.btnApkList:
                        intent.setClass(getApplicationContext(), ApkListActivity.class);
                        break;
                    default:
                        return;
                }
                //intent.setClassName("com.jqh.assist", strActivityName);
                startActivity(intent);
            }
        };
        btnInstalledAppList.setOnClickListener(obj);
        btnBootStartAppList.setOnClickListener(obj);
        btnAutoStartAppList.setOnClickListener(obj);
        btnSdcardInfo.setOnClickListener(obj);
        btnApkList.setOnClickListener(obj);
    }
}
