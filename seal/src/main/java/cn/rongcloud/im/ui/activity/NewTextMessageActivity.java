package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.adapter.NewTextReplyAdapter;
import cn.rongcloud.im.utils.Constants;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Discussion;
import io.rong.imlib.model.UserInfo;

/**
 * Created by Bob on 15/11/16.
 */
public class NewTextMessageActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private ListView mReplyListView;
    private String mTargetId;
    private Conversation.ConversationType mConversationType;
    private List<UserInfo> mUserInfoList;
    private List mNumberlist;
    private NewTextReplyAdapter mNewTextReplyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reply);

        getSupportActionBar().setTitle("选择回复的人");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);

        mReplyListView = (ListView) findViewById(R.id.de_reply);

        mNumberlist = new ArrayList();
        mUserInfoList = new ArrayList<UserInfo>();
        initDate();
    }

    private void initDate() {

        Intent intent = getIntent();
        if(intent!=null && intent.hasExtra("DEMO_REPLY_CONVERSATIONTYPE")&&intent.hasExtra("DEMO_REPLY_TARGETID")) {

            String conversationType = intent.getStringExtra("DEMO_REPLY_CONVERSATIONTYPE");
            mTargetId = intent.getStringExtra("DEMO_REPLY_TARGETID");

            mConversationType = Conversation.ConversationType.valueOf(conversationType);

            if (mConversationType.equals(Conversation.ConversationType.DISCUSSION)) {
                RongIM.getInstance().getDiscussion(mTargetId, new RongIMClient.ResultCallback<Discussion>() {
                    @Override
                    public void onSuccess(Discussion discussion) {

                        mNumberlist = discussion.getMemberIdList();

//                        if (DemoContext.getInstance().getSharedPreferences() != null) {
//                            String   userId = DemoContext.getInstance().getSharedPreferences().getString(Constants.APP_USER_ID,Constants.DEFAULT);
//                            mNumberlist.remove(userId);
//                        }
                        //TODO 根据讨论组信息获取讨论组成员id的集合 除去自己的 userid  批处理去网络请求数据 提供给adapter

//                        mUserInfoList = DemoContext.getInstance().getUserInfoList(mNumberlist);

                        mNewTextReplyAdapter = new NewTextReplyAdapter(NewTextMessageActivity.this, mUserInfoList);
                        mReplyListView.setAdapter(mNewTextReplyAdapter);
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode e) {

                    }
                });
            } else if (mConversationType.equals(Conversation.ConversationType.GROUP)) {

            }
        }

        mReplyListView.setOnItemClickListener(this);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        Intent intent = new Intent();
        intent.putExtra("REPLY_ID",mNumberlist.get(i).toString());
        intent.putExtra("REPLY_NAME",mUserInfoList.get(i).getName().toString());
        setResult(Constants.MESSAGE_REPLY,intent);
        finish();
    }
}
