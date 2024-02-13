package com.example.love.Adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.example.love.R
import com.example.love.Ui.MainActivity
import com.example.love.Volley.VolleySingleton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject

class AcceptanceAdapter(
    private val context: Context,
    private val userlist: List<RequestUserData>,
    private val currentuserData: RequestUserData,
): RecyclerView.Adapter<AcceptanceAdapter.ViewHolder>()  {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val Userimage = itemView.findViewById<ImageView>(R.id.dpImageacc)
        val UserName = itemView.findViewById<TextView>(R.id.UserNameacc)
        val sendButton = itemView.findViewById<Button>(R.id.acceptButton)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcceptanceAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.itemviewuseraccept,parent,false)
        return AcceptanceAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val auth = FirebaseAuth.getInstance()
        val databaseref2 = FirebaseDatabase.getInstance().getReference("Accepts")
        val databaseref1 = FirebaseDatabase.getInstance().getReference("Requests")
        val databaseref3  = FirebaseDatabase.getInstance().getReference("User")

        holder.UserName.text = userlist[position].username
        Glide.with(context).load(userlist[position].image).placeholder(R.drawable.baseline_person_24).into(holder.Userimage)

        holder.sendButton.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION && position < userlist.size) {
                val userid = userlist[position].token
                databaseref3.child(userlist[position].token).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val deviceToken = snapshot.child("deviceToken").getValue(String::class.java)
                        if (!deviceToken.isNullOrEmpty()) {
                            sendNotification(deviceToken, userlist[position].username, "Accepted")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle onCancelled event
                    }
                })

                // Perform database operations
                databaseref1.child(auth.uid.toString()).child(userlist[position].token).removeValue()
                databaseref1.child(userlist[position].token).child(auth.uid.toString()).removeValue()

                databaseref2.child(auth.uid.toString()).child(userlist[position].token).removeValue()
                databaseref2.child(userlist[position].token).child(auth.uid.toString()).removeValue()

                databaseref3.child(auth.uid.toString()).child("connection").setValue(userid)
                databaseref3.child(userid).child("connection").setValue(auth.uid.toString())

                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("connect","yes")
                context.startActivity(intent)
                (context as? Activity)?.finish()
            }

        }
    }

    override fun getItemCount(): Int {
        return userlist.size
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

        JsonData.put("type","accept")

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