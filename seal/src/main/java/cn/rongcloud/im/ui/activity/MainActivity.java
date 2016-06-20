package cn.rongcloud.im.ui.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.utils.SharedPreferencesContext;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.ui.adapter.ConversationListAdapterEx;
import cn.rongcloud.im.ui.fragment.DiscoverFragment;
import cn.rongcloud.im.ui.fragment.ContactsFragment;
import cn.rongcloud.im.ui.fragment.CustomerFragment;
import cn.rongcloud.im.utils.Constants;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.message.ContactNotificationMessage;

public class MainActivity extends BaseActivity implements View.OnClickListener, ViewPager.OnPageChangeListener, ActionBar.OnMenuVisibilityListener {

    public static final String ACTION_DMEO_RECEIVE_MESSAGE = "action_demo_receive_message";

    private Menu mMenu;
    private ReceiveMessageBroadcastReciver mBroadcastReciver;

    ActivityManager activityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setLogo(R.drawable.seal_logo_icon);
        getSupportActionBar().setTitle("");
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm); // 获取屏幕信息
        indicatorWidth = dm.widthPixels / 4;// 指示器宽度为屏幕宽度的4/1

        mMainShow = (LinearLayout) findViewById(R.id.main_show);
        mMainConversationLiner = (RelativeLayout) findViewById(R.id.main_conversation_liner);
        mMainGroupLiner = (RelativeLayout) findViewById(R.id.main_group_liner);
        mMainChatroomLiner = (RelativeLayout) findViewById(R.id.main_chatroom_liner);
        mMainCustomerLiner = (RelativeLayout) findViewById(R.id.main_customer_liner);
        mMainConversationTv = (TextView) findViewById(R.id.main_conversation_tv);
        mMainGroupTv = (TextView) findViewById(R.id.main_group_tv);
        mMainChatroomTv = (TextView) findViewById(R.id.main_chatroom_tv);
        mMainCustomerTv = (TextView) findViewById(R.id.main_customer_tv);
        mViewPager = (ViewPager) findViewById(R.id.main_viewpager);
        mMainSelectImg = (ImageView) findViewById(R.id.main_switch_img);
        mUnreadNumView = (TextView) findViewById(R.id.de_num);
        mCustomerNoRead = (TextView) findViewById(R.id.customer_noread);

        ViewGroup.LayoutParams cursor_Params = mMainSelectImg.getLayoutParams();
        cursor_Params.width = indicatorWidth;// 初始化滑动下标的宽
        mMainSelectImg.setLayoutParams(cursor_Params);
        // 获取布局填充器
        mInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);

        activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        mMainChatroomLiner.setOnClickListener(this);
        mMainConversationLiner.setOnClickListener(this);
        mMainGroupLiner.setOnClickListener(this);
        mMainCustomerLiner.setOnClickListener(this);
        mDemoFragmentPagerAdapter = new DemoFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mDemoFragmentPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setOnPageChangeListener(this);
        initData();
    }

    protected void initData() {

        final Conversation.ConversationType[] conversationTypes = {Conversation.ConversationType.PRIVATE,
                Conversation.ConversationType.GROUP, Conversation.ConversationType.SYSTEM,
                Conversation.ConversationType.PUBLIC_SERVICE, Conversation.ConversationType.APP_PUBLIC_SERVICE};

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                RongIM.getInstance().setOnReceiveUnreadCountChangedListener(mCountListener, conversationTypes);
                RongIM.getInstance().setOnReceiveUnreadCountChangedListener(mCountListener1, Conversation.ConversationType.APP_PUBLIC_SERVICE);
            }
        }, 500);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_DMEO_RECEIVE_MESSAGE);
        if (mBroadcastReciver == null) {
            mBroadcastReciver = new ReceiveMessageBroadcastReciver();
        }
        this.registerReceiver(mBroadcastReciver, intentFilter);

        getConversationPush();// 获取 push 的 id 和 target

        getPushMessage();
    }

    /**
     *
     */
    private void getConversationPush() {
        if (getIntent() != null && getIntent().hasExtra("PUSH_CONVERSATIONTYPE") && getIntent().hasExtra("PUSH_TARGETID")) {

            final String conversationType = getIntent().getStringExtra("PUSH_CONVERSATIONTYPE");
            final String targetId = getIntent().getStringExtra("PUSH_TARGETID");


                RongIM.getInstance().getConversation(Conversation.ConversationType.valueOf(conversationType), targetId, new RongIMClient.ResultCallback<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {

                        if (conversation != null) {

                            if (conversation.getLatestMessage() instanceof ContactNotificationMessage) { //好友消息的push
                                startActivity(new Intent(MainActivity.this, NewFriendListActivity.class));
                            } else {
                                Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon().appendPath("conversation")
                                        .appendPath(conversationType).appendQueryParameter("targetId", targetId).build();
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        }
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode e) {

                    }
                });
        }
    }

    /**
     * 得到不落地 push 消息
     */
    private void getPushMessage() {

        Intent intent = getIntent();
        if (intent != null && intent.getData() != null && intent.getData().getScheme().equals("rong")) {
            String content = intent.getData().getQueryParameter("pushContent");
            String data = intent.getData().getQueryParameter("pushData");
            String id = intent.getData().getQueryParameter("pushId");

            if (SharedPreferencesContext.getInstance() != null) {
                String token = SharedPreferencesContext.getInstance().getSharedPreferences().getString("DEMO_TOKEN", "default");
                if (token.equals("default")) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                } else {
                        RongIMClient.ConnectionStatusListener.ConnectionStatus status = RongIM.getInstance().getCurrentConnectionStatus();
                        if (RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED.equals(status)) {
                            return;
                        } else if (RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTING.equals(status)) {
                            return;
                        } else {
                            Intent intent1 = new Intent(MainActivity.this, LoginActivity.class);
                            intent1.putExtra("PUSH_MESSAGE", true);
                            startActivity(intent1);
                            finish();
                        }

                }
            }
        }
    }


    public RongIM.OnReceiveUnreadCountChangedListener mCountListener = new RongIM.OnReceiveUnreadCountChangedListener() {
        @Override
        public void onMessageIncreased(int count) {
            if (count == 0) {
                mUnreadNumView.setVisibility(View.GONE);
            } else if (count > 0 && count < 100) {
                mUnreadNumView.setVisibility(View.VISIBLE);
                mUnreadNumView.setText(count + "");
            } else {
                mUnreadNumView.setVisibility(View.VISIBLE);
                mUnreadNumView.setText(R.string.no_read_message);
            }
        }
    };

    public RongIM.OnReceiveUnreadCountChangedListener mCountListener1 = new RongIM.OnReceiveUnreadCountChangedListener() {
        @Override
        public void onMessageIncreased(int count) {
            if (count == 0) {
                mCustomerNoRead.setVisibility(View.GONE);
            } else if (count > 0) {
                mCustomerNoRead.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    public void onMenuVisibilityChanged(boolean b) {

    }


    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }



    @Override
    public void onPageSelected(int i) {
        switch (i) {
            case 0:
                selectNavSelection(0);
                hintKbTwo();
                break;
            case 1:
                selectNavSelection(1);
                hintKbTwo();
                break;
            case 2:
                selectNavSelection(2);
                break;
            case 3:
                selectNavSelection(3);
                hintKbTwo();
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    private class DemoFragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {

        public DemoFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;
            switch (i) {
                case 0:
                    mMainConversationTv.setTextColor(getResources().getColor(R.color.de_title_bg));
                    //TODO
                    if (mConversationFragment == null) {
                        ConversationListFragment listFragment = ConversationListFragment.getInstance();
                        listFragment.setAdapter(new ConversationListAdapterEx(RongContext.getInstance()));
                        Uri uri;
                        if (SealConst.ISOPENDISCUSSION) {
                            uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                                    .appendPath("conversationlist")
                                    .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") //设置私聊会话是否聚合显示
                                    .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "false")//群组
                                    .appendQueryParameter(Conversation.ConversationType.PUBLIC_SERVICE.getName(), "false")//公共服务号
                                    .appendQueryParameter(Conversation.ConversationType.APP_PUBLIC_SERVICE.getName(), "false")//订阅号
                                    .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "true")//系统
                                    .appendQueryParameter(Conversation.ConversationType.DISCUSSION.getName(),"false")
                                    .build();
                        }else {
                            uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                                    .appendPath("conversationlist")
                                    .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") //设置私聊会话是否聚合显示
                                    .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "false")//群组
                                    .appendQueryParameter(Conversation.ConversationType.PUBLIC_SERVICE.getName(), "false")//公共服务号
                                    .appendQueryParameter(Conversation.ConversationType.APP_PUBLIC_SERVICE.getName(), "false")//订阅号
                                    .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "true")//系统
                                    .build();
                        }
                        listFragment.setUri(uri);
                        fragment = listFragment;
                    } else {
                        fragment = mConversationFragment;
                    }
                    break;
                case 2:
                    if (mGroupListFragment == null) {
                        mGroupListFragment = new ContactsFragment();
                    }

                    fragment = mGroupListFragment;
                    break;

                case 1:
                    if (mChatroomFragment == null) {
                        fragment = new DiscoverFragment();
                    } else {
                        fragment = mChatroomFragment;
                    }
                    break;
                case 3:
                    if (mCustomerFragment == null) {
                        fragment = new CustomerFragment();
                    } else {
                        fragment = mCustomerFragment;
                    }
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }
    }

    private void selectNavSelection(int index) {
        clearSelection();
        switch (index) {
            case 0:
                mMainConversationTv.setTextColor(getResources().getColor(R.color.de_title_bg));
                TranslateAnimation animation = new TranslateAnimation(0, 0,
                        0f, 0f);
                animation.setInterpolator(new LinearInterpolator());
                animation.setDuration(100);
                animation.setFillAfter(true);
                mMainSelectImg.startAnimation(animation);

                break;
            case 1:
                mMainGroupTv.setTextColor(getResources().getColor(R.color.de_title_bg));
                TranslateAnimation animation1 = new TranslateAnimation(
                        indicatorWidth, indicatorWidth,
                        0f, 0f);
                animation1.setInterpolator(new LinearInterpolator());
                animation1.setDuration(100);
                animation1.setFillAfter(true);
                mMainSelectImg.startAnimation(animation1);

                break;
            case 2:
                mMainChatroomTv.setTextColor(getResources().getColor(R.color.de_title_bg));
                TranslateAnimation animation2 = new TranslateAnimation(
                        2 * indicatorWidth, indicatorWidth * 2,
                        0f, 0f);
                animation2.setInterpolator(new LinearInterpolator());
                animation2.setDuration(100);
                animation2.setFillAfter(true);
                mMainSelectImg.startAnimation(animation2);

                break;
            case 3:
                mMainCustomerTv.setTextColor(getResources().getColor(R.color.de_title_bg));
                TranslateAnimation animation3 = new TranslateAnimation(
                        3 * indicatorWidth, indicatorWidth * 3,
                        0f, 0f);
                animation3.setInterpolator(new LinearInterpolator());
                animation3.setDuration(100);
                animation3.setFillAfter(true);
                mMainSelectImg.startAnimation(animation3);
                break;
        }
    }

    private void clearSelection() {
        mMainConversationTv.setTextColor(getResources().getColor(R.color.black_textview));
        mMainGroupTv.setTextColor(getResources().getColor(R.color.black_textview));
        mMainChatroomTv.setTextColor(getResources().getColor(R.color.black_textview));
        mMainCustomerTv.setTextColor(getResources().getColor(R.color.black_textview));
    }

    private class ReceiveMessageBroadcastReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //收到好友添加的邀请，需要更新 Actionbar
            if (action.equals(ACTION_DMEO_RECEIVE_MESSAGE)) {
                supportInvalidateOptionsMenu();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_conversation_liner:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.main_group_liner:
                mViewPager.setCurrentItem(1);
                break;
            case R.id.main_chatroom_liner:
                mViewPager.setCurrentItem(2);
                break;
            case R.id.main_customer_liner:
                mViewPager.setCurrentItem(3);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        this.mMenu = menu;
//        inflater.inflate(R.menu.de_main_menu, menu);
        if (SealConst.ISOPENDISCUSSION) {
            inflater.inflate(R.menu.de_main_menu, menu);
        }else {
            inflater.inflate(R.menu.de_main_menu1, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_conversation:
                mMenu.getItem(0).setIcon(getResources().getDrawable(R.drawable.de_ic_add));
                break;
            case R.id.add_item1://发起聊天
                startActivity(new Intent(this, SelectFriendsActivity.class));
                break;
            case R.id.add_item2://选择群组
                Intent intent = new Intent(new Intent(this, SelectFriendsActivity.class));
                intent.putExtra("createGroup", true);
                startActivity(intent);
                break;
            case R.id.set_item1://我的账号
                startActivity(new Intent(MainActivity.this, MyAccountActivity.class));
                break;
            case R.id.set_item2://新消息提醒
                startActivity(new Intent(MainActivity.this, NewMessageRemindActivity.class));
                break;
            case R.id.set_item3://隐私
                startActivity(new Intent(MainActivity.this, PrivacyActivity.class));
                break;
            case R.id.set_item4://关于融云
                startActivity(new Intent(MainActivity.this, AboutRongCloudActivity.class));
                break;
            case R.id.set_item5://退出

                final AlertDialog.Builder alterDialog = new AlertDialog.Builder(this);
                alterDialog.setMessage(R.string.exit_app);
                alterDialog.setCancelable(true);

                alterDialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (RongIM.getInstance() != null) {
                            if (SharedPreferencesContext.getInstance() != null) {
                                SharedPreferences.Editor edit = SharedPreferencesContext.getInstance().getSharedPreferences().edit();
                                edit.putString(Constants.APP_TOKEN, Constants.DEFAULT);
                                edit.apply();
                            }
                            RongIM.getInstance().logout();
                            try {
                                Thread.sleep(500);
                                Process.killProcess(Process.myPid());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                alterDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alterDialog.show();

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {


            final AlertDialog.Builder alterDialog = new AlertDialog.Builder(this);
            alterDialog.setMessage(R.string.exit_app);
            alterDialog.setCancelable(true);

            alterDialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                        RongIM.getInstance().disconnect(true);

                    try {
                        Thread.sleep(500);
                        Process.killProcess(Process.myPid());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            });
            alterDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alterDialog.show();
        }

        return false;
    }

    @Override
    protected void onDestroy() {
        BroadcastManager.getInstance(mContext).destroy(SealAppContext.UPDATEREDDOT);
        if (mBroadcastReciver != null) {
            this.unregisterReceiver(mBroadcastReciver);
        }
        super.onDestroy();
    }

    private void hintKbTwo() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }




    private RelativeLayout mMainConversationLiner;
    private RelativeLayout mMainGroupLiner;
    private RelativeLayout mMainChatroomLiner;
    private RelativeLayout mMainCustomerLiner;
    /**
     * 聊天室的fragment
     */
    private Fragment mChatroomFragment = null;
    /**
     * 客服的fragment
     */
    private Fragment mCustomerFragment = null;
    /**
     * 会话列表的fragment
     */
    private Fragment mConversationFragment = null;
    /**
     * 群组的fragment
     */
    private ContactsFragment mGroupListFragment = null;
    /**
     * 会话TextView
     */
    private TextView mMainConversationTv;
    /**
     * 群组TextView
     */
    private TextView mMainGroupTv;

    private TextView mUnreadNumView;
    /**
     * 聊天室TextView
     */
    private TextView mMainChatroomTv;
    /**
     * 客服TextView
     */
    private TextView mMainCustomerTv;

    private ViewPager mViewPager;
    /**
     * 下划线
     */
    private ImageView mMainSelectImg;

    private DemoFragmentPagerAdapter mDemoFragmentPagerAdapter;

    private LayoutInflater mInflater;
    /**
     * 下划线长度
     */
    int indicatorWidth;
    private LinearLayout mMainShow;
    private TextView mCustomerNoRead;

}
