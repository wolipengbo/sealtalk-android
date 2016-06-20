package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.rongcloud.im.R;

/**
 * Created by Administrator on 2015/3/3.
 */
public class AboutRongCloudActivity extends BaseActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        getSupportActionBar().setTitle(R.string.set_rongcloud);
        RelativeLayout mUpdateLog = (RelativeLayout) findViewById(R.id.rl_update_log);
        RelativeLayout mFunctionIntroduce = (RelativeLayout) findViewById(R.id.rl_function_introduce);
        RelativeLayout mDVDocument = (RelativeLayout) findViewById(R.id.rl_dv_document);
        RelativeLayout mRongCloudWeb = (RelativeLayout) findViewById(R.id.rl_rongcloud_web);
        TextView mCurrentVersion = (TextView) findViewById(R.id.version_new);

        mUpdateLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AboutRongCloudActivity.this, UpdateLogActivity.class));
            }
        });
        mFunctionIntroduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AboutRongCloudActivity.this, FunctionIntroducedActivity.class));
            }
        });
        mDVDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AboutRongCloudActivity.this, DocumentActivity.class));
            }
        });
        mRongCloudWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AboutRongCloudActivity.this, RongWebActivity.class));
            }
        });

        String[] versionInfo = getVersionInfo();
        mCurrentVersion.setText(versionInfo[1]);
    }

    private String[] getVersionInfo() {
        String[] version = new String[2];

        PackageManager packageManager = getPackageManager();

        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            version[0] = String.valueOf(packageInfo.versionCode);
            version[1] = packageInfo.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return version;
    }
}
