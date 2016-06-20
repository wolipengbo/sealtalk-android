package cn.rongcloud.im.message.provider;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.rongcloud.im.R;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.widget.ArraysDialogFragment;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.location.message.RealTimeLocationStartMessage;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;

/**
 * Created by weiqinxiao on 15/8/14.
 */
@ProviderTag(messageContent = RealTimeLocationStartMessage.class)
public class RealTimeLocationMessageProvider extends IContainerItemProvider.MessageProvider<RealTimeLocationStartMessage> {
    class ViewHolder {
        TextView message;
        boolean longClick;
    }

    @Override
    public View newView(Context context, ViewGroup group) {
        View view = LayoutInflater.from(context).inflate(cn.rongcloud.im.R.layout.de_share_location_message, null);

        ViewHolder holder = new ViewHolder();
        holder.message = (TextView) view.findViewById(android.R.id.text1);
        view.setTag(holder);
        return view;
    }

    @Override
    public Spannable getContentSummary(RealTimeLocationStartMessage data) {
        if (data != null && data.getContent() != null)
            return new SpannableString("我发起了位置共享");
        return null;
    }

    @Override
    public void onItemClick(View view, int position, RealTimeLocationStartMessage content, UIMessage message) {

    }

    @Override
    public void onItemLongClick(final View view, int position, final RealTimeLocationStartMessage content, final UIMessage message) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.longClick = true;
        if (view instanceof TextView) {
            CharSequence text = ((TextView) view).getText();
            if (text != null && text instanceof Spannable)
                Selection.removeSelection((Spannable) text);
        }

        String name = null;

        if (message.getSenderUserId() != null) {
            UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(message.getSenderUserId());
            if (userInfo != null)
                name = userInfo.getName();
        }
        String[] items;

        Resources res = view.getContext().getResources();
        items = new String[]{res.getString(R.string.rc_dialog_item_message_delete)};

        ArraysDialogFragment.newInstance(name, items).setArraysDialogItemListener(new ArraysDialogFragment.OnArraysDialogItemListener() {
            @Override
            public void OnArraysDialogItemClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    RongIM.getInstance().getRongIMClient().deleteMessages(new int[]{message.getMessageId()}, null);
                }

            }
        }).show(((FragmentActivity) view.getContext()).getSupportFragmentManager());
    }

    @Override
    public void bindView(View v, int position, final RealTimeLocationStartMessage content, final UIMessage data) {
        ViewHolder holder = (ViewHolder) v.getTag();

        if (data.getMessageDirection() == Message.MessageDirection.SEND) {
            Drawable drawable = holder.message.getResources().getDrawable(R.drawable.rc_icon_rt_message_right);
            drawable.setBounds(0, 0, 29, 41);
            holder.message.setBackgroundResource(R.drawable.rc_ic_bubble_right);
            holder.message.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
            holder.message.setText("发起了位置共享");
        } else {
            Drawable drawable = holder.message.getResources().getDrawable(R.drawable.rc_icon_rt_message_left);
            drawable.setBounds(0, 0, 29, 41);
            holder.message.setBackgroundResource(R.drawable.rc_ic_bubble_left);
            holder.message.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            holder.message.setText("发起了位置共享");
        }
    }
}
