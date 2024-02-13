package com.example.love.Ui

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.love.Notification.NotificationRequests
import com.example.love.R
import com.example.love.Volley.VolleySingleton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject

class Signin : AppCompatActivity() {

    private lateinit var username : EditText
    private lateinit var password : EditText
    private lateinit var login : Button
    private lateinit var create  :TextView

    private lateinit var dialog : Dialog

    private lateinit var auth : FirebaseAuth

    private val volleySingleton: VolleySingleton by lazy {
        VolleySingleton.getInstance(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        auth = FirebaseAuth.getInstance()
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_loding)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(0))


        username = findViewById(R.id.UsernameSin)
        password = findViewById(R.id.PasswordSin)
        login = findViewById(R.id.Login)
        create = findViewById(R.id.create)

        create.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
            finish()
        }

//

        login.setOnClickListener {
            dialog.show()
            if(username.text.isEmpty() && password.text.isEmpty()){
                Toast.makeText(this,"Enter Username & Password",Toast.LENGTH_LONG).show()
            }
            else{

                auth.signInWithEmailAndPassword(username.text.toString(),password.text.toString()).addOnSuccessListener {
                    fetchAndStoreDeviceToken()
                    val user = auth.currentUser
                    if((user != null) && user.isEmailVerified){
                        val databasref = FirebaseDatabase.getInstance().getReference("User").child(auth.uid.toString())

                        databasref.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val connect = snapshot.child("connection").getValue(String::class.java)

                                if(connect.isNullOrEmpty()){
                                    dialog.hide()
                                    startActivity(Intent(this@Signin, AllUserPage::class.java))
                                    finish()
                                }
                                else{
                                    dialog.hide()
                                    startActivity(Intent(this@Signin, MainActivity::class.java))
                                    finish()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                    }
                    else{
                        Toast.makeText(this,"Email not verified",Toast.LENGTH_LONG).show()
                        user?.sendEmailVerification()
                    }

                }
            }
        }

    }

    override fun onStart() {
        super.onStart()

        val user = auth.currentUser
        if((user != null) && user.isEmailVerified){
            val databasref = FirebaseDatabase.getInstance().getReference("User").child(auth.uid.toString())
            fetchAndStoreDeviceToken()
            databasref.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val connect = snapshot.child("connection").getValue(String::class.java)

                    if(connect.isNullOrEmpty()){
                        startActivity(Intent(this@Signin, AllUserPage::class.java))
                        finish()
                    }
                    else{
                        startActivity(Intent(this@Signin, MainActivity::class.java))
                        finish()
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }

    }


    private fun fetchAndStoreDeviceToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            // Once the token is fetched successfully, store it in the database
            storeTokenInDatabase(token)

        }.addOnFailureListener { exception ->
            // Handle failure to fetch token
            Log.e(TAG, "Failed to fetch device token: ${exception.message}")
        }
    }

    private fun storeTokenInDatabase(token: String) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            FirebaseDatabase.getInstance().getReference("User").child(uid).child("deviceToken")
                .setValue(token)
                .addOnSuccessListener {
                    Log.d(TAG, "Device token stored in database: $token")
                    // Proceed to the next activity or perform other operations
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to store device token in database: ${exception.message}")
                    // Handle failure to store token
                }
        }
    }

    companion object {
        private const val TAG = "Signin"
    }

}