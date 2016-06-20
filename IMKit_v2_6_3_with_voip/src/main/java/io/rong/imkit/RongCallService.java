package io.rong.imkit;

import android.content.Context;
import android.content.Intent;

import java.util.List;

import io.rong.calllib.IRongReceivedCallListener;
import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
import io.rong.common.RLog;
import io.rong.imkit.model.ConversationInfo;
import io.rong.imkit.widget.provider.InputProvider;
import io.rong.imlib.model.Conversation;

/**
 * Created by weiqinxiao on 16/4/27.
 */
public class RongCallService {
    private static Context mContext;
    private static boolean uiReady;

    private static RongCallSession mCallSession;

    private static IRongReceivedCallListener callListener = new IRongReceivedCallListener() {
        @Override
        public void onReceivedCall(final RongCallSession callSession) {
            RLog.d("VoIPReceiver", "onReceivedCall");
            if (uiReady) {
                startVoIPActivity(mContext, callSession, false);
            } else {
                mCallSession = callSession;
            }
        }

        @Override
        public void onCheckPermission(RongCallSession callSession) {
            RLog.d("VoIPReceiver", "onCheckPermissions");
            if (uiReady) {
                startVoIPActivity(mContext, callSession, true);
            }
        }
    };

    public static void onInit(Context context) {
        mContext = context.getApplicationContext();

        RongIM.registerMessageTemplate(new CallEndMessageItemProvider());
        RongCallClient.setReceivedCallListener(callListener);
    }

    public static void onUiReady() {
        uiReady = true;
        if (mCallSession != null) {
            startVoIPActivity(mContext, mCallSession, false);
        }
    }

    public static void onConnected() {
        InputProvider.ExtendProvider[] audioProvider = {
                new AudioCallInputProvider(RongContext.getInstance())
        };
        InputProvider.ExtendProvider[] videoProvider = {
                new VideoCallInputProvider(RongContext.getInstance())
        };

        if (RongCallClient.getInstance().isAudioCallEnabled(Conversation.ConversationType.PRIVATE)) {
            RongIM.addInputExtensionProvider(Conversation.ConversationType.PRIVATE, audioProvider);
        }
        if (RongCallClient.getInstance().isAudioCallEnabled(Conversation.ConversationType.DISCUSSION)) {
            RongIM.addInputExtensionProvider(Conversation.ConversationType.DISCUSSION, audioProvider);
        }
        if (RongCallClient.getInstance().isVideoCallEnabled(Conversation.ConversationType.PRIVATE)) {
            RongIM.addInputExtensionProvider(Conversation.ConversationType.PRIVATE, videoProvider);
        }
        if (RongCallClient.getInstance().isVideoCallEnabled(Conversation.ConversationType.DISCUSSION)) {
            RongIM.addInputExtensionProvider(Conversation.ConversationType.DISCUSSION, videoProvider);
        }

        List<ConversationInfo> infoList = RongContext.getInstance().getCurrentConversationList();
        if (infoList.size() > 0) {
            Conversation.ConversationType conversationType = infoList.get(0).getConversationType();
            String targetId = infoList.get(0).getTargetId();
            Conversation conversation = Conversation.obtain(conversationType, targetId, null);
            for (InputProvider provider : RongContext.getInstance().getRegisteredExtendProviderList(Conversation.ConversationType.PRIVATE)) {
                if (provider instanceof VideoCallInputProvider || provider instanceof AudioCallInputProvider) {
                    provider.setCurrentConversation(conversation);
                }
            }
            for (InputProvider provider : RongContext.getInstance().getRegisteredExtendProviderList(Conversation.ConversationType.DISCUSSION)) {
                if (provider instanceof VideoCallInputProvider || provider instanceof AudioCallInputProvider) {
                    provider.setCurrentConversation(conversation);
                }
            }
        }
    }

    public static void startVoIPActivity(Context context, final RongCallSession callSession, boolean startForCheckPermissions) {
        RLog.d("VoIPReceiver", "startVoIPActivity");
        String action;
        if (callSession.getConversationType().equals(Conversation.ConversationType.DISCUSSION)) {
            if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {
                action = RongVoIPIntent.RONG_INTENT_ACTION_VOIP_MULTIVIDEO;
            } else {
                action = RongVoIPIntent.RONG_INTENT_ACTION_VOIP_MULTIAUDIO;
            }
            Intent intent = new Intent(action);
            intent.putExtra("callSession", callSession);
            intent.putExtra("callAction", RongCallAction.ACTION_INCOMING_CALL.getName());
            if (startForCheckPermissions) {
                intent.putExtra("checkPermissions", true);
            } else {
                intent.putExtra("checkPermissions", false);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setPackage(context.getPackageName());
            context.startActivity(intent);
        } else {
            if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {
                action = RongVoIPIntent.RONG_INTENT_ACTION_VOIP_SINGLEVIDEO;
            } else {
                action = RongVoIPIntent.RONG_INTENT_ACTION_VOIP_SINGLEAUDIO;
            }
            Intent intent = new Intent(action);
            intent.putExtra("callSession", callSession);
            intent.putExtra("callAction", RongCallAction.ACTION_INCOMING_CALL.getName());
            if (startForCheckPermissions) {
                intent.putExtra("checkPermissions", true);
            } else {
                intent.putExtra("checkPermissions", false);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setPackage(context.getPackageName());
            context.startActivity(intent);
        }
    }
}
