package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.FriendInvitationResponse;
import cn.rongcloud.im.server.response.GetUserInfoByPhoneResponse;
import cn.rongcloud.im.server.utils.RongGenerate;
import cn.rongcloud.im.server.utils.AMUtils;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.DialogWithYesOrNoUtils;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;

/**
 * Created by Bob on 2015/3/26.
 */
public class SearchFriendActivity extends BaseActivity {

    private static final int SEARCHPHONE = 10;
    private static final int ADDFRIEND = 11;
    private EditText mEtSearch;

    private Button mBtSearch;

    private LinearLayout searchItem;

    private TextView searchName;

    private SelectableRoundedImageView searchImage;

    private String mPhone, addFriendMessage, mFriendId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().setTitle(R.string.Search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);

        mEtSearch = (EditText) findViewById(R.id.de_ui_search);
        mBtSearch = (Button) findViewById(R.id.de_search);
        searchItem = (LinearLayout) findViewById(R.id.search_result);
        searchName = (TextView) findViewById(R.id.search_name);
        searchImage = (SelectableRoundedImageView) findViewById(R.id.search_header);


        mBtSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhone = mEtSearch.getText().toString().trim();
                if (TextUtils.isEmpty(mPhone)) {
                    NToast.shortToast(mContext, R.string.phone_number_is_null);
                    return;
                }

                if (!AMUtils.isMobile(mPhone)) {
                    NToast.shortToast(mContext, "手机号正则验证失败");
                    return;
                }

                LoadDialog.show(mContext);
                request(SEARCHPHONE, true);
            }
        });

    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case SEARCHPHONE:
                return action.getUserInfoFromPhone("86", mPhone);
            case ADDFRIEND:
                return action.sendFriendInvitation(mFriendId, addFriendMessage);
        }
        return super.doInBackground(requestCode, id);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case SEARCHPHONE:
                    final GetUserInfoByPhoneResponse guifres = (GetUserInfoByPhoneResponse) result;
                    if (guifres.getCode() == 200) {
                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(mContext, "success");
                        mFriendId = guifres.getResult().getId();
                        searchItem.setVisibility(View.VISIBLE);
                        if (TextUtils.isEmpty(guifres.getResult().getPortraitUri())) {
                            ImageLoader.getInstance().displayImage(RongGenerate.generateDefaultAvatar(guifres.getResult().getNickname(), guifres.getResult().getId()) ,searchImage, App.getOptions());
                        }else {
                            ImageLoader.getInstance().displayImage(guifres.getResult().getPortraitUri(), searchImage, App.getOptions());
                        }
                        searchName.setText(guifres.getResult().getNickname());
                        searchItem.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (getSharedPreferences("config", MODE_PRIVATE).getString("loginphone", "").equals(mEtSearch.getText().toString().trim())) {
                                    NToast.shortToast(mContext, getString(R.string.can_not_add_yourself));
                                    return;
                                }

                                DialogWithYesOrNoUtils.getInstance().showEditDialog(mContext, getString(R.string.add_text), getString(R.string.add_friend), new DialogWithYesOrNoUtils.DialogCallBack() {
                                    @Override
                                    public void exectEvent() {

                                    }

                                    @Override
                                    public void updatePassword(String oldPassword, String newPassword) {

                                    }

                                    @Override
                                    public void exectEditEvent(String editText) {
//                                        String name = getSharedPreferences("config", MODE_PRIVATE).getString("loginnickname", "");
                                        addFriendMessage = editText;
                                        if (TextUtils.isEmpty(editText)) {
                                            addFriendMessage = getString(R.string.request_friends);
                                        }
                                        if (!TextUtils.isEmpty(mFriendId)) {
                                            LoadDialog.show(mContext);
                                            request(ADDFRIEND);
                                        } else {
                                            NToast.shortToast(mContext, "id is null");
                                        }
                                    }
                                });
                            }
                        });

                    }
                    break;
                case ADDFRIEND:
                    FriendInvitationResponse fres = (FriendInvitationResponse) result;
                    if (fres.getCode() == 200) {
                        NToast.shortToast(mContext, getString(R.string.request_success));
                        LoadDialog.dismiss(mContext);
                    } else {
                        NToast.shortToast(mContext, "请求失败 错误码:" + fres.getCode());
                        LoadDialog.dismiss(mContext);
                    }
                    break;
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case ADDFRIEND:
                NToast.shortToast(mContext, "好友邀请请求失败");
                LoadDialog.dismiss(mContext);
                break;
            case SEARCHPHONE:
                NToast.shortToast(mContext, "用户不存在");
                LoadDialog.dismiss(mContext);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
