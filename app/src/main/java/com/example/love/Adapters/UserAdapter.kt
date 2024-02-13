package com.example.love.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.example.love.Modals.RequestUserData
import com.example.love.Notification.NotificationRequests
import com.example.love.R
import com.example.love.Volley.VolleySingleton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject

class UserAdapter(
    private val context: Context,
    private val userlist: List<RequestUserData>,
    private val currentuserData: RequestUserData
):RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val Userimage = itemView.findViewById<ImageView>(R.id.dpImage)
        val UserName = itemView.findViewById<TextView>(R.id.UserNameRequest)
        val sendButton = itemView.findViewById<Button>(R.id.requestButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.itemviewuser,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userlist.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val auth = FirebaseAuth.getInstance()
        val databaseref = FirebaseDatabase.getInstance().getReference("Requests").child(auth.uid.toString())
        val databaseref2 = FirebaseDatabase.getInstance().getReference("Accepts")
        val databaseref3 = FirebaseDatabase.getInstance().getReference("User")

        databaseref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val token = userlist[holder.adapterPosition].token
                val positionTokenSnapshot = snapshot.child(token)
                if(positionTokenSnapshot.exists()){
                    holder.sendButton.setBackgroundResource(R.drawable.user_button_edge)
                    holder.sendButton.text = "Requested"
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        holder.UserName.text = userlist[position].username
        Glide.with(context).load(userlist[position].image).placeholder(R.drawable.baseline_person_24).into(holder.Userimage)

        holder.sendButton.setOnClickListener {
            holder.sendButton.setBackgroundResource(R.drawable.user_button_edge)
            holder.sendButton.text = "Requested"

            databaseref.child(userlist[holder.adapterPosition].token).setValue(userlist[position])
            databaseref2.child(userlist[holder.adapterPosition].token).child(auth.uid.toString()).setValue(currentuserData)
            databaseref3.child(userlist[holder.adapterPosition].token).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val deviceToken = snapshot.child("deviceToken").getValue(String::class.java)
                    sendNotification(deviceToken.toString(),userlist[holder.adapterPosition].username,"Request")
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        }
    }

    private fun sendNotification(token: String, title: String, message: String) {


        val volleySingleton  : VolleySingleton = VolleySingleton(context)
        val fcmUrl = "https://fcm.googleapis.com/fcm/send"
        val serverKey = "AAAA078dJic:APA91bGNGV_pCq3NJ5e9_BBD6v2mnKPS4p9syZlBySlwdZGhPkRApcE5q4XlOWZpHATRxZUzlfw8piBzr1krwlNfuVQoaMpPHIC1_zzLJG0a7-H09syo8V9_BIouyzCMi6ODgjIjoDOp" // Your FCM server key

        val notification = JSONObject()
        val notificationBody = JSONObject()
        val JsonData = JSONObject()

        notificationBody.put("title", title)
        notificationBody.put("body", message)

        JsonData.put("type","request")

        notification.put("to", token)
        notification.put("notification", notificationBody)
        notification.put("data",JsonData)

        val request = object : JsonObjectRequest(
            Request.Method.POST, fcmUrl, notification,
            Response.Listener {
                // Notification sent successfully
            },
            Response.ErrorListener {
                // Error occurred while sending notification
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "key=$serverKey"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        // Add the request to the RequestQueue
        volleySingleton.addToRequestQueue(request)
    }
}