/**
 * Copyright Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.NaqelApp.driver.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.NaqelApp.driver.R;
import com.NaqelApp.driver.activity.AcceptOrderActivity;
import com.NaqelApp.driver.model.EventTrip;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFMService";

    // firebase
    private DatabaseReference mFirebaseDatabaseReference;
    private static String sender_id;

    private static String trip_id;
    private static String response;


    private static String sender_name;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle data payload of FCM messages.
        Log.d("hazem", "FCM Message Id: " + remoteMessage.getMessageId());
        Log.d("hazem", "FCM Notification Message: " +
                remoteMessage.getNotification());
        Log.d("hazem", "FCM Data Message: " + remoteMessage.getData().get("id"));


        final Intent intent = new Intent(this, AcceptOrderActivity.class);
        intent.putExtra("order", remoteMessage.getData().get("id"));


        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.looogooo);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder
                .setContentTitle("سائق ناقل")
                .setContentText("لقد استقبلت طلب")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setLargeIcon(image)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        };
//        mFirebaseDatabaseReference.addValueEventListener(postListener);


        // here the oreo notification
        // Sets an ID for the notification, so it can be updated.

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = getString(R.string.default_notification_channel_id);
            NotificationChannel channel = new NotificationChannel(channelId, "this is the notification ",
                    NotificationManager.IMPORTANCE_HIGH);

            channel.setDescription("لقد استقبلت طلب");
            notificationManager.createNotificationChannel(channel);
            notificationBuilder.setChannelId(channelId);
            notificationManager.notify(1, notificationBuilder.build());
        } else {
            notificationManager.notify(0, notificationBuilder.build());
        }
//        notificationManager.notify(1, notificationBuilder.build());



        //todo here if the user open the app show the dialog inside the app and clear the notification
        EventBus.getDefault().post(new EventTrip(remoteMessage.getData().get("id")));






    }
}