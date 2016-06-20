package cn.rongcloud.im.message.provider;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.rongcloud.im.R;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.ContactNotificationMessageData;
import cn.rongcloud.im.server.utils.json.JsonMananger;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.widget.ArraysDialogFragment;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.PublicServiceProfile;
import io.rong.imlib.model.UserInfo;
import io.rong.message.ContactNotificationMessage;

/**
 * Created by Bob on 2015/4/17.
 * 如何自定义消息模板
 */
@ProviderTag(messageContent = ContactNotificationMessage.class, showPortrait = false, centerInHorizontal = true, showProgress = false)
public class ContactNotificationMessageProvider extends IContainerItemProvider.MessageProvider<ContactNotificationMessage> {
    @Override
    public void bindView(View v, int position, ContactNotificationMessage content, UIMessage message) {
        ViewHolder viewHolder = (ViewHolder) v.getTag();
        if (content != null) {
            if (!TextUtils.isEmpty(content.getExtra())) {
                ContactNotificationMessageData bean = null;
                try {
                    bean = JsonMananger.jsonToBean(content.getExtra(), ContactNotificationMessageData.class);
                } catch (HttpException e) {
                    e.printStackTrace();
                }
                if (content.getOperation().equals("AcceptResponse")) {
                    viewHolder.contentTextView.setText(bean.getSourceUserNickname() + "已同意你的好友请求");
                }
                if (content.getOperation().equals("Request")) {
                    viewHolder.contentTextView.setText(content.getMessage());
                }
            }
        }


    }


    @Override
    public Spannable getContentSummary(ContactNotificationMessage data) {
        if (data != null && !TextUtils.isEmpty(data.getExtra())) {
            ContactNotificationMessageData bean = null;
            try {
                bean = JsonMananger.jsonToBean(data.getExtra(), ContactNotificationMessageData.class);
            } catch (HttpException e) {
                e.printStackTrace();
            }
            if (data.getOperation().equals("AcceptResponse")) {
                return new SpannableString(bean.getSourceUserNickname() + "已同意你的好友请求");
            }
            return new SpannableString(data.getMessage());
        }
        return null;
    }

    @Override
    public void onItemClick(View view, int position, ContactNotificationMessage
            content, UIMessage message) {
    }

    @Override
    public void onItemLongClick(View view, int position, ContactNotificationMessage content, final UIMessage message) {
        String name = null;
        if (message.getConversationType().getName().equals(Conversation.ConversationType.APP_PUBLIC_SERVICE.getName()) ||
                message.getConversationType().getName().equals(Conversation.ConversationType.PUBLIC_SERVICE.getName())) {
            Conversation.PublicServiceType publicServiceType = Conversation.PublicServiceType.setValue(message.getConversationType().getValue());
            PublicServiceProfile info = RongUserInfoManager.getInstance().getPublicServiceProfile(publicServiceType, message.getTargetId());
            if (info != null)
                name = info.getName();
        } else {
            UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(message.getSenderUserId());
            if (userInfo != null)
                name = userInfo.getName();
        }
        String[] items;

        items = new String[]{view.getContext().getResources().getString(R.string.de_dialog_item_message_delete)};

        ArraysDialogFragment.newInstance(name, items).setArraysDialogItemListener(new ArraysDialogFragment.OnArraysDialogItemListener() {
            @Override
            public void OnArraysDialogItemClick(DialogInterface dialog, int which) {
                if (which == 0)
                    RongIM.getInstance().getRongIMClient().deleteMessages(new int[]{message.getMessageId()}, null);

            }
        }).show(((FragmentActivity) view.getContext()).getSupportFragmentManager());
    }

    @Override
    public View newView(Context context, ViewGroup group) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_information_notification_message, null);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.contentTextView = (TextView) view.findViewById(R.id.rc_msg);
        viewHolder.contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
        view.setTag(viewHolder);

        return view;
    }


    class ViewHolder {
        TextView contentTextView;
    }
}
