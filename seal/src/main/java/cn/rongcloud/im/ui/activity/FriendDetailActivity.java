package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.pinyin.Friend;
import cn.rongcloud.im.server.response.AddToBlackListResponse;
import cn.rongcloud.im.server.response.GetBlackListResponse;
import cn.rongcloud.im.server.response.GetUserInfoByIdResponse;
import cn.rongcloud.im.server.response.RemoveFromBlackListResponse;
import cn.rongcloud.im.server.utils.RongGenerate;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.utils.OperationRong;
import cn.rongcloud.im.server.widget.DialogWithYesOrNoUtils;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.ui.widget.switchbutton.SwitchButton;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

/**
 * Created by AMing on 16/3/9.
 * Company RongCloud
 */
public class FriendDetailActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    private static final int ADDBLACKLIST = 88;
    private static final int REMOVEBLACKLIST = 89;
    private static final int GETBLACKLIST = 90;
    private static final int GETUSERINFO = 91;
    private Friend friend;

    private SwitchButton messageTop, messageNotif;

    private SelectableRoundedImageView mImageView;

    private Button startChat;

    private TextView friendName;

    private SwitchButton blackState;

    private boolean isBlackList, isFromConversation;

    private String fromConversationId;

    private LinearLayout cleanMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_fr_friend_detail);
        getSupportActionBar().setTitle(R.string.user_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        initView();
        fromConversationId = getIntent().getStringExtra("TargetId");
        if (!TextUtils.isEmpty(fromConversationId)) {
            isFromConversation = true;
            LoadDialog.show(mContext);
            request(GETUSERINFO);
        } else {
            //好友界面进入详情界面
            friend = (Friend) getIntent().getSerializableExtra("FriendDetails");
            initData();
            request(GETBLACKLIST);
            getState(friend);
        }

    }


    private void initData() {
        if (friend != null) {
            if (TextUtils.isEmpty(friend.getPortraitUri())) {
                ImageLoader.getInstance().displayImage(RongGenerate.generateDefaultAvatar(friend.getName(), friend.getUserId()), mImageView, App.getOptions());
            } else {
                ImageLoader.getInstance().displayImage(friend.getPortraitUri(), mImageView, App.getOptions());
            }
            friendName.setText(friend.getName());
        }
    }

    private void initView() {
        cleanMessage = (LinearLayout) findViewById(R.id.clean_friend);
        mImageView = (SelectableRoundedImageView) findViewById(R.id.friend_header);
        startChat = (Button) findViewById(R.id.start_friend_chat);
        messageTop = (SwitchButton) findViewById(R.id.sw_freind_top);
        messageNotif = (SwitchButton) findViewById(R.id.sw_friend_notfaction);
        friendName = (TextView) findViewById(R.id.friend_name);
        blackState = (SwitchButton) findViewById(R.id.black_state);
        startChat.setOnClickListener(this);
        blackState.setOnCheckedChangeListener(this);
        cleanMessage.setOnClickListener(this);
        messageNotif.setOnCheckedChangeListener(this);
        messageTop.setOnCheckedChangeListener(this);
    }


    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case ADDBLACKLIST:
                if (userInfo != null) {
                    return action.addToBlackList(userInfo.getId());
                }else {
                    return action.addToBlackList(friend.getUserId());
                }
            case REMOVEBLACKLIST:
                if (userInfo != null) {
                    return action.removeFromBlackList(userInfo.getId());
                }else {
                    return action.removeFromBlackList(friend.getUserId());
                }
            case GETBLACKLIST:
                return action.getBlackList();
            case GETUSERINFO:
                return action.getUserInfoById(fromConversationId);
        }
        return super.doInBackground(requestCode, id);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case ADDBLACKLIST:
                    AddToBlackListResponse response = (AddToBlackListResponse) result;
                    if (response.getCode() == 200) {
                        LoadDialog.dismiss(mContext);
                    }
                    break;
                case REMOVEBLACKLIST:
                    RemoveFromBlackListResponse response1 = (RemoveFromBlackListResponse) result;
                    if (response1.getCode() == 200) {
                        LoadDialog.dismiss(mContext);
                    }
                    break;
                case GETBLACKLIST:
                    GetBlackListResponse response2 = (GetBlackListResponse) result;
                    if (response2.getCode() == 200) {
                        if (response2.getResult().size() == 0) {
                            isBlackList = false;
                        } else {
                            if (userInfo != null) {
                                for (GetBlackListResponse.ResultEntity g : response2.getResult()) {
                                    if (userInfo.getId().contains(g.getUser().getId())) {
                                        isBlackList = true;
                                    }
                                }
                            } else if (friend != null) {
                                for (GetBlackListResponse.ResultEntity g : response2.getResult()) {
                                    if (friend.getUserId().contains(g.getUser().getId())) {
                                        isBlackList = true;
                                    }
                                }
                            }

                        }
                        blackState.setChecked(isBlackList);
                        LoadDialog.dismiss(mContext);
                    }
                    break;
                case GETUSERINFO:
                    GetUserInfoByIdResponse response3 = (GetUserInfoByIdResponse) result;
                    if (response3.getCode() == 200) {
                        userInfo = response3.getResult();

                        if (TextUtils.isEmpty(userInfo.getPortraitUri())) {
                            ImageLoader.getInstance().displayImage(RongGenerate.generateDefaultAvatar(userInfo.getNickname(), userInfo.getId()), mImageView, App.getOptions());
                        } else {
                            ImageLoader.getInstance().displayImage(userInfo.getPortraitUri(), mImageView, App.getOptions());
                        }
                        friendName.setText(userInfo.getNickname());
                        getState2(userInfo);
                        request(GETBLACKLIST);
                    }

                    break;
            }
        }
    }

    private GetUserInfoByIdResponse.ResultEntity userInfo;

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case ADDBLACKLIST:
                blackState.setChecked(false);
                NToast.shortToast(mContext, "加入失败");
                break;
            case REMOVEBLACKLIST:
                blackState.setChecked(true);
                NToast.shortToast(mContext, "移除失败");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clean_friend:
                DialogWithYesOrNoUtils.getInstance().showDialog(mContext, getString(R.string.clean_history), new DialogWithYesOrNoUtils.DialogCallBack() {
                    @Override
                    public void exectEvent() {
                        if (RongIM.getInstance() != null) {
                            if (friend != null) {
                                RongIM.getInstance().clearMessages(Conversation.ConversationType.PRIVATE, friend.getUserId(), new RongIMClient.ResultCallback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean aBoolean) {
                                        NToast.shortToast(mContext, getString(R.string.clear_success));
                                    }

                                    @Override
                                    public void onError(RongIMClient.ErrorCode errorCode) {
                                        NToast.shortToast(mContext, getString(R.string.clear_failure));
                                    }
                                });
                            } else if (userInfo != null) {
                                RongIM.getInstance().clearMessages(Conversation.ConversationType.PRIVATE, userInfo.getId(), new RongIMClient.ResultCallback<Boolean>() {
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

            case R.id.start_friend_chat:

                if (RongIM.getInstance() != null) {
                    if (friend != null) {
                        RongIM.getInstance().startPrivateChat(mContext, friend.getUserId(), friend.getName());
                    } else if (userInfo != null) {
                        RongIM.getInstance().startPrivateChat(mContext, userInfo.getId(), userInfo.getNickname());
                    }

                    finish();
                }

                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sw_friend_notfaction:
                if (isChecked) {
                    if (userInfo != null) {
                        OperationRong.setConverstionNotif(mContext, Conversation.ConversationType.PRIVATE, userInfo.getId(), true);
                    } else if (friend != null) {
                        OperationRong.setConverstionNotif(mContext, Conversation.ConversationType.PRIVATE, friend.getUserId(), true);
                    }
                } else {
                    if (userInfo != null) {
                        OperationRong.setConverstionNotif(mContext, Conversation.ConversationType.PRIVATE, userInfo.getId(), false);
                    } else if (friend != null) {
                        OperationRong.setConverstionNotif(mContext, Conversation.ConversationType.PRIVATE, friend.getUserId(), false);
                    }
                }
                break;
            case R.id.sw_freind_top:
                if (isChecked) {
                    if (userInfo != null) {
                        OperationRong.setConversationTop(mContext, Conversation.ConversationType.PRIVATE, userInfo.getId(), true);
                    } else if (friend != null) {
                        OperationRong.setConversationTop(mContext, Conversation.ConversationType.PRIVATE, friend.getUserId(), true);
                    }
                } else {
                    if (userInfo != null) {
                        OperationRong.setConversationTop(mContext, Conversation.ConversationType.PRIVATE, userInfo.getId(), false);
                    } else if (friend != null) {
                        OperationRong.setConversationTop(mContext, Conversation.ConversationType.PRIVATE, friend.getUserId(), false);
                    }
                }
                break;
            case R.id.black_state:
                if (isChecked) {
                    LoadDialog.show(mContext);
                    request(ADDBLACKLIST);
                }else {
                    LoadDialog.show(mContext);
                    request(REMOVEBLACKLIST);
                }
                break;


        }
    }

    private void getState(Friend friend) {
        if (friend != null) {//群组列表 page 进入
            if (RongIM.getInstance() != null) {
                RongIM.getInstance().getConversation(Conversation.ConversationType.PRIVATE, friend.getUserId(), new RongIMClient.ResultCallback<Conversation>() {
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

                RongIM.getInstance().getConversationNotificationStatus(Conversation.ConversationType.PRIVATE, friend.getUserId(), new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
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
        }
    }

    private void getState2(GetUserInfoByIdResponse.ResultEntity friend) {
        if (friend != null) {//群组列表 page 进入
            if (RongIM.getInstance() != null) {
                RongIM.getInstance().getConversation(Conversation.ConversationType.PRIVATE, friend.getId(), new RongIMClient.ResultCallback<Conversation>() {
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

                RongIM.getInstance().getConversationNotificationStatus(Conversation.ConversationType.PRIVATE, friend.getId(), new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
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
        }
    }
}
