package cn.rongcloud.im.message.provider;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;

import cn.rongcloud.im.utils.SharedPreferencesContext;
import cn.rongcloud.im.R;
import cn.rongcloud.im.utils.Constants;
import io.rong.imkit.RongContext;
import io.rong.imkit.model.ConversationKey;
import io.rong.imkit.model.ConversationProviderTag;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIConversation;

import io.rong.imkit.utils.AndroidEmoji;
import io.rong.imkit.utils.RongDateUtils;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.model.MessageContent;
import io.rong.message.TextMessage;

/**
 * Created by Bob on 15/11/16.
 * 讨论组 @ 消息
 */
@ConversationProviderTag(conversationType = "discussion", portraitPosition = 1)
public class NewDiscussionConversationProvider implements IContainerItemProvider.ConversationProvider<UIConversation> {

    private String TAG = NewDiscussionConversationProvider.class.getSimpleName();

    class ViewHolder {

        TextView title;
        TextView time;
        TextView content;
        ImageView notificationBlockImage;
        TextView atMe;
        final NewDiscussionConversationProvider provider;

        ViewHolder() {
            provider = NewDiscussionConversationProvider.this;
        }
    }

    public NewDiscussionConversationProvider() {

    }

    @Override
    public void bindView(View view, int position, UIConversation data) {

        ViewHolder holder = (ViewHolder) view.getTag();
        ProviderTag tag = null;

        if (data == null) {
            holder.title.setText(null);
            holder.time.setText(null);
            holder.content.setText(null);
        } else {
            //设置会话标题
            holder.title.setText(data.getUIConversationTitle());
            //设置会话时间
            String time = RongDateUtils.getConversationListFormatDate(new Date(data.getUIConversationTime()));
            holder.time.setText(time);
            //设置内容
            if (!TextUtils.isEmpty(data.getDraft())) {
                SpannableStringBuilder builder = new SpannableStringBuilder();
                SpannableString string = new SpannableString(view.getContext().getString(R.string.de_message_content_draft));
                string.setSpan(new ForegroundColorSpan(view.getContext().getResources().getColor(R.color.de_draft_color)), 0, string.length(), 33);
                builder.append(string).append(data.getDraft());
                AndroidEmoji.ensure(builder);
                holder.content.setText(builder);
            } else {
                setDateView(holder, data);
                holder.content.setText(data.getConversationContent());
            }
            if (RongContext.getInstance() != null && data.getMessageContent() != null)
                tag = RongContext.getInstance().getMessageProviderTag(data.getMessageContent().getClass());
            if (data.getSentStatus() != null && (data.getSentStatus() == io.rong.imlib.model.Message.SentStatus.FAILED || data.getSentStatus() == io.rong.imlib.model.Message.SentStatus.SENDING) && tag != null && tag.showWarning()) {
                int width = (int) view.getContext().getResources().getDimension(R.dimen.de_message_send_status_image_size);
                Drawable drawable = null;
                if (data.getSentStatus() == io.rong.imlib.model.Message.SentStatus.FAILED)
                    drawable = view.getContext().getResources().getDrawable(R.drawable.de_conversation_list_msg_send_failure);
                else if (data.getSentStatus() == io.rong.imlib.model.Message.SentStatus.SENDING)
                    drawable = view.getContext().getResources().getDrawable(R.drawable.de_conversation_list_msg_sending);
                if (drawable != null) {
                    drawable.setBounds(0, 0, width, width);
                    holder.content.setCompoundDrawablePadding(10);
                    holder.content.setCompoundDrawables(drawable, null, null, null);
                }
            } else {
                holder.content.setCompoundDrawables(null, null, null, null);
            }
            ConversationKey key = ConversationKey.obtain(data.getConversationTargetId(), data.getConversationType());
            io.rong.imlib.model.Conversation.ConversationNotificationStatus status = RongContext.getInstance().getConversationNotifyStatusFromCache(key);
            if (status != null && status.equals(io.rong.imlib.model.Conversation.ConversationNotificationStatus.DO_NOT_DISTURB))
                holder.notificationBlockImage.setVisibility(View.VISIBLE);
            else
                holder.notificationBlockImage.setVisibility(View.GONE);
        }
    }

    /**
     *  @ 消息提示
     * @param holder
     * @param data
     */
    private void setDateView(ViewHolder holder, UIConversation data) {

        String userId = null;
        String username = null;

        if (SharedPreferencesContext.getInstance() != null) {
            userId = SharedPreferencesContext.getInstance().getSharedPreferences().getString(Constants.APP_USER_ID, Constants.DEFAULT);
            username = SharedPreferencesContext.getInstance().getSharedPreferences().getString(Constants.APP_USER_NAME, Constants.DEFAULT);
        }

        MessageContent messageContent = data.getMessageContent();
        if (messageContent instanceof TextMessage) {

            if (((TextMessage) messageContent).getContent().contains("@" + userId)
                    || ((TextMessage) messageContent).getContent().contains("@" + username)) {

                if (data.getUnReadMessageCount() == 0) {
                    holder.atMe.setVisibility(View.GONE);
                    data.setExtraFlag(false);
                } else if (data.getUnReadMessageCount() > 0) {
                    holder.atMe.setVisibility(View.VISIBLE);
                    data.setExtraFlag(true);
                }
            } else {
                if (data.getExtraFlag()) {
                    holder.atMe.setVisibility(View.VISIBLE);
                } else {
                    holder.atMe.setVisibility(View.GONE);
                    data.setExtraFlag(false);
                }
                if (data.getUnReadMessageCount() == 0) {
                    holder.atMe.setVisibility(View.GONE);
                    data.setExtraFlag(false);
                }
            }
        }
    }


    @Override
    public View newView(Context context, ViewGroup viewgroup) {
        // TODO Auto-generated method stub
        View result = LayoutInflater.from(context).inflate(R.layout.item_base_conversation, null);
        ViewHolder holder = new ViewHolder();
        holder.title = (TextView) result.findViewById(R.id.de_conversation_title);
        holder.time = (TextView) result.findViewById(R.id.de_conversation_time);
        holder.content = (TextView) result.findViewById(R.id.de_conversation_content);
        holder.notificationBlockImage = (ImageView) result.findViewById(R.id.de_conversation_msg_block);
        holder.atMe = (TextView) result.findViewById(R.id.de_at_me);
        result.setTag(holder);
        return result;
    }

    @Override
    public String getTitle(String s) {
        // TODO Auto-generated method stub
        String name;
        if (RongContext.getInstance().getDiscussionInfoFromCache(s) == null)
            name = RongContext.getInstance().getResources().getString(R.string.de_group_list_default_discussion_name);
        else
            name = RongContext.getInstance().getDiscussionInfoFromCache(s).getName();
        return name;
    }

    @Override
    public Uri getPortraitUri(String s) {
        // TODO Auto-generated method stub
        return null;
    }

}
