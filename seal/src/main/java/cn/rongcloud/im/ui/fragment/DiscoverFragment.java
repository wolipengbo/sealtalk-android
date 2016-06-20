package cn.rongcloud.im.ui.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.DBManager;
import cn.rongcloud.im.db.Groups;
import cn.rongcloud.im.server.SealAction;
import cn.rongcloud.im.server.network.async.AsyncTaskManager;
import cn.rongcloud.im.server.network.async.OnDataListener;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.DefaultConversationResponse;
import cn.rongcloud.im.server.response.GetGroupResponse;
import cn.rongcloud.im.server.response.JoinGroupResponse;
import cn.rongcloud.im.server.utils.CommonUtils;
import cn.rongcloud.im.server.utils.RongGenerate;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.ui.activity.NewGroupDetailActivity;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;

/**
 * Tab 2 , 默认的广场接口，缺点: 为了实时性的更新 UI 上 默认群组上的 人数 和 加入状态的变化 onResume() 中频繁请求服务器
 * <p/>
 * Created by Administrator on 2015/3/6.
 */
public class DiscoverFragment extends Fragment implements OnDataListener, View.OnClickListener {


    private static final int GETDEFCONVERSATION = 333;
    private static final int JOINGROUP = 334;
    private static final int JOINGROUP1 = 335;
    private static final int JOINGROUP2 = 336;
    private static final int GETGROUPS = 337;
    private TextView chatRoom1, chatRoom2, chatRoom3, chatRoom4, groupNumber1, groupNumber2, groupNumber3, groupName1, groupName2, groupName3;

    private Button groupState1, groupState2, groupState3, groupStateChat1, groupStateChat2, groupStateChat3;

    private AsyncTaskManager atm = AsyncTaskManager.getInstance(getActivity());

    private ArrayList<DefaultConversationResponse.ResultEntity> groupList;

    private ArrayList<DefaultConversationResponse.ResultEntity> chatroomList;

    private LinearLayout groupItem1, groupItem2, groupItem3;

    private boolean isJoin1, isJoin2, isJoin3;

    private SelectableRoundedImageView defGroupImage1, defGroupImage2, defGroupImage3;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatroom_list, container, false);
        initViews(view);
        atm.request(GETDEFCONVERSATION, this);
        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.def_chatroom1:
                if (!CommonUtils.isNetworkConnected(getActivity())) {
                    NToast.shortToast(getActivity(), R.string.check_network);
                    return;
                }
                RongIM.getInstance().startConversation(getActivity(), Conversation.ConversationType.CHATROOM, chatroomList.get(0).getId(), "聊天室1");
                break;
            case R.id.def_chatroom2:
                if (!CommonUtils.isNetworkConnected(getActivity())) {
                    NToast.shortToast(getActivity(), R.string.check_network);
                    return;
                }
                RongIM.getInstance().startConversation(getActivity(), Conversation.ConversationType.CHATROOM, chatroomList.get(1).getId(), "聊天室2");
                break;
            case R.id.def_chatroom3:
                if (!CommonUtils.isNetworkConnected(getActivity())) {
                    NToast.shortToast(getActivity(), R.string.check_network);
                    return;
                }
                RongIM.getInstance().startConversation(getActivity(), Conversation.ConversationType.CHATROOM, chatroomList.get(2).getId(), "聊天室3");
                break;
            case R.id.def_chatroom4:
                if (!CommonUtils.isNetworkConnected(getActivity())) {
                    NToast.shortToast(getActivity(), R.string.check_network);
                    return;
                }
                RongIM.getInstance().startConversation(getActivity(), Conversation.ConversationType.CHATROOM, chatroomList.get(3).getId(), "聊天室4");
                break;
            case R.id.def_state_group1_chat:
                if (!CommonUtils.isNetworkConnected(getActivity())) {
                    NToast.shortToast(getActivity(), R.string.check_network);
                    return;
                }
                RongIM.getInstance().startConversation(getActivity(), Conversation.ConversationType.GROUP, groupList.get(0).getId(), "用户体验群 I");
                break;
            case R.id.def_state_group2_chat:
                if (!CommonUtils.isNetworkConnected(getActivity())) {
                    NToast.shortToast(getActivity(), R.string.check_network);
                    return;
                }
                RongIM.getInstance().startConversation(getActivity(), Conversation.ConversationType.GROUP, groupList.get(1).getId(), "用户体验群 II");
                break;
            case R.id.def_state_group3_chat:
                if (!CommonUtils.isNetworkConnected(getActivity())) {
                    NToast.shortToast(getActivity(), R.string.check_network);
                    return;
                }
                RongIM.getInstance().startConversation(getActivity(), Conversation.ConversationType.GROUP, groupList.get(2).getId(), "用户体验群 III");
                break;
            case R.id.def_state_group1:
                if (groupList.get(0) != null) {
                    LoadDialog.show(getActivity());
                    atm.request(JOINGROUP, this);
                }
                break;
            case R.id.def_state_group2:
                if (groupList.get(1) != null) {
                    LoadDialog.show(getActivity());
                    atm.request(JOINGROUP1, this);
                }
                break;
            case R.id.def_state_group3:
                if (groupList.get(2) != null) {
                    LoadDialog.show(getActivity());
                    atm.request(JOINGROUP2, this);
                }
                break;
            case R.id.group_item1:
                if (!isJoin1) {
                    NToast.shortToast(getActivity(), R.string.not_group_members);
                    return;
                }
                Intent intent = new Intent(getActivity(), NewGroupDetailActivity.class);
                intent.putExtra("TargetId", groupList.get(0).getId());
                startActivity(intent);
                break;
            case R.id.group_item2:
                if (!isJoin2) {
                    NToast.shortToast(getActivity(), R.string.not_group_members);
                    return;
                }
                Intent intent1 = new Intent(getActivity(), NewGroupDetailActivity.class);
                intent1.putExtra("TargetId", groupList.get(1).getId());
                startActivity(intent1);

                break;
            case R.id.group_item3:
                if (!isJoin3) {
                    NToast.shortToast(getActivity(), R.string.not_group_members);
                    return;
                }
                Intent intent2 = new Intent(getActivity(), NewGroupDetailActivity.class);
                intent2.putExtra("TargetId", groupList.get(2).getId());
                startActivity(intent2);
                break;
        }
    }


    @Override
    public Object doInBackground(int requsetCode, String id) throws HttpException {
        switch (requsetCode) {
            case GETDEFCONVERSATION:
                return new SealAction(getActivity()).getDefaultConversation();
            case JOINGROUP:
                return new SealAction(getActivity()).JoinGroup(groupList.get(0).getId());
            case JOINGROUP1:
                return new SealAction(getActivity()).JoinGroup(groupList.get(1).getId());
            case JOINGROUP2:
                return new SealAction(getActivity()).JoinGroup(groupList.get(2).getId());
            case GETGROUPS:
                return new SealAction(getActivity()).getGroups();
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case GETDEFCONVERSATION:
                    DefaultConversationResponse response = (DefaultConversationResponse) result;
                    if (response.getCode() == 200) {


                        groupList = new ArrayList();
                        chatroomList = new ArrayList();
                        if (response.getResult().size() > 0) {
                            groupList.clear();
                            chatroomList.clear();
                            for (DefaultConversationResponse.ResultEntity d : response.getResult()) {
                                if (d.getType().equals("group")) {
                                    groupList.add(d);
                                } else {
                                    chatroomList.add(d);
                                }
                            }

                            atm.request(GETGROUPS, this);

                            if (chatroomList != null && chatroomList.size() > 0) {
                                chatRoom1.setText(chatroomList.get(0).getName());
                                chatRoom2.setText(chatroomList.get(1).getName());
                                chatRoom3.setText(chatroomList.get(2).getName());
                                chatRoom4.setText(chatroomList.get(3).getName());
                            }
                            chatRoom1.setText(chatroomList.get(0).getName());
                            if (groupList.size() != 0) {
                                if (groupList.get(0) != null) {
                                    groupNumber1.setText(groupList.get(0).getMemberCount() + "/" + groupList.get(0).getMaxMemberCount());
                                    groupName1.setText(groupList.get(0).getName());
                                    ImageLoader.getInstance().displayImage(RongGenerate.generateDefaultAvatar(groupList.get(0).getName(), groupList.get(0).getId()), defGroupImage1);
                                }
                                if (groupList.get(1) != null) {
                                    groupNumber2.setText(groupList.get(1).getMemberCount() + "/" + groupList.get(1).getMaxMemberCount());
                                    groupName2.setText(groupList.get(1).getName());
                                    ImageLoader.getInstance().displayImage(RongGenerate.generateDefaultAvatar(groupList.get(1).getName(), groupList.get(1).getId()), defGroupImage2);
                                }
                                if (groupList.get(2) != null) {
                                    groupNumber3.setText(groupList.get(2).getMemberCount() + "/" + groupList.get(2).getMaxMemberCount());
                                    groupName3.setText(groupList.get(2).getName());
                                    ImageLoader.getInstance().displayImage(RongGenerate.generateDefaultAvatar(groupList.get(2).getName(), groupList.get(2).getId()), defGroupImage3);
                                }
                            }
                        }

                    }
                    break;

                case JOINGROUP:
                    JoinGroupResponse j1 = (JoinGroupResponse) result;
                    if (j1.getCode() == 200) {
                        refresh();
                        NToast.shortToast(getActivity(), R.string.add_success);
                        LoadDialog.dismiss(getActivity());
                    }
                    break;
                case JOINGROUP1:
                    JoinGroupResponse j2 = (JoinGroupResponse) result;
                    if (j2.getCode() == 200) {
                        refresh();
                        NToast.shortToast(getActivity(), R.string.add_success);
                        LoadDialog.dismiss(getActivity());
                    }

                    break;
                case JOINGROUP2:
                    JoinGroupResponse j3 = (JoinGroupResponse) result;
                    if (j3.getCode() == 200) {
                        refresh();
                        NToast.shortToast(getActivity(), R.string.add_success);
                        LoadDialog.dismiss(getActivity());
                    }
                    break;
                case GETGROUPS:
                    GetGroupResponse getGroupResponse = (GetGroupResponse) result;
                    if (getGroupResponse.getCode() == 200) {
//                        DBManager.getInstance(getActivity()).getDaoSession().getQunDao().deleteAll();
                        for (GetGroupResponse.ResultEntity g : getGroupResponse.getResult()) {
                            DBManager.getInstance(getActivity()).getDaoSession().getGroupsDao().insertOrReplace(
                                    new Groups(g.getGroup().getId(), g.getGroup().getName(), g.getGroup().getPortraitUri(), String.valueOf(g.getRole()))
                            );
                        }

                        isJoin1 = false;
                        isJoin2 = false;
                        isJoin3 = false;

                        List<Groups> list = DBManager.getInstance(getActivity()).getDaoSession().getGroupsDao().loadAll();
                        if (list != null && list.size() > 0) {
                            for (GetGroupResponse.ResultEntity g : getGroupResponse.getResult()) {
                                if (g.getGroup().getId().equals(groupList.get(0).getId())) {
                                    isJoin1 = true;
                                }
                                if (g.getGroup().getId().equals(groupList.get(1).getId())) {
                                    isJoin2 = true;
                                }
                                if (g.getGroup().getId().equals(groupList.get(2).getId())) {
                                    isJoin3 = true;
                                }
                            }
                            if (isJoin1) {
                                groupState1.setVisibility(View.GONE);
                                groupStateChat1.setVisibility(View.VISIBLE);
                            } else {
                                groupState1.setVisibility(View.VISIBLE);
                                groupStateChat1.setVisibility(View.GONE);
                            }
                            if (isJoin2) {
                                groupState2.setVisibility(View.GONE);
                                groupStateChat2.setVisibility(View.VISIBLE);
                            } else {
                                groupState2.setVisibility(View.VISIBLE);
                                groupStateChat2.setVisibility(View.GONE);
                            }
                            if (isJoin3) {
                                groupState3.setVisibility(View.GONE);
                                groupStateChat3.setVisibility(View.VISIBLE);
                            } else {
                                groupState3.setVisibility(View.VISIBLE);
                                groupStateChat3.setVisibility(View.GONE);
                            }

                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case GETDEFCONVERSATION:
                break;
        }
    }

    public void initViews(View view) {
        chatRoom1 = (TextView) view.findViewById(R.id.def_chatroom1);
        chatRoom2 = (TextView) view.findViewById(R.id.def_chatroom2);
        chatRoom3 = (TextView) view.findViewById(R.id.def_chatroom3);
        chatRoom4 = (TextView) view.findViewById(R.id.def_chatroom4);
        groupState1 = (Button) view.findViewById(R.id.def_state_group1);
        groupState2 = (Button) view.findViewById(R.id.def_state_group2);
        groupState3 = (Button) view.findViewById(R.id.def_state_group3);
        groupStateChat1 = (Button) view.findViewById(R.id.def_state_group1_chat);
        groupStateChat2 = (Button) view.findViewById(R.id.def_state_group2_chat);
        groupStateChat3 = (Button) view.findViewById(R.id.def_state_group3_chat);
        groupNumber1 = (TextView) view.findViewById(R.id.group_number1);
        groupNumber2 = (TextView) view.findViewById(R.id.group_number2);
        groupNumber3 = (TextView) view.findViewById(R.id.group_number3);
        groupName1 = (TextView) view.findViewById(R.id.def_group_name1);
        groupName2 = (TextView) view.findViewById(R.id.def_group_name2);
        groupName3 = (TextView) view.findViewById(R.id.def_group_name3);
        groupItem1 = (LinearLayout) view.findViewById(R.id.group_item1);
        groupItem2 = (LinearLayout) view.findViewById(R.id.group_item2);
        groupItem3 = (LinearLayout) view.findViewById(R.id.group_item3);
        defGroupImage1 = (SelectableRoundedImageView) view.findViewById(R.id.def_group_image1);
        defGroupImage2 = (SelectableRoundedImageView) view.findViewById(R.id.def_group_image2);
        defGroupImage3 = (SelectableRoundedImageView) view.findViewById(R.id.def_group_image3);
        chatRoom1.setOnClickListener(this);
        chatRoom2.setOnClickListener(this);
        chatRoom3.setOnClickListener(this);
        chatRoom4.setOnClickListener(this);
        groupState1.setOnClickListener(this);
        groupState2.setOnClickListener(this);
        groupState3.setOnClickListener(this);
        groupStateChat1.setOnClickListener(this);
        groupStateChat2.setOnClickListener(this);
        groupStateChat3.setOnClickListener(this);
        groupItem1.setOnClickListener(this);
        groupItem2.setOnClickListener(this);
        groupItem3.setOnClickListener(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void refresh() {
        atm.request(GETDEFCONVERSATION, this);
    }

}
