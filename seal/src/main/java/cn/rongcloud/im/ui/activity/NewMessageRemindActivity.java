package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import cn.rongcloud.im.R;


/**
 * Created by Administrator on 2015/3/2.
 */
public class NewMessageRemindActivity extends BaseActionBarActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message_remind);

        getSupportActionBar().setTitle(R.string.new_message_notice);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);

        RelativeLayout  mNotice = (RelativeLayout) findViewById(R.id.re_notice);

        mNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NewMessageRemindActivity.this, DisturbActivity.class));
            }
        });
    }
}
