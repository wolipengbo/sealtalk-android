package cn.rongcloud.im.message;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;

import cn.rongcloud.im.R;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.provider.InputProvider;
import io.rong.imlib.RongIMClient;
import io.rong.message.TextMessage;

/**
 * Created by Bob on 15/6/6.
 * 会话扩展功能自定义
 */
public class ContactsProvider extends InputProvider.ExtendProvider {

    HandlerThread mWorkThread;
    Handler mUploadHandler;
    private int REQUEST_CONTACT = 20;

    public ContactsProvider(RongContext context) {
        super(context);
        mWorkThread = new HandlerThread("RongDemo");
        mWorkThread.start();
        mUploadHandler = new Handler(mWorkThread.getLooper());
    }

    /**
     * 设置展示的图标
     *
     * @param context
     * @return
     */
    @Override
    public Drawable obtainPluginDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.de_contacts);
    }

    /**
     * 设置图标下的title
     *
     * @param context
     * @return
     */
    @Override
    public CharSequence obtainPluginTitle(Context context) {
        return context.getString(R.string.add_contacts);
    }

    /**
     * click 事件
     *
     * @param view
     */
    @Override
    public void onPluginClick(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setData(ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CONTACT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK)
            return;

        if (data.getData() != null && "content".equals(data.getData().getScheme())) {
            mUploadHandler.post(new MyRunnable(data.getData()));
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    class MyRunnable implements Runnable {

        Uri mUri;

        public MyRunnable(Uri uri) {
            mUri = uri;
        }

        @Override
        public void run() {
            String[] contact = getPhoneContacts(mUri);

            String showMessage = contact[0] + "\n" + contact[1];
            final TextMessage content = TextMessage.obtain(showMessage);

            if (RongIM.getInstance().getRongIMClient() != null)
                RongIM.getInstance().getRongIMClient().sendMessage(getCurrentConversation().getConversationType(), getCurrentConversation().getTargetId(), content, null, null, new RongIMClient.SendMessageCallback() {
                    @Override
                    public void onError(Integer integer, RongIMClient.ErrorCode errorCode) {
                        Log.d("ExtendProvider", "onError--" + errorCode);
                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        Log.d("ExtendProvider", "onSuccess--" + integer);
                    }
                });
        }
    }

    private String[] getPhoneContacts(Uri uri) {

        String[] contact = new String[2];
        ContentResolver cr = getContext().getContentResolver();
        Cursor cursor = cr.query(uri, null, null, null, null);
        cursor.moveToFirst();
        if (cursor != null) {
            cursor.moveToFirst();
            int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
            if (contact.length > 0)
                contact[0] = cursor.getString(nameFieldColumnIndex);

            String ContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor phone = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + ContactId, null, null);

            if (phone != null) {
                phone.moveToFirst();
                contact[1] = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            phone.close();
            cursor.close();
        }
        return contact;
    }

}
