package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.DBManager;
import cn.rongcloud.im.db.Groups;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.pinyin.Friend;
import cn.rongcloud.im.server.response.CreateGroupResponse;
import cn.rongcloud.im.server.response.QiNiuTokenResponse;
import cn.rongcloud.im.server.response.SetGroupPortraitResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.utils.photo.PhotoUtils;
import cn.rongcloud.im.server.widget.BottomMenuDialog;
import cn.rongcloud.im.server.widget.ClearWriteEditText;
import cn.rongcloud.im.server.widget.LoadDialog;
import io.rong.imkit.widget.AsyncImageView;

/**
 * Created by AMing on 16/1/25.
 * Company RongCloud
 */
public class CreateGroupActivity extends BaseActivity implements View.OnClickListener {


    private static final int CREATEGROUP = 16;
    private static final int SETGROUPPORTRAITURI = 17;
    public static final String REFRESHGROUPUI = "REFRESHGROUPUI";
    private List<Friend> memberList;

    private AsyncImageView asyncImageView;

    private PhotoUtils photoUtils;

    private BottomMenuDialog dialog;

    private String mGroupName, mGroupId;

    private Button mButton;

    private ClearWriteEditText mGroupNameEdit;

    private List<String> groupIds = new ArrayList<>();

    private Uri selectUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        getSupportActionBar().setTitle(R.string.create_groups);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        memberList = (List<Friend>) getIntent().getSerializableExtra("GroupMember");
        initView();
        setPortraitChangeListener();
        if (memberList != null && memberList.size() > 0) {
            groupIds.add(getSharedPreferences("config", MODE_PRIVATE).getString("loginid", ""));
            for (Friend f : memberList) {
                groupIds.add(f.getUserId());
            }

        }
    }

    private void setPortraitChangeListener() {
        photoUtils = new PhotoUtils(new PhotoUtils.OnPhotoResultListener() {
            @Override
            public void onPhotoResult(Uri uri) {
                if (uri != null && !TextUtils.isEmpty(uri.getPath())) {
                    selectUri = uri;
                    LoadDialog.show(mContext);
                    request(131);
                }
            }

            @Override
            public void onPhotoCancel() {

            }
        });
    }

    private void initView() {
        asyncImageView = (AsyncImageView) findViewById(R.id.img_Group_portrait);
        asyncImageView.setOnClickListener(this);
        mButton = (Button) findViewById(R.id.create_ok);
        mButton.setOnClickListener(this);
        mGroupNameEdit = (ClearWriteEditText) findViewById(R.id.create_groupname);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_Group_portrait:
                showPhotoDialog();
                break;
            case R.id.create_ok:
                mGroupName = mGroupNameEdit.getText().toString().trim();
                if (TextUtils.isEmpty(mGroupName)) {
                    NToast.shortToast(mContext, getString(R.string.group_name_not_is_null));
                    break;
                }
                if (groupIds.size() > 1) {
                    LoadDialog.show(mContext);
                    request(CREATEGROUP, true);
                }

                break;
        }
    }


    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case CREATEGROUP:
                return action.createGroup(mGroupName, groupIds);
            case SETGROUPPORTRAITURI:
                return action.setGroupPortrait(mGroupId, imageUrl);
            case 131:
                return action.getQiNiuToken();
        }
        return null;
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case CREATEGROUP:
                    CreateGroupResponse cgRes = (CreateGroupResponse) result;
                    if (cgRes.getCode() == 200) {
                        mGroupId = cgRes.getResult().getId(); //id == null
                        if (TextUtils.isEmpty(imageUrl)) {
                            DBManager.getInstance(mContext).getDaoSession().getGroupsDao().insertOrReplace(new Groups(mGroupId, mGroupName, imageUrl, String.valueOf(0)));
                            BroadcastManager.getInstance(mContext).sendBroadcast(REFRESHGROUPUI);
                            LoadDialog.dismiss(mContext);
                            NToast.shortToast(mContext, getString(R.string.create_group_success));
                            finish();
                        } else {
                            if (!TextUtils.isEmpty(mGroupId)) {
                                request(SETGROUPPORTRAITURI);
                            }
                        }
                    }
                    break;
                case SETGROUPPORTRAITURI:
                    SetGroupPortraitResponse spRes = (SetGroupPortraitResponse) result;
                    if (spRes.getCode() == 200) {
                        DBManager.getInstance(mContext).getDaoSession().getGroupsDao().insertOrReplace(new Groups(mGroupId, mGroupName, imageUrl, String.valueOf(0)));
                        BroadcastManager.getInstance(mContext).sendBroadcast(REFRESHGROUPUI);
                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(mContext, getString(R.string.create_group_success));
                        finish();
                    }
                case 131:
                    QiNiuTokenResponse response = (QiNiuTokenResponse) result;
                    if (response.getCode() == 200) {
                        uploadImage(response.getResult().getDomain(), response.getResult().getToken(), selectUri);
                    }
                    break;
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case CREATEGROUP:
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, getString(R.string.group_create_api_fail));
                break;
            case SETGROUPPORTRAITURI:
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, getString(R.string.group_header_api_fail));
                break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }


    /**
     * 弹出底部框
     */
    private void showPhotoDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        dialog = new BottomMenuDialog(mContext);
        dialog.setConfirmListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                photoUtils.takePicture(CreateGroupActivity.this);
            }
        });
        dialog.setMiddleListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                photoUtils.selectPicture(CreateGroupActivity.this);
            }
        });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PhotoUtils.INTENT_CROP:
            case PhotoUtils.INTENT_TAKE:
            case PhotoUtils.INTENT_SELECT:
                photoUtils.onActivityResult(CreateGroupActivity.this, requestCode, resultCode, data);
                break;
        }
    }

    private UploadManager uploadManager;

    private String imageUrl;

    public void uploadImage(final String domain, String imageToken, Uri imagePath) {
        if (TextUtils.isEmpty(domain) && TextUtils.isEmpty(imageToken) && TextUtils.isEmpty(imagePath.toString())) {
            throw new RuntimeException("upload parameter is null!");
        }
        File imageFile = new File(imagePath.getPath());

        if (this.uploadManager == null) {
            this.uploadManager = new UploadManager();
        }
        this.uploadManager.put(imageFile, null, imageToken, new UpCompletionHandler() {

            @Override
            public void complete(String s, ResponseInfo responseInfo, JSONObject jsonObject) {
                if (responseInfo.isOK()) {
                    try {
                        String key = (String) jsonObject.get("key");
                        imageUrl = "http://" + domain + "/" + key;
                        Log.e("uploadImage", imageUrl);
                        if (!TextUtils.isEmpty(imageUrl)) {
                            ImageLoader.getInstance().displayImage(imageUrl, asyncImageView);
                            LoadDialog.dismiss(mContext);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, null);
    }
}
