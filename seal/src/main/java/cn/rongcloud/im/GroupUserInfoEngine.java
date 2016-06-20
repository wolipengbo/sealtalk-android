package cn.rongcloud.im;

import android.content.Context;
import android.text.TextUtils;

import java.util.List;

import cn.rongcloud.im.server.SealAction;
import cn.rongcloud.im.server.network.async.AsyncTaskManager;
import cn.rongcloud.im.server.network.async.OnDataListener;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.GetGroupMemberResponse;
import cn.rongcloud.im.server.utils.NLog;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.GroupUserInfo;

/**
 * Created by AMing on 16/2/29.
 * Company RongCloud
 */
public class GroupUserInfoEngine implements OnDataListener {

    private String groupId, userId;

    private static final int REQUESTGROUPUSERINFO = 50;
    private static GroupUserInfoEngine instance;

    private GroupUserInfoEngine(Context context) {
        this.context = context;
    }

    private static Context context;

    private GroupUserInfo groupUserInfo;

    public GroupUserInfo getGroupUserInfo() {
        return groupUserInfo;
    }

    public void setGroupUserInfo(GroupUserInfo groupUserInfo) {
        this.groupUserInfo = groupUserInfo;
    }

    public static GroupUserInfoEngine getInstance(Context context) {
        if (instance == null) {
            instance = new GroupUserInfoEngine(context);
        }
        return instance;
    }

    public GroupUserInfo startEngine(String groupId, String userid) {
        if (!TextUtils.isEmpty(groupId) && !TextUtils.isEmpty(userid)) {
            this.groupId = groupId;
            this.userId = userid;
            AsyncTaskManager.getInstance(context).request(REQUESTGROUPUSERINFO, this);
        }
        return getGroupUserInfo();
    }


    @Override
    public Object doInBackground(int requsetCode, String id) throws HttpException {
        return new SealAction(context).getGroupMember(id);
    }

    private List<GetGroupMemberResponse.ResultEntity> mGroupMember;

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            GetGroupMemberResponse res = (GetGroupMemberResponse) result;
            if (res.getCode() == 200) {
                mGroupMember = res.getResult();
                for (GetGroupMemberResponse.ResultEntity g : mGroupMember) {
                    if (g.getUser().getId().equals(userId)) {
                        if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {
                            RongIM.getInstance().refreshGroupUserInfoCache(new GroupUserInfo(groupId,userId,g.getDisplayName()));
                            setGroupUserInfo(new GroupUserInfo(groupId,userId,g.getDisplayName()));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {

    }
}
