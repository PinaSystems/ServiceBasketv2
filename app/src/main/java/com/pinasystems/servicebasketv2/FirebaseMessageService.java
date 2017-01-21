package com.pinasystems.servicebasketv2;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class FirebaseMessageService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());
            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("message");
            Intent intent;

            String account = ((DataBank) getApplication()).getAccount();
            if (account.equalsIgnoreCase("provider")) {
                intent = new Intent(getApplicationContext(), ProviderMainActivity.class);
            } else if (account.equalsIgnoreCase("requester")) {
                intent = new Intent(getApplicationContext(), RequesterMainActivity.class);
            } else {
                intent = new Intent(getApplicationContext(), LoginActivity.class);
            }
            SBNotificationManager mNotificationManager = new SBNotificationManager(getApplicationContext());
            mNotificationManager.showSmallNotification(title, message, intent);
        }
    }

}