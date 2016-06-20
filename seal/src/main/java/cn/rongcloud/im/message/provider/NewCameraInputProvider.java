package cn.rongcloud.im.message.provider;

/**
 * Created by AMing on 16/4/14.
 * Company RongCloud
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import android.os.Handler;


import cn.rongcloud.im.R;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.provider.InputProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;

/**
 * 相机提供方案二 :
 * Kit SDK 内提供的相机功能 有一个预览界面 如果不想要那个预览界面拍照完直接发送
 * 即可用此类 拍照完勾选直接发送出
 *
 */
public class NewCameraInputProvider extends InputProvider.ExtendProvider {


    HandlerThread mWorkThread;

    Handler mUploadHandler;

    private RongContext mContext;
    private File photoFile;

    public NewCameraInputProvider(RongContext context) {
        super(context);
        this.mContext = context;
        // 来自融云demo
        mWorkThread = new HandlerThread("RongDemo");
        mWorkThread.start();
        mUploadHandler = new Handler(mWorkThread.getLooper());

    }

    @Override
    public Drawable obtainPluginDrawable(Context arg0) {
        // TODO Auto-generated method stub
        return arg0.getResources().getDrawable(R.drawable.rc_ic_picture);
    }

    @Override
    public CharSequence obtainPluginTitle(Context arg0) {
        return "拍照";
    }

    @Override
    public void onPluginClick(View arg0) {
        // 点击跳转至拍照
        photoFile = new File(Environment.getExternalStorageDirectory()
                + "/my_camera/" + UUID.randomUUID() + ".jpg");// 图片储存路径
        if (!photoFile.getParentFile().exists()) {
            photoFile.getParentFile().mkdirs();
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 根据选择完毕的图片返回值，直接上传文件
        if (requestCode == 1) {// 拍照
            String localStrPath = photoFile.getPath();
            byte[] compressBitmap = BitmapUtils.compressBitmap(480 * 480,
                    localStrPath);
            if (null != compressBitmap) {
                Bitmap bmPhoto = BitmapUtils
                        .Bytes2Bimap(compressBitmap);
                if (null != bmPhoto) {
                    String strTempPhotoPath;
                    try {
                        strTempPhotoPath = BitmapUtils
                                .saveFile(bmPhoto,
                                        UUID.randomUUID()
                                                + ".jpeg");
                        if(bmPhoto != null){
                            bmPhoto.recycle();
                            bmPhoto = null;
                        }
                        if (null != strTempPhotoPath
                                && !"".equals(strTempPhotoPath)) {
                            localStrPath = strTempPhotoPath;
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                localStrPath = "file://" + localStrPath;
                Uri pathUri = Uri.parse(localStrPath);
                mUploadHandler.post(new MyRunnable(pathUri));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 用于显示文件的异步线程
     */
    class MyRunnable implements Runnable {

        Uri mUri;

        public MyRunnable(Uri uri) {
            mUri = uri;
        }

        @Override
        public void run() {

            // 封装image类型的IM消息
            final ImageMessage content = ImageMessage.obtain(mUri, mUri);

            if (RongIM.getInstance() != null
                    && RongIM.getInstance().getRongIMClient() != null)
                RongIM.getInstance()
                        .getRongIMClient()
                        .sendImageMessage(getCurrentConversation().getConversationType(),getCurrentConversation().getTargetId(),content,null,null,new RongIMClient.SendImageMessageCallback() {
                            @Override
                            public void onAttached(Message message) {

                            }

                            @Override
                            public void onError(Message message, RongIMClient.ErrorCode errorCode) {

                            }

                            @Override
                            public void onSuccess(Message message) {

                            }

                            @Override
                            public void onProgress(Message message, int i) {

                            }
                        });

        }
    }

}
