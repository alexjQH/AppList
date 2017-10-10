package com.jqh.assist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.widget.TextView;

import com.jqh.apkdupscan.ApkUtils;

import org.w3c.dom.Text;

public class SpaceInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_info);
        this.setTitle("空间信息");

        ApkUtils.SpaceInfo info = ApkUtils.getRootSpaceInfo();
        setSpaceInfo(info, R.id.tvPath1, R.id.tvTotalSize1, R.id.tvFreeSize1);

        info = ApkUtils.getSystemSpaceInfo(this);
        setSpaceInfo(info, R.id.tvPath2, R.id.tvTotalSize2, R.id.tvFreeSize2);

        info = ApkUtils.getInnerSDCardInfo();
        setSpaceInfo(info, R.id.tvPath3, R.id.tvTotalSize3, R.id.tvFreeSize3);
    }

    private void setSpaceInfo(ApkUtils.SpaceInfo info, int idPath, int idTotal, int idFree) {
        TextView tvPath = (TextView) findViewById(idPath);
        TextView tvTotal = (TextView) findViewById(idTotal);
        TextView tvFree = (TextView) findViewById(idFree);

        if (info == null) {
            tvPath.setText("");
            tvTotal.setText("");
            tvFree.setText("");
        } else {
            tvPath.setText(info.strPath);
            tvTotal.setText(Formatter.formatFileSize(this, info.total));
            tvFree.setText(Formatter.formatFileSize(this, info.free));
        }
    }
}
