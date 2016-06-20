package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.db.DBManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.GetUserInfosResponse;
import cn.rongcloud.im.server.utils.RongGenerate;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.utils.OperationRong;
import cn.rongcloud.im.server.widget.DialogWithYesOrNoUtils;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.ui.widget.DemoGridView;
import cn.rongcloud.im.ui.widget.switchbutton.SwitchButton;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Discussion;
import io.rong.imlib.model.UserInfo;


/**
 * Created by AMing on 16/5/5.
 * Company RongCloud
 */
public class DiscussionDetailActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private static final int FINDUSERINFO = 010;
    private String targetId, createId, discussionName;

    private Discussion mDiscussion;

    private TextView memberSize;

    private List<UserInfo> memberList = new ArrayList<>();;

    private DemoGridView gridview;

    private GridAdapter adapter;

    private boolean isCreated;

    private SwitchButton discuTop, discuNof;

    private LinearLayout discuClean;

    private Button deleteDiscussion;

    private List<String> ids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_discussion);
        getSupportActionBar().setTitle("讨论组详情");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        targetId = getIntent().getStringExtra("TargetId");
        if (TextUtils.isEmpty(targetId)) {
            return;
        }
        LoadDialog.show(this);
        initView();
        RongIM.getInstance().getDiscussion(targetId, new RongIMClient.ResultCallback<Discussion>() {
            @Override
            public void onSuccess(Discussion discussion) {
                mDiscussion = discussion;
                if (mDiscussion != null) {
                    initData(mDiscussion);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {

            }
        });

    }


    private void initView() {
        memberSize = (TextView) findViewById(R.id.discu_member_size);
        gridview = (DemoGridView) findViewById(R.id.discu_gridview);
        discuTop = (SwitchButton) findViewById(R.id.sw_discu_top);
        discuNof = (SwitchButton) findViewById(R.id.sw_discu_notfaction);
        discuClean = (LinearLayout) findViewById(R.id.discu_clean);
        deleteDiscussion = (Button) findViewById(R.id.discu_quit);
        discuTop.setOnCheckedChangeListener(this);
        discuNof.setOnCheckedChangeListener(this);
        discuClean.setOnClickListener(this);
        deleteDiscussion.setOnClickListener(this);
        RongIM.getInstance().getConversation(Conversation.ConversationType.DISCUSSION, targetId, new RongIMClient.ResultCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                if (conversation == null) {
                    return;
                }
                if (conversation.isTop()) {
                    discuTop.setChecked(true);
                } else {
                    discuTop.setChecked(false);
                }

            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });

        RongIM.getInstance().getConversationNotificationStatus(Conversation.ConversationType.DISCUSSION, targetId, new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
            @Override
            public void onSuccess(Conversation.ConversationNotificationStatus conversationNotificationStatus) {

                if (conversationNotificationStatus == Conversation.ConversationNotificationStatus.DO_NOT_DISTURB ? true : false) {
                    discuNof.setChecked(true);
                } else {
                    discuNof.setChecked(false);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });
    }


    private void initData(Discussion mDiscussion) {
        memberSize.setText("讨论组成员(" + mDiscussion.getMemberIdList().size() + ")");
        createId = mDiscussion.getCreatorId();
        discussionName = mDiscussion.getName();
        ids = mDiscussion.getMemberIdList();
        if (ids != null) {
            request(FINDUSERINFO);
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sw_discu_top:
                if (isChecked) {
                    OperationRong.setConversationTop(mContext, Conversation.ConversationType.DISCUSSION, targetId, true);
                } else {
                    OperationRong.setConversationTop(mContext, Conversation.ConversationType.DISCUSSION, targetId, false);
                }
                break;
            case R.id.sw_discu_notfaction:
                if (isChecked) {
                    OperationRong.setConverstionNotif(mContext, Conversation.ConversationType.DISCUSSION, targetId, true);
                } else {
                    OperationRong.setConverstionNotif(mContext, Conversation.ConversationType.DISCUSSION, targetId, false);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.discu_clean:
                DialogWithYesOrNoUtils.getInstance().showDialog(mContext, "是否清除会话聊天记录？", new DialogWithYesOrNoUtils.DialogCallBack() {
                    @Override
                    public void exectEvent() {
                        if (RongIM.getInstance() != null) {
                            RongIM.getInstance().clearMessages(Conversation.ConversationType.DISCUSSION, targetId, new RongIMClient.ResultCallback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean aBoolean) {
                                    NToast.shortToast(mContext, "清除成功");
                                }

                                @Override
                                public void onError(RongIMClient.ErrorCode errorCode) {
                                    NToast.shortToast(mContext, "清除失败");
                                }
                            });
                        }
                    }

                    @Override
                    public void exectEditEvent(String editText) {

                    }

                    @Override
                    public void updatePassword(String oldPassword, String newPassword) {

                    }
                });
                break;
            case R.id.discu_quit:
                DialogWithYesOrNoUtils.getInstance().showDialog(mContext, "是否退出并删除当前讨论组?", new DialogWithYesOrNoUtils.DialogCallBack() {
                    @Override
                    public void exectEvent() {
                        RongIM.getInstance().quitDiscussion(targetId, new RongIMClient.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                RongIM.getInstance().removeConversation(Conversation.ConversationType.DISCUSSION, targetId);
                                Intent i = new Intent();
                                i.putExtra("disFinish","disFinish");
                                setResult(112,i);
                                finish();
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {

                            }
                        });
                    }

                    @Override
                    public void exectEditEvent(String editText) {

                    }

                    @Override
                    public void updatePassword(String oldPassword, String newPassword) {

                    }
                });

                break;
        }
    }


    private class GridAdapter extends BaseAdapter {

        private List<UserInfo> list;
        Context context;


        public GridAdapter(Context context, List<UserInfo> list) {
            this.list = list;
            this.context = context;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.social_chatsetting_gridview_item, null);
            }
            SelectableRoundedImageView iv_avatar = (SelectableRoundedImageView) convertView.findViewById(R.id.iv_avatar);
            TextView tv_username = (TextView) convertView.findViewById(R.id.tv_username);
            ImageView badge_delete = (ImageView) convertView.findViewById(R.id.badge_delete);

            // 最后一个item，减人按钮
            if (position == getCount() - 1 && isCreated) {
                tv_username.setText("");
                badge_delete.setVisibility(View.GONE);
                iv_avatar.setImageResource(R.drawable.icon_btn_deleteperson);

                iv_avatar.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(DiscussionDetailActivity.this, SelectFriendsActivity.class);
                        intent.putExtra("DeleteDiscuMember", (Serializable) memberList);
                        intent.putExtra("DeleteDiscuId", targetId);
                        startActivityForResult(intent, SealConst.DISCUSSION_REMOVE_MEMBER_REQUEST_CODE);
                    }

                });
            } else if ((isCreated && position == getCount() - 2) || (!isCreated && position == getCount() - 1)) {
                tv_username.setText("");
                badge_delete.setVisibility(View.GONE);
                iv_avatar.setImageResource(R.drawable.jy_drltsz_btn_addperson);

                iv_avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(DiscussionDetailActivity.this, SelectFriendsActivity.class);
                        intent.putExtra("AddDiscuMember", (Serializable) memberList);
                        intent.putExtra("AddDiscuId", targetId);
                        startActivityForResult(intent, SealConst.DISCUSSION_ADD_MEMBER_REQUEST_CODE);

                    }
                });
            } else { // 普通成员
                UserInfo bean = list.get(position);
                if (!TextUtils.isEmpty(bean.getName())) {
                    tv_username.setText(bean.getName());
                }
                if (TextUtils.isEmpty(bean.getPortraitUri().toString())) {
                    ImageLoader.getInstance().displayImage(RongGenerate.generateDefaultAvatar(bean.getName(), bean.getUserId()), iv_avatar, App.getOptions());
                } else {
                    ImageLoader.getInstance().displayImage(String.valueOf(bean.getPortraitUri()), iv_avatar, App.getOptions());
                }
                iv_avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }

                });

            }

            return convertView;
        }

        @Override
        public int getCount() {
            if (isCreated) {
                return list.size() + 2;
            } else {
                return list.size() + 1;
            }
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * 传入新的数据 刷新UI的方法
         */
        public void updateListView(List<UserInfo> list) {
            this.list = list;
            notifyDataSetChanged();
        }

    }


    // 拿到新增的成员刷新adapter
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case SealConst.DISCUSSION_ADD_MEMBER_REQUEST_CODE:
                    final List<String> addMember = (List<String>) data.getSerializableExtra("addDiscuMember");
                    RongIMClient.getInstance().addMemberToDiscussion(targetId, addMember, new RongIMClient.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            List<cn.rongcloud.im.db.Friend> list = DBManager.getInstance(mContext).getDaoSession().getFriendDao().loadAll();
                            for(Friend friend : list) {
                                for(String userId : addMember) {
                                    if(userId.equals(friend.getUserId()))
                                        memberList.add(new UserInfo(userId, friend.getName(), Uri.parse(friend.getPortraitUri())));
                                }
                            }
                            adapter.updateListView(memberList);
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {

                        }
                    });
                    break;
                case SealConst.DISCUSSION_REMOVE_MEMBER_REQUEST_CODE:
                    List<String> deleMember = (List<String>) data.getSerializableExtra("deleteDiscuMember");
                    List<UserInfo> filtered = new ArrayList<>();
                    for(String id : deleMember) {
                        int count = memberList.size();
                        for (int i = 0; i < count; i++) {
                            if (memberList.get(i).getUserId().equals(id))
                                filtered.add(memberList.get(i));
                        }
                    }
                    for (UserInfo userInfo : filtered) {
                        RongIMClient.getInstance().removeMemberFromDiscussion(targetId, userInfo.getUserId(), null);
                        memberList.remove(userInfo);
                    }
                    adapter.updateListView(memberList);
                    break;
            }
        }
    }

    @Override
    public Object doInBackground(int requsetCode, String id) throws HttpException {
        switch (requsetCode) {
            case FINDUSERINFO:
                return action.getUserInfos(ids);
        }
        return super.doInBackground(requsetCode, id);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        switch (requestCode) {
            case FINDUSERINFO:
                GetUserInfosResponse response = (GetUserInfosResponse) result;
                if (response.getCode() == 200) {
                    List<GetUserInfosResponse.ResultEntity> infos = response.getResult();
                    memberList.clear();
                    for (GetUserInfosResponse.ResultEntity g : infos) {
                        memberList.add(new UserInfo(g.getId(),g.getNickname(), Uri.parse(g.getPortraitUri())));
                    }
                    String loginid = getSharedPreferences("config", MODE_PRIVATE).getString("loginid", "");
                    if (loginid.equals(createId)) {
                        isCreated = true;
                    }
                    if (memberList != null && memberList.size() > 1) {
                        if(adapter == null) {
                            adapter = new GridAdapter(mContext, memberList);
                            gridview.setAdapter(adapter);
                        } else {
                            adapter.updateListView(memberList);
                        }
                    }
                    LoadDialog.dismiss(mContext);
                }
                break;
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        LoadDialog.dismiss(mContext);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
