package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;


import cn.rongcloud.im.R;
import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.db.DBManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.AgreeFriendsResponse;
import cn.rongcloud.im.server.response.UserRelationshipResponse;
import cn.rongcloud.im.server.utils.CommonUtils;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.ui.adapter.NewFriendListAdapter;
import cn.rongcloud.im.message.AgreedFriendRequestMessage;
import cn.rongcloud.im.utils.Constants;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;

/**
 * Created by Bob on 2015/3/26.
 */
public class NewFriendListActivity extends BaseActivity implements NewFriendListAdapter.OnItemButtonClick {

    private static final String TAG = NewFriendListActivity.class.getSimpleName();
    private static final int GETALL = 11;
    private static final int AGREEFRIENDS = 12;

    private ListView shipListView;
    private NewFriendListAdapter adapter;
    private String friendId;
    private TextView isData;

    private SharedPreferences sp;

    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friendlist);
        initView();

        if (!CommonUtils.isNetworkConnected(mContext)) {
            NToast.shortToast(mContext, R.string.check_network);
            return;
        }
        LoadDialog.show(mContext);
        request(GETALL);
        adapter = new NewFriendListAdapter(mContext);
        shipListView.setAdapter(adapter);

    }

    protected void initView() {
        getSupportActionBar().setTitle(R.string.new_friends);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        shipListView = (ListView) findViewById(R.id.shiplistview);
        isData = (TextView) findViewById(R.id.isData);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        editor = sp.edit();

    }


    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case GETALL:
                return action.getAllUserRelationship();
            case AGREEFRIENDS:
                return action.agreeFriends(friendId);
        }
        return super.doInBackground(requestCode, id);
    }

    UserRelationshipResponse urres;

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case GETALL:
                    urres = (UserRelationshipResponse) result;

                    if (urres.getResult().size() == 0) {
                        isData.setVisibility(View.VISIBLE);
                        LoadDialog.dismiss(mContext);
                        return;
                    }

//                    friendId = urres.getResult().get(index).getUser().getId();
                    adapter.removeAll();
                    adapter.addData(urres.getResult());

                    adapter.notifyDataSetChanged();
                    adapter.setOnItemButtonClick(this);
                    LoadDialog.dismiss(mContext);
                    break;
                case AGREEFRIENDS:
                    AgreeFriendsResponse afres = (AgreeFriendsResponse) result;
                    if (afres.getCode() == 200) {
                        UserRelationshipResponse.ResultEntity bean = urres.getResult().get(index);
                        DBManager.getInstance(mContext).getDaoSession().getFriendDao().insertOrReplace(new Friend(bean.getUser().getId()
                                , bean.getUser().getNickname()
                                , bean.getUser().getPortraitUri()
                                , bean.getDisplayName()
                                , String.valueOf(bean.getStatus())
                                , null  //TODO 不是时间戳 格式错误 2016-01-07T06:22:55.000Z
                        ));
                        sendMessage(friendId);
                        // 通知好友列表刷新数据
                        NToast.shortToast(mContext, R.string.agreed_friend);
                        LoadDialog.dismiss(mContext);
                        BroadcastManager.getInstance(mContext).sendBroadcast(SealAppContext.UPDATEFRIEND);
                        request(GETALL); //刷新 UI 按钮
                    }

            }
        }
    }


    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case GETALL:
                break;

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.de_add_friend_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.icon:
                Intent intent = new Intent(NewFriendListActivity.this, SearchFriendActivity.class);
                startActivityForResult(intent, Constants.FRIENDLIST_REQUESTCODE);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        if (adapter != null) {
            adapter = null;
        }
        super.onDestroy();
    }

    private int index;

    @Override
    public boolean onButtonClick(int position, View view, int status) {
        index = position;
        switch (status) {
            case 11: //收到了好友邀请
                if (!CommonUtils.isNetworkConnected(mContext)) {
                    NToast.shortToast(mContext, R.string.check_network);
                    break;
                }
                LoadDialog.show(mContext);
//                friendId = null;
                friendId = urres.getResult().get(position).getUser().getId();
                request(AGREEFRIENDS);
                break;
            case 10: // 发出了好友邀请
                break;
            case 21: // 忽略好友邀请
                break;
            case 20: // 已是好友
                break;
            case 30: // 删除了好友关系
                break;
        }
        return false;
    }


    /**
     * 添加好友成功后，向对方发送一条消息
     *
     * @param id 对方id
     */
    private void sendMessage(String id) {
        final AgreedFriendRequestMessage message = new AgreedFriendRequestMessage(id, "agree");

            //获取当前用户的 userid
            String userid = sp.getString("loginid", "");
            String username = sp.getString("loginnickname", "");
            String userportrait = sp.getString("loginPortrait", "");

            UserInfo userInfo = new UserInfo(userid, username, Uri.parse(userportrait));
            //把用户信息设置到消息体中，直接发送给对方，可以不设置，非必选项
            message.setUserInfo(userInfo);

                //发送一条添加成功的自定义消息，此条消息不会在ui上展示
                RongIM.getInstance().sendMessage(Conversation.ConversationType.PRIVATE, id, message, null, null, new RongIMClient.SendMessageCallback() {
                    @Override
                    public void onError(Integer messageId, RongIMClient.ErrorCode e) {
                        Log.e(TAG, Constants.DEBUG + "------DeAgreedFriendRequestMessage----onError--");
                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        Log.e(TAG, Constants.DEBUG + "------DeAgreedFriendRequestMessage----onSuccess--" + message.getMessage());

                    }
                });
    }
}
