package com.arsiwala.shamoil.sppuquestionpapers;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

// TODO : read message and open a link
public class myFirebaseMessagingService extends FirebaseMessagingService {
    String TAG = "SQP_LOGS_NOTIFICATION";

    public myFirebaseMessagingService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
    }
}
