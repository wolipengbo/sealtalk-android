package cn.rongcloud.im.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.db.DBManager;
import cn.rongcloud.im.db.Groups;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.pinyin.Friend;
import cn.rongcloud.im.server.response.DismissGroupResponse;
import cn.rongcloud.im.server.response.GetGroupInfoResponse;
import cn.rongcloud.im.server.response.GetGroupMemberResponse;
import cn.rongcloud.im.server.response.QuitGroupResponse;
import cn.rongcloud.im.server.response.SetGroupDisplayNameResponse;
import cn.rongcloud.im.server.utils.CommonUtils;
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

/**
 * Created by AMing on 16/1/27.
 * Company RongCloud
 */
public class NewGroupDetailActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final int GETGROUPMEMBER = 20;
    private static final int DISMISSGROUP = 26;
    private static final int QUITGROUP = 27;
    private static final int SETGROUPNAME = 29;
    private static final int GETGROUPINFO = 30;
    private static final int GETGROUPINFO2 = 31;


    private boolean isCreated;

    private DemoGridView gridview;

    private List<GetGroupMemberResponse.ResultEntity> mGroupMember;

    private GridAdapter adapter;

    private TextView mTextViewMemberSize, mGroupName, mGroupDisplayNameText;

    private ImageView showIcon;

    private SelectableRoundedImageView mGroupHeader;

    private LinearLayout mGroupInfo, mGroupDisplayName, groupClean;

    private Button mQuitBtn, mDismissBtn, mStartGroupChat;
    private String groupDisplayNmae;

    private SwitchButton messageTop, messageNotif;
    private GetGroupInfoResponse.ResultEntity mGroup;
    private String fromConversationId;
    private boolean isFromConversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_group);

        messageTop = (SwitchButton) findViewById(R.id.sw_group_top);
        messageNotif = (SwitchButton) findViewById(R.id.sw_group_notfaction);
        messageTop.setOnCheckedChangeListener(this);
        messageNotif.setOnCheckedChangeListener(this);
        groupClean = (LinearLayout) findViewById(R.id.group_clean);
        gridview = (DemoGridView) findViewById(R.id.gridview);
        mTextViewMemberSize = (TextView) findViewById(R.id.group_member_size);
        mGroupName = (TextView) findViewById(R.id.group_name);
        mGroupInfo = (LinearLayout) findViewById(R.id.group_info);
        mGroupHeader = (SelectableRoundedImageView) findViewById(R.id.group_header);
        showIcon = (ImageView) findViewById(R.id.show_icon);
        mGroupDisplayName = (LinearLayout) findViewById(R.id.group_displayname);
        mGroupDisplayNameText = (TextView) findViewById(R.id.group_displayname_text);
        mStartGroupChat = (Button) findViewById(R.id.start_group_chat);
        mQuitBtn = (Button) findViewById(R.id.group_quit);
        mDismissBtn = (Button) findViewById(R.id.group_dismiss);
        mGroupDisplayName.setOnClickListener(this);
        mStartGroupChat.setOnClickListener(this);
        mQuitBtn.setOnClickListener(this);
        mDismissBtn.setOnClickListener(this);
        groupClean.setOnClickListener(this);
        getSupportActionBar().setTitle(R.string.group_info);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);

        //群组会话界面点进群组详情
        fromConversationId = getIntent().getStringExtra("TargetId");
        if (!TextUtils.isEmpty(fromConversationId)) {
            isFromConversation = true;
        }

        if (isFromConversation) {//群组会话页进入
            LoadDialog.show(mContext);
            request(GETGROUPINFO2);
        }


        BroadcastManager.getInstance(mContext).addAction(ChangeGroupInfoActivity.UPDATEGROUPINFONAME, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getAction();
                if (!TextUtils.isEmpty(command)) {
                    String s = intent.getStringExtra("String");
                    mGroupName.setText(s);
                }
            }
        });

    }


    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case GETGROUPMEMBER:
                return action.getGroupMember(fromConversationId);
            case QUITGROUP:
                return action.quitGroup(fromConversationId);
            case DISMISSGROUP:
                return action.dissmissGroup(fromConversationId);
            case SETGROUPNAME:
                return action.setGroupDisplayName(fromConversationId, groupDisplayNmae);
            case GETGROUPINFO:
                return action.getGroupInfo(fromConversationId);
            case GETGROUPINFO2:
                return action.getGroupInfo(fromConversationId);
        }
        return super.doInBackground(requestCode, id);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case GETGROUPMEMBER:
                    GetGroupMemberResponse res = (GetGroupMemberResponse) result;
                    if (res.getCode() == 200) {
                        mGroupMember = res.getResult();
                        if (mGroupMember != null && mGroupMember.size() > 0) {
                            mTextViewMemberSize.setText(getString(R.string.group_member_size)+"(" + mGroupMember.size() + ")");
                            adapter = new GridAdapter(mContext, mGroupMember);
                            gridview.setAdapter(adapter);
                        }

                        for (GetGroupMemberResponse.ResultEntity g : mGroupMember) {
                            if (g.getUser().getId().equals(getSharedPreferences("config", MODE_PRIVATE).getString("loginid", ""))) {
                                if (!TextUtils.isEmpty(g.getDisplayName())) {
                                    mGroupDisplayNameText.setText(g.getDisplayName());
                                } else {
                                    mGroupDisplayNameText.setText("无");
                                }
                            }
                        }

                        if (isCreated) {
                            mDismissBtn.setVisibility(View.VISIBLE);
                            showIcon.setVisibility(View.VISIBLE);
                            mGroupInfo.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(NewGroupDetailActivity.this, ChangeGroupInfoActivity.class);
                                    if (isFromConversation) {
                                        intent.putExtra("GroupInfo", (Serializable) new Groups(mGroup.getId(), mGroup.getName(), mGroup.getPortraitUri()));
                                    }
                                    startActivityForResult(intent, 102);
                                }
                            });
                        }
                        LoadDialog.dismiss(mContext);
                    }
                    break;
                case QUITGROUP:
                    QuitGroupResponse response = (QuitGroupResponse) result;
                    if (response.getCode() == 200) {
                        BroadcastManager.getInstance(mContext).sendBroadcast(SealAppContext.NETUPDATEGROUP);
                            if (RongIM.getInstance().getConversation(Conversation.ConversationType.GROUP, fromConversationId) != null) {
                                RongIM.getInstance().removeConversation(Conversation.ConversationType.GROUP, fromConversationId);
                                RongIM.getInstance().clearMessages(Conversation.ConversationType.GROUP, fromConversationId);
                            }
                        NToast.shortToast(mContext, getString(R.string.quit_success));
                        LoadDialog.dismiss(mContext);
                        finish();
                    }
                    break;

                case DISMISSGROUP:
                    DismissGroupResponse response1 = (DismissGroupResponse) result;
                    if (response1.getCode() == 200) {
                        BroadcastManager.getInstance(mContext).sendBroadcast(SealAppContext.NETUPDATEGROUP);
                            if (RongIM.getInstance().getConversation(Conversation.ConversationType.GROUP, fromConversationId) != null) {
                                RongIM.getInstance().removeConversation(Conversation.ConversationType.GROUP, fromConversationId);
                                RongIM.getInstance().clearMessages(Conversation.ConversationType.GROUP, fromConversationId);
                            }
                        NToast.shortToast(mContext, getString(R.string.dismiss_success));
                        LoadDialog.dismiss(mContext);
                        finish();
                    }
                    break;

                case SETGROUPNAME:
                    SetGroupDisplayNameResponse response2 = (SetGroupDisplayNameResponse) result;
                    if (response2.getCode() == 200) {
                        request(GETGROUPINFO);
                    }
                    break;
                case GETGROUPINFO:
                    GetGroupInfoResponse response3 = (GetGroupInfoResponse) result;
                    if (response3.getCode() == 200) {
                        int i;
                        if (isCreated) {
                            i = 0;
                        } else {
                            i = 1;
                        }
                        GetGroupInfoResponse.ResultEntity bean = response3.getResult();
                        DBManager.getInstance(mContext).getDaoSession().getGroupsDao().insertOrReplace(
                                new Groups(bean.getId(), bean.getName(), bean.getPortraitUri(), groupDisplayNmae, String.valueOf(i), null)
                        );
                        mGroupDisplayNameText.setText(groupDisplayNmae);
                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(mContext, "修改成功");
                    }
                    break;
                case GETGROUPINFO2:
                    GetGroupInfoResponse response4 = (GetGroupInfoResponse) result;
                    if (response4.getCode() == 200) {
                        if (response4.getResult() != null) {
                            mGroup = response4.getResult();
                            mGroupName.setText(response4.getResult().getName());
                            if (TextUtils.isEmpty(response4.getResult().getPortraitUri())) {
                                ImageLoader.getInstance().displayImage(RongGenerate.generateDefaultAvatar(response4.getResult().getName(), response4.getResult().getId()), mGroupHeader, App.getOptions());
                            } else {
                                ImageLoader.getInstance().displayImage(response4.getResult().getPortraitUri(), mGroupHeader, App.getOptions());
                            }


                            if (RongIM.getInstance() != null) {
                                RongIM.getInstance().getConversation(Conversation.ConversationType.GROUP, mGroup.getId(), new RongIMClient.ResultCallback<Conversation>() {
                                    @Override
                                    public void onSuccess(Conversation conversation) {
                                        if (conversation == null) {
                                            return;
                                        }
                                        if (conversation.isTop()) {
                                            messageTop.setChecked(true);
                                        } else {
                                            messageTop.setChecked(false);
                                        }

                                    }

                                    @Override
                                    public void onError(RongIMClient.ErrorCode errorCode) {

                                    }
                                });

                                RongIM.getInstance().getConversationNotificationStatus(Conversation.ConversationType.GROUP, mGroup.getId(), new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
                                    @Override
                                    public void onSuccess(Conversation.ConversationNotificationStatus conversationNotificationStatus) {

                                        if (conversationNotificationStatus == Conversation.ConversationNotificationStatus.DO_NOT_DISTURB ? true : false) {
                                            messageNotif.setChecked(true);
                                        } else {
                                            messageNotif.setChecked(false);
                                        }
                                    }

                                    @Override
                                    public void onError(RongIMClient.ErrorCode errorCode) {

                                    }
                                });
                            }


                            SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
                            if (sp.getString("loginid", "").equals(response4.getResult().getCreatorId())) {
                                isCreated = true;
                            }

                            request(GETGROUPMEMBER);
                        }
                    }
                    break;
            }
        }
    }


    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case GETGROUPMEMBER:
                NToast.shortToast(mContext, "获取群组成员请求失败");
                LoadDialog.dismiss(mContext);
                break;
            case QUITGROUP:
                NToast.shortToast(mContext, "退出群组请求失败");
                LoadDialog.dismiss(mContext);
                break;
            case DISMISSGROUP:
                NToast.shortToast(mContext, "解散群组请求失败");
                LoadDialog.dismiss(mContext);
                break;
            case GETGROUPINFO2:
                LoadDialog.dismiss(mContext);
                if (!CommonUtils.isNetworkConnected(mContext)) {
                    NToast.shortToast(mContext,"网络不可用");
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_group_chat:
                if (RongIM.getInstance() != null) {
                    if (mGroup != null) {
                        RongIM.getInstance().startGroupChat(mContext, mGroup.getId(), mGroup.getName());
                    }

                    finish();
                }
                break;
            case R.id.group_displayname:
                DialogWithYesOrNoUtils.getInstance().showEditDialog(mContext, getString(R.string.group_name), getString(R.string.confirm), new DialogWithYesOrNoUtils.DialogCallBack() {
                    @Override
                    public void exectEvent() {

                    }

                    @Override
                    public void exectEditEvent(String editText) {
                        if (TextUtils.isEmpty(editText)) {
                            return;
                        }
                        groupDisplayNmae = editText;
                        LoadDialog.show(mContext);
                        request(SETGROUPNAME);
                    }

                    @Override
                    public void updatePassword(String oldPassword, String newPassword) {

                    }
                });

                break;
            case R.id.group_quit:
                DialogWithYesOrNoUtils.getInstance().showDialog(mContext, getString(R.string.confirm_quit_group), new DialogWithYesOrNoUtils.DialogCallBack() {
                    @Override
                    public void exectEvent() {
                        LoadDialog.show(mContext);
                        request(QUITGROUP);
                    }

                    @Override
                    public void exectEditEvent(String editText) {

                    }

                    @Override
                    public void updatePassword(String oldPassword, String newPassword) {

                    }
                });
                break;
            case R.id.group_dismiss:
                DialogWithYesOrNoUtils.getInstance().showDialog(mContext, getString(R.string.confirm_dismiss_group), new DialogWithYesOrNoUtils.DialogCallBack() {
                    @Override
                    public void exectEvent() {
                        LoadDialog.show(mContext);
                        request(DISMISSGROUP);
                    }

                    @Override
                    public void exectEditEvent(String editText) {

                    }

                    @Override
                    public void updatePassword(String oldPassword, String newPassword) {

                    }
                });
                break;
            case R.id.group_clean:
                DialogWithYesOrNoUtils.getInstance().showDialog(mContext, getString(R.string.clean_history), new DialogWithYesOrNoUtils.DialogCallBack() {
                    @Override
                    public void exectEvent() {
                        if (RongIM.getInstance() != null) {
                            if (mGroup != null) {
                                RongIM.getInstance().clearMessages(Conversation.ConversationType.GROUP, mGroup.getId(), new RongIMClient.ResultCallback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean aBoolean) {
                                        NToast.shortToast(mContext, getString(R.string.clear_success));
                                    }

                                    @Override
                                    public void onError(RongIMClient.ErrorCode errorCode) {
                                        NToast.shortToast(mContext, getString(R.string.clear_failure));
                                    }
                                });
                            }
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
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sw_group_top:
                if (isChecked) {
                    if (mGroup != null) {
                        OperationRong.setConversationTop(mContext, Conversation.ConversationType.GROUP, mGroup.getId(), true);
                    }
                } else {
                    if (mGroup != null) {
                        OperationRong.setConversationTop(mContext, Conversation.ConversationType.GROUP, mGroup.getId(), false);
                    }
                }
                break;
            case R.id.sw_group_notfaction:
                if (isChecked) {
                    if (mGroup != null) {
                        OperationRong.setConverstionNotif(mContext, Conversation.ConversationType.GROUP, mGroup.getId(), true);
                    }
                } else {
                    if (mGroup != null) {
                        OperationRong.setConverstionNotif(mContext, Conversation.ConversationType.GROUP, mGroup.getId(), false);
                    }
                }

                break;
        }
    }


    private class GridAdapter extends BaseAdapter {

        private List<GetGroupMemberResponse.ResultEntity> list;
        Context context;


        public GridAdapter(Context context, List<GetGroupMemberResponse.ResultEntity> list) {
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
                        Intent intent = new Intent(NewGroupDetailActivity.this, SelectFriendsActivity.class);
                        intent.putExtra("DeleteGroupMember", (Serializable) mGroupMember);
                        if (isFromConversation) {
                            intent.putExtra("DeleteGroupId", mGroup.getId());
                        }
                        startActivityForResult(intent, 101);
                    }

                });
            } else if ((isCreated && position == getCount() - 2) || (!isCreated && position == getCount() - 1)) {
                tv_username.setText("");
                badge_delete.setVisibility(View.GONE);
                iv_avatar.setImageResource(R.drawable.jy_drltsz_btn_addperson);

                iv_avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(NewGroupDetailActivity.this, SelectFriendsActivity.class);
                        intent.putExtra("AddGroupMember", (Serializable) mGroupMember);
                        if (isFromConversation) {
                            intent.putExtra("GroupId", mGroup.getId());
                        }
                        startActivityForResult(intent, 100);

                    }
                });
            } else { // 普通成员
                GetGroupMemberResponse.ResultEntity bean = list.get(position);
                if (!TextUtils.isEmpty(bean.getDisplayName())) {
                    tv_username.setText(bean.getDisplayName());
                } else {
                    tv_username.setText(bean.getUser().getNickname());
                }
                if (TextUtils.isEmpty(bean.getUser().getPortraitUri())) {
                    ImageLoader.getInstance().displayImage(RongGenerate.generateDefaultAvatar(bean.getUser().getNickname(), bean.getUser().getId()), iv_avatar, App.getOptions());
                } else {
                    ImageLoader.getInstance().displayImage(bean.getUser().getPortraitUri(), iv_avatar, App.getOptions());
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
        public void updateListView(List<GetGroupMemberResponse.ResultEntity> list) {
            this.list = list;
            notifyDataSetChanged();
        }

    }


    // 拿到新增的成员刷新adapter
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            List<Friend> newMemberData = (List<Friend>) data.getSerializableExtra("newAddMember");
            List<Friend> deleMember = (List<Friend>) data.getSerializableExtra("deleteMember");
            if (newMemberData != null && newMemberData.size() > 0) {
                request(GETGROUPMEMBER);
            } else if (deleMember != null && deleMember.size() > 0) {
                request(GETGROUPMEMBER);
            }

        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        BroadcastManager.getInstance(mContext).destroy(ChangeGroupInfoActivity.UPDATEGROUPINFONAME);
        BroadcastManager.getInstance(mContext).destroy(ChangeGroupInfoActivity.UPDATEGROUPINFOIMG);
    }
}
