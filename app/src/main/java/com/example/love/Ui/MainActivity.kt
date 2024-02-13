package com.example.love.Ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.drawerlayout.widget.DrawerLayout
import com.example.love.R
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(),SensorEventListener{

    private lateinit var auth : FirebaseAuth
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    private lateinit var sensorManager: SensorManager
    private var mAccelerometer: Sensor? = null

    private lateinit var Xaxis : TextView
    private lateinit var Yaxis : TextView
    private lateinit var Zaxis : TextView
    var check1 = false
    var check2 = false
    var cnt = 0
    private var processingSensorValues = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        drawerLayout  =findViewById(R.id.drawerlayout)
        navigationView = findViewById(R.id.navView)

        toggle = ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navigationView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.NavProfile->{
                    startActivity(Intent(this,ProfilePage::class.java))
                }

                R.id.NavRequests->startActivity(Intent(this,Acceptance_User::class.java))

                R.id.navLogout-> {
                    auth.signOut()
                    startActivity(Intent(this,Signin::class.java))
                    finish()
                }
            }

            true
        }


        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onSensorChanged(sense: SensorEvent?) {
        if (processingSensorValues) {
            val x = sense!!.values[0]
            val y = sense.values[1]
            val z = sense.values[2]

            if (sense.values[0] > 4) {
                check1 = true
            }
            if (sense.values[0] < -4) {
                check2 = true
            }

            if (check1 && check2) {
                check1 = false
                check2 = false

                processingSensorValues = false // Pause processing sensor values

                val alertDialog = AlertDialog.Builder(this)

                alertDialog.setTitle("Heart Connected")
                alertDialog.setMessage("You are soulmates")
                alertDialog.setPositiveButton("Love") { dialog, which ->
                    dialog.dismiss()

                    processingSensorValues = true // Resume processing sensor values
                }

                val alert: AlertDialog = alertDialog.create()
                alert.show()
            }

        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onResume() {
        super.onResume()

        mAccelerometer.also {accel->
            sensorManager.registerListener(this,accel,SensorManager.SENSOR_DELAY_NORMAL)
        }


    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()

        if(cnt==0) {
            val connect = intent.getStringExtra("connect")
//        Toast.makeText(this@MainActivity,connect.toString(),Toast.LENGTH_LONG).show()
            if (connect.toString() == "yes") {
                val dialog = Dialog(this)
                dialog.setContentView(R.layout.heart_connect)
                dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
                dialog.show()
                Handler(Looper.getMainLooper()).postDelayed({
                    dialog.dismiss()
                    dialog.hide()
                }, 3000)
            }
            cnt++
        }

    }
}