package cn.rongcloud.im;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import cn.rongcloud.im.db.DBManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.message.AgreedFriendRequestMessage;
import cn.rongcloud.im.message.provider.RealTimeLocationInputProvider;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.ContactNotificationMessageData;
import cn.rongcloud.im.server.utils.NLog;
import cn.rongcloud.im.server.utils.RongGenerate;
import cn.rongcloud.im.server.utils.json.JsonMananger;
import cn.rongcloud.im.ui.activity.AMAPLocationActivity;
import cn.rongcloud.im.ui.activity.NewFriendListActivity;
import cn.rongcloud.im.ui.activity.RealTimeLocationActivity;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.GroupUserInfo;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.widget.AlterDialogFragment;
import io.rong.imkit.widget.provider.ImageInputProvider;
import io.rong.imkit.widget.provider.InputProvider;
import io.rong.imkit.widget.provider.LocationInputProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.location.RealTimeLocationConstant;
import io.rong.imlib.location.message.RealTimeLocationStartMessage;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.ContactNotificationMessage;
import io.rong.message.GroupNotificationMessage;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;

/**
 * 融云相关监听 事件集合类
 * Created by AMing on 16/1/7.
 * Company RongCloud
 */
public class SealAppContext implements RongIM.ConversationListBehaviorListener, RongIMClient.OnReceiveMessageListener, RongIM.UserInfoProvider, RongIM.GroupInfoProvider, RongIM.GroupUserInfoProvider, RongIMClient.ConnectionStatusListener, RongIM.LocationProvider, RongIM.ConversationBehaviorListener {


    public static final String UPDATEFRIEND = "updatefriend";
    public static final String UPDATEREDDOT = "updatereddot";
    public static String NETUPDATEGROUP = "netupdategroup";
    private Context mContext;

    private static SealAppContext mRongCloudInstance;

    private RongIM.LocationProvider.LocationCallback mLastLocationCallback;

    public SealAppContext(Context mContext) {
        this.mContext = mContext;
        initListener();
    }

    /**
     * 初始化 RongCloud.
     *
     * @param context 上下文。
     */
    public static void init(Context context) {

        if (mRongCloudInstance == null) {

            synchronized (SealAppContext.class) {

                if (mRongCloudInstance == null) {
                    mRongCloudInstance = new SealAppContext(context);
                }
            }
        }

    }

    /**
     * 获取RongCloud 实例。
     *
     * @return RongCloud。
     */
    public static SealAppContext getInstance() {
        return mRongCloudInstance;
    }

    /**
     * init 后就能设置的监听
     */
    private void initListener() {
        RongIM.setConversationBehaviorListener(this);//设置会话界面操作的监听器。
        RongIM.setConversationListBehaviorListener(this);
        RongIM.setUserInfoProvider(this, true);
        RongIM.setGroupInfoProvider(this, true);
        RongIM.setLocationProvider(this);//设置地理位置提供者,不用位置的同学可以注掉此行代码
        setInputProvider();
        setUserInfoEngineListener();
//        RongIM.setGroupUserInfoProvider(this, true);
    }

    private void setInputProvider() {

        RongIM.setOnReceiveMessageListener(this);
        RongIM.setConnectionStatusListener(this);

        InputProvider.ExtendProvider[] singleProvider = {
                new ImageInputProvider(RongContext.getInstance()),
                new RealTimeLocationInputProvider(RongContext.getInstance()) //带位置共享的地理位置
        };

        InputProvider.ExtendProvider[] muiltiProvider = {
                new ImageInputProvider(RongContext.getInstance()),
                new LocationInputProvider(RongContext.getInstance()),//地理位置
        };

        RongIM.resetInputExtensionProvider(Conversation.ConversationType.PRIVATE, singleProvider);
        RongIM.resetInputExtensionProvider(Conversation.ConversationType.DISCUSSION, muiltiProvider);
        RongIM.resetInputExtensionProvider(Conversation.ConversationType.CUSTOMER_SERVICE, muiltiProvider);
        RongIM.resetInputExtensionProvider(Conversation.ConversationType.GROUP, muiltiProvider);
    }

    /**
     * 需要 rongcloud connect 成功后设置的 listener
     */
    public void setUserInfoEngineListener() {
        UserInfoEngine.getInstance(mContext).setListener(new UserInfoEngine.UserInfoListener() {
            @Override
            public void onResult(UserInfo info) {
                if (info != null && RongIM.getInstance() != null) {
                    if (TextUtils.isEmpty(String.valueOf(info.getPortraitUri()))) {
                        info.setPortraitUri(Uri.parse(RongGenerate.generateDefaultAvatar(info.getName(), info.getUserId())));
                    }
                    NLog.e("UserInfoEngine", info.getName() + info.getPortraitUri());
                    RongIM.getInstance().refreshUserInfoCache(info);
                }
            }
        });
        GroupInfoEngine.getInstance(mContext).setmListener(new GroupInfoEngine.GroupInfoListeners() {
            @Override
            public void onResult(Group info) {
                if (info != null && RongIM.getInstance() != null) {
                    NLog.e("GroupInfoEngine:" + info.getId() + "----" + info.getName() + "----" + info.getPortraitUri());
                    if (TextUtils.isEmpty(String.valueOf(info.getPortraitUri()))) {
                        info.setPortraitUri(Uri.parse(RongGenerate.generateDefaultAvatar(info.getName(), info.getId())));
                    }
                    RongIM.getInstance().refreshGroupInfoCache(info);

                }
            }
        });
    }

    @Override
    public boolean onConversationPortraitClick(Context context, Conversation.ConversationType conversationType, String s) {
        return false;
    }

    @Override
    public boolean onConversationPortraitLongClick(Context context, Conversation.ConversationType conversationType, String s) {
        return false;
    }

    @Override
    public boolean onConversationLongClick(Context context, View view, UIConversation uiConversation) {
        return false;
    }

    @Override
    public boolean onConversationClick(Context context, View view, UIConversation uiConversation) {
        MessageContent messageContent = uiConversation.getMessageContent();
        if (messageContent instanceof ContactNotificationMessage) {
            ContactNotificationMessage contactNotificationMessage = (ContactNotificationMessage) messageContent;
            if (contactNotificationMessage.getOperation().equals("AcceptResponse")) {
                // 被加方同意请求后
                if (contactNotificationMessage.getExtra() != null) {
                    ContactNotificationMessageData bean = null;
                    try {
                        bean = JsonMananger.jsonToBean(contactNotificationMessage.getExtra(), ContactNotificationMessageData.class);
                    } catch (HttpException e) {
                        e.printStackTrace();
                    }
                    RongIM.getInstance().startPrivateChat(context, uiConversation.getConversationSenderId(), bean.getSourceUserNickname());

                }
            } else {
                context.startActivity(new Intent(context, NewFriendListActivity.class));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onReceived(Message message, int i) {
        MessageContent messageContent = message.getContent();
        if (messageContent instanceof ContactNotificationMessage) {
            ContactNotificationMessage contactNotificationMessage = (ContactNotificationMessage) messageContent;
            if (contactNotificationMessage.getOperation().equals("Request")) {
                //对方发来好友邀请
                BroadcastManager.getInstance(mContext).sendBroadcast(SealAppContext.UPDATEREDDOT);
            } else if (contactNotificationMessage.getOperation().equals("AcceptResponse")) {
                //对方同意我的好友请求
                ContactNotificationMessageData c = null;
                try {
                    c = JsonMananger.jsonToBean(contactNotificationMessage.getExtra(), ContactNotificationMessageData.class);
                } catch (HttpException e) {
                    e.printStackTrace();
                }
                if (c != null) {
                    DBManager.getInstance(mContext).getDaoSession().getFriendDao().insertOrReplace(new Friend(contactNotificationMessage.getSourceUserId(), c.getSourceUserNickname(), null, null, null, null));
                }
                BroadcastManager.getInstance(mContext).sendBroadcast(UPDATEFRIEND);
                BroadcastManager.getInstance(mContext).sendBroadcast(SealAppContext.UPDATEREDDOT);
            }
//                // 发广播通知更新好友列表
//            BroadcastManager.getInstance(mContext).sendBroadcast(UPDATEREDDOT);
//            }
        } else if (messageContent instanceof AgreedFriendRequestMessage) {//好友添加成功消息
            AgreedFriendRequestMessage agreedFriendRequestMessage = (AgreedFriendRequestMessage) messageContent;
            if (agreedFriendRequestMessage.getUserInfo() != null) {
                UserInfo bean = agreedFriendRequestMessage.getUserInfo();
                DBManager.getInstance(mContext).getDaoSession().getFriendDao().insertOrReplace(
                        new Friend(bean.getUserId(), bean.getName(), String.valueOf(bean.getPortraitUri()), null, null, null)
                );
            } else {
                DBManager.getInstance(mContext).getDaoSession().getFriendDao().insertOrReplace(
                        new Friend(agreedFriendRequestMessage.getFriendId())
                );
                RongIM.getInstance().refreshUserInfoCache(new UserInfo(agreedFriendRequestMessage.getUserInfo().getUserId(),
                        agreedFriendRequestMessage.getUserInfo().getName(),
                        agreedFriendRequestMessage.getUserInfo().getPortraitUri()
                ));
            }
        } else if (messageContent instanceof GroupNotificationMessage) {
            GroupNotificationMessage groupNotificationMessage = (GroupNotificationMessage) messageContent;
            NLog.e("" + groupNotificationMessage.getMessage());
            if (groupNotificationMessage.getOperation().equals("Kicked")) {
            } else if (groupNotificationMessage.getOperation().equals("Add")) {
            } else if (groupNotificationMessage.getOperation().equals("Quit")) {
            } else if (groupNotificationMessage.getOperation().equals("Rename")) {
            }

            BroadcastManager.getInstance(mContext).sendBroadcast(SealAppContext.NETUPDATEGROUP);
        } else if (messageContent instanceof ImageMessage) {
            ImageMessage imageMessage = (ImageMessage) messageContent;
            Log.e("imageMessage", imageMessage.getRemoteUri().toString());
        }
        return false;
    }

    @Override
    public UserInfo getUserInfo(String s) {
        NLog.e("Rongcloudevent : getUserInfo:" + s);
        return UserInfoEngine.getInstance(mContext).startEngine(s);
    }

    @Override
    public Group getGroupInfo(String s) {
        NLog.e("Rongcloudevent : getGroupInfo:" + s);
        return GroupInfoEngine.getInstance(mContext).startEngine(s);
    }

    @Override
    public GroupUserInfo getGroupUserInfo(String groupId, String userId) {
//        return GroupUserInfoEngine.getInstance(mContext).startEngine(groupId, userId);
        return null;
    }

    @Override
    public void onChanged(ConnectionStatus connectionStatus) {
        Log.e("onChanged", "onChanged");
        if (connectionStatus.getMessage().equals(ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT)) {

        }
    }

    @Override
    public void onStartLocation(Context context, LocationCallback locationCallback) {
        /**
         * demo 代码  开发者需替换成自己的代码。
         */
        SealAppContext.getInstance().setLastLocationCallback(locationCallback);
        Intent intent = new Intent(context, AMAPLocationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

    }

    @Override
    public boolean onUserPortraitClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo) {
        return false;
    }

    @Override
    public boolean onUserPortraitLongClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo) {
        return false;
    }

    @Override
    public boolean onMessageClick(final Context context, final View view, final Message message) {

        //real-time location message begin
        if (message.getContent() instanceof RealTimeLocationStartMessage) {
            RealTimeLocationConstant.RealTimeLocationStatus status = RongIMClient.getInstance().getRealTimeLocationCurrentState(message.getConversationType(), message.getTargetId());

//            if (status == RealTimeLocationConstant.RealTimeLocationStatus.RC_REAL_TIME_LOCATION_STATUS_IDLE) {
//                startRealTimeLocation(context, message.getConversationType(), message.getTargetId());
//            } else
            if (status == RealTimeLocationConstant.RealTimeLocationStatus.RC_REAL_TIME_LOCATION_STATUS_INCOMING) {


                final AlterDialogFragment alterDialogFragment = AlterDialogFragment.newInstance("", "加入位置共享", "取消", "加入");
                alterDialogFragment.setOnAlterDialogBtnListener(new AlterDialogFragment.AlterDialogBtnListener() {

                    @Override
                    public void onDialogPositiveClick(AlterDialogFragment dialog) {
                        RealTimeLocationConstant.RealTimeLocationStatus status = RongIMClient.getInstance().getRealTimeLocationCurrentState(message.getConversationType(), message.getTargetId());

                        if (status == null || status == RealTimeLocationConstant.RealTimeLocationStatus.RC_REAL_TIME_LOCATION_STATUS_IDLE) {
                            startRealTimeLocation(context, message.getConversationType(), message.getTargetId());
                        } else {
                            joinRealTimeLocation(context, message.getConversationType(), message.getTargetId());
                        }

                    }

                    @Override
                    public void onDialogNegativeClick(AlterDialogFragment dialog) {
                        alterDialogFragment.dismiss();
                    }
                });

                alterDialogFragment.show(((FragmentActivity) context).getSupportFragmentManager());
            } else {

                if (status != null && (status == RealTimeLocationConstant.RealTimeLocationStatus.RC_REAL_TIME_LOCATION_STATUS_OUTGOING || status == RealTimeLocationConstant.RealTimeLocationStatus.RC_REAL_TIME_LOCATION_STATUS_CONNECTED)) {

                    Intent intent = new Intent(((FragmentActivity) context), RealTimeLocationActivity.class);
                    intent.putExtra("conversationType", message.getConversationType().getValue());
                    intent.putExtra("targetId", message.getTargetId());
                    context.startActivity(intent);
                }
            }
            return true;
        }

        //real-time location message end
        /**
         * demo 代码  开发者需替换成自己的代码。
         */
        if (message.getContent() instanceof LocationMessage) {
            Intent intent = new Intent(context, AMAPLocationActivity.class);
            intent.putExtra("location", message.getContent());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else if (message.getContent() instanceof ImageMessage) {
//            Intent intent = new Intent(context, PhotoActivity.class);
//            intent.putExtra("message", message);
//            context.startActivity(intent);
        }

        return false;
    }


    private void startRealTimeLocation(Context context, Conversation.ConversationType conversationType, String targetId) {
        RongIMClient.getInstance().startRealTimeLocation(conversationType, targetId);

        Intent intent = new Intent(((FragmentActivity) context), RealTimeLocationActivity.class);
        intent.putExtra("conversationType", conversationType.getValue());
        intent.putExtra("targetId", targetId);
        context.startActivity(intent);
    }

    private void joinRealTimeLocation(Context context, Conversation.ConversationType conversationType, String targetId) {
        RongIMClient.getInstance().joinRealTimeLocation(conversationType, targetId);

        Intent intent = new Intent(((FragmentActivity) context), RealTimeLocationActivity.class);
        intent.putExtra("conversationType", conversationType.getValue());
        intent.putExtra("targetId", targetId);
        context.startActivity(intent);
    }

    @Override
    public boolean onMessageLinkClick(Context context, String s) {
        return false;
    }

    @Override
    public boolean onMessageLongClick(Context context, View view, Message message) {
        return false;
    }


    public RongIM.LocationProvider.LocationCallback getLastLocationCallback() {
        return mLastLocationCallback;
    }

    public void setLastLocationCallback(RongIM.LocationProvider.LocationCallback lastLocationCallback) {
        this.mLastLocationCallback = lastLocationCallback;
    }
}
