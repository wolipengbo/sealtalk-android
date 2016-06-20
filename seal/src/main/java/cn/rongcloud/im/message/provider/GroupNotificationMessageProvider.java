package cn.rongcloud.im.message.provider;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.CreateGroupData;
import cn.rongcloud.im.server.response.GroupNotificationMessageData;
import cn.rongcloud.im.server.utils.json.JsonMananger;
import io.rong.imkit.RongContext;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.message.GroupNotificationMessage;

/**
 * Created by AMing on 16/2/18.
 * Company RongCloud
 */
@ProviderTag(messageContent = GroupNotificationMessage.class, showPortrait = false, centerInHorizontal = true, showProgress = false, showSummaryWithName = false)
public class GroupNotificationMessageProvider extends IContainerItemProvider.MessageProvider<GroupNotificationMessage> {

    @Override
    public void bindView(View view, int i, GroupNotificationMessage groupNotificationMessage, UIMessage uiMessage) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        if (groupNotificationMessage != null && uiMessage != null) {

            GroupNotificationMessageData data = null;
            try {
                data = JsonMananger.jsonToBean(groupNotificationMessage.getData(), GroupNotificationMessageData.class);
            } catch (HttpException e) {
                e.printStackTrace();
            }

            String operatorNickname = data.getData().getOperatorNickname();
            List<String> memberList = data.getData().getTargetUserDisplayNames();
            String memberName = null;
            if (memberList != null && memberList.size() == 1) {
                memberName = memberList.get(0);
            } else if (memberList != null) {
                StringBuilder sb = new StringBuilder();
                for (String s : memberList) {
                    sb.append(s);
                    sb.append(",");
                }
                String str = sb.toString();
                memberName = str.substring(0, str.length() - 1);
            }
            Context context = RongContext.getInstance();
            if (!TextUtils.isEmpty(groupNotificationMessage.getOperation()))
                if (groupNotificationMessage.getOperation().equals("Add")) {
                    if (operatorNickname.equals(memberName)) {
                        viewHolder.contentTextView.setText(memberName + context.getString(R.string.join_group));
                    } else {
                        viewHolder.contentTextView.setText(operatorNickname + context.getString(R.string.invitation) + memberName + context.getString(R.string.join_group));
                    }
                } else if (groupNotificationMessage.getOperation().equals("Kicked")) {
                    viewHolder.contentTextView.setText(operatorNickname + context.getString(R.string.will) + memberName + context.getString(R.string.remove_group));
                } else if (groupNotificationMessage.getOperation().equals("Create")) {
                    CreateGroupData createGroupData = null;
                    try {
                        createGroupData = JsonMananger.jsonToBean(groupNotificationMessage.getData(), CreateGroupData.class);
                    } catch (HttpException e) {
                        e.printStackTrace();
                    }
                    viewHolder.contentTextView.setText(createGroupData.getData().getOperatorNickname() + context.getString(R.string.create_groups));
                } else if (groupNotificationMessage.getOperation().equals("Dismiss")) {
                    viewHolder.contentTextView.setText(operatorNickname + context.getString(R.string.dismiss_groups));
                } else if (groupNotificationMessage.getOperation().equals("Quit")) {
                    viewHolder.contentTextView.setText(operatorNickname + context.getString(R.string.quit_groups));
                } else if (groupNotificationMessage.getOperation().equals("Rename")) {
                    viewHolder.contentTextView.setText(operatorNickname + context.getString(R.string.change_group_name));
                }
        }
    }

    @Override
    public Spannable getContentSummary(GroupNotificationMessage groupNotificationMessage) {
        GroupNotificationMessageData data = null;
        try {
            data = JsonMananger.jsonToBean(groupNotificationMessage.getData(), GroupNotificationMessageData.class);
        } catch (HttpException e) {
            e.printStackTrace();
        }
        String operatorNickname = data.getData().getOperatorNickname();
        List<String> memberList = data.getData().getTargetUserDisplayNames();
        String memberName = "";
        if (memberList != null && memberList.size() == 1) {
            memberName = memberList.get(0);
        } else if (memberList != null) {
            StringBuilder sb = new StringBuilder();
            for (String s : memberList) {
                sb.append(s);
                sb.append(",");
            }
            String str = sb.toString();
            memberName = str.substring(0, str.length() - 1);
        }


        Context context = RongContext.getInstance();
        if (TextUtils.isEmpty(memberName)) {
            memberName = operatorNickname;
        }
        if (groupNotificationMessage.getOperation().equals("Add")) {
            if (groupNotificationMessage.getOperatorUserId().toString().trim().equals(data.getData().getTargetUserIds().get(0).toString().trim())) {
                return new SpannableString(operatorNickname + context.getString(R.string.join_group));
            } else {
                return new SpannableString(operatorNickname + context.getString(R.string.invitation) + memberName + context.getString(R.string.join_group));
            }
        } else if (groupNotificationMessage.getOperation().equals("Kicked")) {
            return new SpannableString(operatorNickname + context.getString(R.string.will) + memberName + context.getString(R.string.remove_group));
        } else if (groupNotificationMessage.getOperation().equals("Create")) {
            return new SpannableString(operatorNickname + context.getString(R.string.create_groups));
        } else if (groupNotificationMessage.getOperation().equals("Dismiss")) {
            return new SpannableString(operatorNickname + context.getString(R.string.dismiss_groups));
        } else if (groupNotificationMessage.getOperation().equals("Quit")) {
            return new SpannableString(operatorNickname + context.getString(R.string.quit_groups));
        } else if (groupNotificationMessage.getOperation().equals("Rename")) {
            return new SpannableString(operatorNickname + context.getString(R.string.change_group_name));
        }
        return new SpannableString("[群组通知]");
    }

    @Override
    public void onItemClick(View view, int i, GroupNotificationMessage groupNotificationMessage, UIMessage uiMessage) {

    }

    @Override
    public void onItemLongClick(View view, int i, GroupNotificationMessage groupNotificationMessage, UIMessage uiMessage) {

    }

    @Override
    public View newView(Context context, ViewGroup viewGroup) {
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
