package cn.rongcloud.im.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.rongcloud.im.R;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.ui.activity.DoubleChatRoomActivity;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;

/**
 * tab 4 客服的 Fragment
 * Created by Administrator on 2015/3/6.
 */
public class CustomerFragment extends Fragment implements View.OnClickListener {

    /**
     * 客服聊天的按钮
     */
    private TextView mCustomerChat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer, container, false);
        mCustomerChat = (TextView) view.findViewById(R.id.customer_chat);
        mCustomerChat.setOnClickListener(this);
        mCustomerChat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                    startDoubleChatroom(getActivity(),"OIBbeKlkx","675NdFjkx");
                return true;
            }
        });
        return view;
    }

    private void startDoubleChatroom(Context context ,String chatroomId1,String chatroomId2) {
        if (TextUtils.isEmpty(chatroomId1)&&TextUtils.isEmpty(chatroomId2)) {
            NToast.shortToast(context, "chatroom ids is null");
            return;
        }
        if (RongIM.getInstance().getCurrentConnectionStatus().equals(RongIMClient.ConnectionStatusListener.ConnectionStatus.DISCONNECTED)) {
            NToast.shortToast(context, "please connection");
            return;
        }
        Intent intent = new Intent(context,DoubleChatRoomActivity.class);
        intent.putExtra("chatroomId1",chatroomId1);
        intent.putExtra("chatroomId2",chatroomId2);
        startActivity(intent);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.customer_chat:
                if (RongIM.getInstance() != null)
                    // KEFU146001495753714 正式  KEFU145930951497220 测试
                    RongIM.getInstance().startCustomerServiceChat(getActivity(),"KEFU146001495753714","在线客服",null);
                break;
        }
    }
}
