package com.easycoach.easyloyalty.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.easycoach.easyloyalty.MainActivity;
import com.easycoach.easyloyalty.R;

/**
 * Created by muoki on 2/13/2016.
 */
public class MyService extends Service {
    int numMessages = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        //Toast.makeText(this, "Service was Created", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        //Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mNotifyBuilder;
        NotificationManager mNotificationManager;
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Sets an ID for the notification, so it can be updated
        int notifyID = 9001;
        mNotifyBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Sync Needed")
                //.setContentText("You've received new messages.")
                .setSmallIcon(R.mipmap.ic_launcher);
        mNotifyBuilder.setContentInfo("Please SYNC to reflect new MySQL DB changes");
        // Set pending intent
        mNotifyBuilder.setContentIntent(resultPendingIntent);
        // Set Vibrate, Sound and Light
        int defaults = 0;
        defaults = defaults | Notification.DEFAULT_LIGHTS;
        defaults = defaults | Notification.DEFAULT_VIBRATE;
        defaults = defaults | Notification.DEFAULT_SOUND;
        mNotifyBuilder.setDefaults(defaults);
        // Set the content for Notification
        mNotifyBuilder.setContentText(intent.getStringExtra("intntdata"));
        // Set autocancel
        mNotifyBuilder.setAutoCancel(true);
        // Post a notification
        mNotificationManager.notify(notifyID, mNotifyBuilder.build());
    }

    @Override
    public void onDestroy() {
        //Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }
}
