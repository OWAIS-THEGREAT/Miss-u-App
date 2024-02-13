package com.example.love.Notification

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage

class NotificationRequests {

    fun sendNotificationToUser(uid: String, message: String) {
        // Retrieve the device token of the user from the Firebase Realtime Database
        FirebaseDatabase.getInstance().getReference("User").child(uid).child("deviceToken")
            .get().addOnSuccessListener { dataSnapshot ->
                val deviceToken = dataSnapshot.getValue(String::class.java)
                if (deviceToken != null) {
                    // Send FCM message to the device token
                    FirebaseMessaging.getInstance().send(
                        RemoteMessage.Builder(deviceToken)
                            .setMessageId(java.lang.String.valueOf(System.currentTimeMillis()))
                            .setData(mapOf("message" to message))
                            .build()
                    )
                } else {
                    // Handle case where device token is not found
                }
            }.addOnFailureListener { exception ->
                // Handle any errors that occurred while retrieving device token
            }
    }
}