package com.example.love.Ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.airbnb.lottie.LottieAnimationView
import com.example.love.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.snapshots

class SplashScreeen : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screeen)

        val lottieAnimationView: LottieAnimationView = findViewById(R.id.lottieAnimationView)
        lottieAnimationView.playAnimation()
        auth = FirebaseAuth.getInstance()

    }

    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed({
            if(auth.currentUser!=null) {
                val databasref = FirebaseDatabase.getInstance().getReference("User").child(auth.uid.toString())

                databasref.addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val connect = snapshot.child("connection").getValue(String::class.java)

                        if(connect.isNullOrEmpty()){
                            startActivity(Intent(this@SplashScreeen, AllUserPage::class.java))
                            finish()
                        }
                        else{
                            startActivity(Intent(this@SplashScreeen, MainActivity::class.java))
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })


            }
            else{
                startActivity(Intent(this, Signin::class.java))
                finish()
            }
        },2500)
    }
}