package com.pinasystems.servicebasketv2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by admin on 12/17/2016.
 */

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

    //this method will display the notification
    //We are passing the JSONObject that is received from
    //firebase cloud messaging
    private void sendPushNotification(JSONObject json) {
        //optionally we can display the json into log
        Log.e(TAG, "Notification JSON " + json.toString());
        try {
            //getting the json data
            JSONObject data = json.getJSONObject("data");

            //parsing json data
            String title = data.getString("title");
            String message = data.getString("message");
            String imageUrl = data.getString("image");

            //creating object
            SBNotificationManager mNotificationManager = new SBNotificationManager(getApplicationContext());

            //creating an intent for the notification
            Intent intent;
            String account = ((DataBank)getApplication()).getAccount();
            if(account.equalsIgnoreCase("provider")){
                intent = new Intent(getApplicationContext(), ProviderMainActivity.class);
            }else if (account.equalsIgnoreCase("requester")){
                intent = new Intent(getApplicationContext(), RequesterMainActivity.class);
            }else{
                intent = new Intent(getApplicationContext(),LoginActivity.class);
            }
            //if there is no image
            if(imageUrl.equals("null")){
                //displaying small notification
                mNotificationManager.showSmallNotification(title, message, intent);
            }else{
                //if there is an image
                //displaying a big notification
                mNotificationManager.showBigNotification(title, message, imageUrl, intent);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

}