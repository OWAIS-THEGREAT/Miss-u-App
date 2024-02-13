package com.example.love

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.love.Ui.Acceptance_User
import com.example.love.Ui.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM Token", token)

    }



    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val type = remoteMessage.data["type"]

        if(type=="request"){
            openRequestPage(remoteMessage.notification?.title,remoteMessage.notification?.body)
        }
        else if(type=="accept"){
            openMainPage(remoteMessage.notification?.title,remoteMessage.notification?.body)
        }

        Log.d("@@@@",type.toString())
//        getmessage(remoteMessage.notification?.title,remoteMessage.notification?.body)
    }

    private fun openMainPage(title: String?, body: String?) {
        val channelId = "push_noti"

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("connect","yes")
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

        // Use PendingIntent.FLAG_IMMUTABLE to comply with Android S+ requirements
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.sciencelogo)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(this)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Push Notifications", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(102, builder.build())
    }

    private fun openRequestPage(title: String?, body: String?) {
        val channelId = "push_noti"

        val intent = Intent(this, Acceptance_User::class.java)

        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        // Use PendingIntent.FLAG_IMMUTABLE to comply with Android S+ requirements
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.sciencelogo)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(this)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Push Notifications", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(102, builder.build())
    }


    override fun onMessageSent(messageId: String) {
        super.onMessageSent(messageId)
        Log.d("@@@@", "Message sent successfully: $messageId")
    }

    override fun onSendError(message: String, exception: Exception) {
        super.onSendError(message, exception)
        Log.e("@@@@", "Message sending failed: $message, ${exception.message}", exception)
    }

}
