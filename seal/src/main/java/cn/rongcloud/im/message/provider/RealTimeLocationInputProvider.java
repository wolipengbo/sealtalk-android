package cn.rongcloud.im.message.provider;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import cn.rongcloud.im.ui.activity.RealTimeLocationActivity;
import cn.rongcloud.im.model.RongEvent;
import io.rong.eventbus.EventBus;
import io.rong.imkit.RongContext;
import io.rong.imkit.widget.ArraysDialogFragment;
import io.rong.imkit.widget.provider.LocationInputProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.location.RealTimeLocationConstant;

/**
 * Created by zhjchen on 8/13/15.
 */
public class RealTimeLocationInputProvider extends LocationInputProvider {

    public RealTimeLocationInputProvider(RongContext context) {
        super(context);
    }

    @Override
    public void onPluginClick(final View view) {

        RealTimeLocationConstant.RealTimeLocationErrorCode errorCode = RongIMClient.getInstance().getRealTimeLocation(getCurrentConversation().getConversationType(), getCurrentConversation().getTargetId());

        if (errorCode != null && errorCode != RealTimeLocationConstant.RealTimeLocationErrorCode.RC_REAL_TIME_LOCATION_CONVERSATION_NOT_SUPPORT) {//服务端未开通

            ArraysDialogFragment arraysDialogFragment = ArraysDialogFragment.newInstance("位置", new String[]{"发送位置", "实时位置共享"});
            arraysDialogFragment.setArraysDialogItemListener(new ArraysDialogFragment.OnArraysDialogItemListener() {

                @Override
                public void OnArraysDialogItemClick(DialogInterface dialog, int which) {

                    if (which == 0) {
                        superOnPluginClick(view);
                    } else if (which == 1) {

                        RealTimeLocationConstant.RealTimeLocationStatus status = RongIMClient.getInstance().getRealTimeLocationCurrentState(getCurrentConversation().getConversationType(), getCurrentConversation().getTargetId());

                        if (status == null || status == RealTimeLocationConstant.RealTimeLocationStatus.RC_REAL_TIME_LOCATION_STATUS_IDLE) {
                            startRealTimeLocation(view.getContext());
                            EventBus.getDefault().post(RongEvent.RealTimeLocationMySelfJoinEvent.obtain(""));
                        } else {
                            joinRealTimeLocation(view.getContext());
                        }

                    }
                }
            });

            arraysDialogFragment.show(getCurrentFragment().getFragmentManager());

        } else {
            superOnPluginClick(view);
        }
    }

    private void startRealTimeLocation(Context context) {

        Log.e(this.getClass().getSimpleName(), "startRealTimeLocation:---");
        RealTimeLocationConstant.RealTimeLocationErrorCode errorCode = RongIMClient.getInstance().startRealTimeLocation(getCurrentConversation().getConversationType(), getCurrentConversation().getTargetId());
        Log.e(this.getClass().getSimpleName(), "startRealTimeLocation RealTimeLocationErrorCode:--->" + errorCode);

        Intent intent = new Intent(((FragmentActivity) context), RealTimeLocationActivity.class);
        intent.putExtra("conversationType", getCurrentConversation().getConversationType().getValue());
        intent.putExtra("targetId", getCurrentConversation().getTargetId());
        startActivityForResult(intent, 123);
    }

    private void joinRealTimeLocation(Context context) {
        Log.e(this.getClass().getSimpleName(), "joinRealTimeLocation:---");
        RealTimeLocationConstant.RealTimeLocationErrorCode errorCode = RongIMClient.getInstance().joinRealTimeLocation(getCurrentConversation().getConversationType(), getCurrentConversation().getTargetId());

        Log.e(this.getClass().getSimpleName(), "joinRealTimeLocation:--->" + errorCode);

        Intent intent = new Intent(((FragmentActivity) context), RealTimeLocationActivity.class);
        intent.putExtra("conversationType", getCurrentConversation().getConversationType().getValue());
        intent.putExtra("targetId", getCurrentConversation().getTargetId());
        startActivityForResult(intent, 123);
    }


    private void superOnPluginClick(View view) {
        super.onPluginClick(view);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
