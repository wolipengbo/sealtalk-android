package cn.rongcloud.im;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import cn.rongcloud.im.message.AgreedFriendRequestMessage;
import cn.rongcloud.im.message.provider.ContactNotificationMessageProvider;
import cn.rongcloud.im.message.provider.GroupNotificationMessageProvider;
import cn.rongcloud.im.message.provider.NewDiscussionConversationProvider;
import cn.rongcloud.im.message.provider.RealTimeLocationMessageProvider;
import cn.rongcloud.im.server.utils.NLog;
import cn.rongcloud.im.utils.SharedPreferencesContext;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imlib.ipc.RongExceptionHandler;
import io.rong.message.GroupNotificationMessage;
import io.rong.push.RongPushClient;
import io.rong.push.common.RongException;


/**
 * Created by bob on 2015/1/30.
 */
public class App extends Application {

    private static DisplayImageOptions options;

    @Override
    public void onCreate() {

        super.onCreate();

        if (getApplicationInfo().packageName.equals(getCurProcessName(getApplicationContext()))) {

            RongPushClient.registerHWPush(this);
            RongPushClient.registerMiPush(this, "2882303761517473625", "5451747338625");
            try {
                RongPushClient.registerGCM(this);
            } catch (RongException e) {
                e.printStackTrace();
            }
        }
        /**
         * 注意：
         *
         * IMKit SDK调用第一步 初始化
         *
         * context上下文
         *
         * 只有两个进程需要初始化，主进程和 push 进程
         */
        RongIM.init(this);

        if (getApplicationInfo().packageName.equals(getCurProcessName(getApplicationContext()))) {
            SealAppContext.init(this);
            SharedPreferencesContext.init(this);
            Thread.setDefaultUncaughtExceptionHandler(new RongExceptionHandler(this));

            try {
                RongIM.registerMessageType(AgreedFriendRequestMessage.class);
                RongIM.registerMessageType(GroupNotificationMessage.class);
                RongIM.registerMessageTemplate(new ContactNotificationMessageProvider());
                RongIM.registerMessageTemplate(new RealTimeLocationMessageProvider());
                RongIM.registerMessageTemplate(new GroupNotificationMessageProvider());
                //@ 消息模板展示
                RongContext.getInstance().registerConversationTemplate(new NewDiscussionConversationProvider());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(cn.rongcloud.im.R.drawable.de_default_portrait)
                .showImageOnFail(cn.rongcloud.im.R.drawable.de_default_portrait)
                .showImageOnLoading(cn.rongcloud.im.R.drawable.de_default_portrait)
                .displayer(new FadeInBitmapDisplayer(300))
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        //初始化图片下载组件
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(200)
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .defaultDisplayImageOptions(options)
                .build();

        //Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }



    public static DisplayImageOptions getOptions() {
        return options;
    }

}
