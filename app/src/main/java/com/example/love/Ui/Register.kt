package com.example.love.Ui

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.love.Modals.UserData
import com.example.love.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Register : AppCompatActivity() {

    private lateinit var username : EditText
    private lateinit var password : EditText
    private lateinit var email: EditText
    private lateinit var Register : Button
    private lateinit var already  :TextView

    private lateinit var dialog : Dialog


    private lateinit var database: FirebaseDatabase

    private lateinit var auth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_loding)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(0))

        username = findViewById(R.id.UsernameReg)
        password = findViewById(R.id.PasswordReg)
        email = findViewById(R.id.EmailReg)
        Register = findViewById(R.id.Register)
        already = findViewById(R.id.Already)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        already.setOnClickListener {
            startActivity(Intent(this, Signin::class.java))
            finish()
        }


        Register.setOnClickListener {
            dialog.show()
            if(username.text.isEmpty() && email.text.isEmpty() && email.text.isEmpty()){
                Toast.makeText(this,"Incomplete Details",Toast.LENGTH_LONG).show()
            }
            else{
                auth.createUserWithEmailAndPassword(email.text.toString().trim(),password.text.toString()).addOnCompleteListener {
                    sendEmailVerification()
                }
            }
        }
    }

    private fun sendEmailVerification(){
        val user = auth.currentUser

        user?.sendEmailVerification()?.addOnCompleteListener {task->
            if(task.isSuccessful){
                dialog.hide()
                val alertDialog = AlertDialog.Builder(this)

                alertDialog.setTitle("Verfication Mail Sent")
                alertDialog.setPositiveButton("OK"){dialog,which->
                    val uid = auth.uid
                    val ref = database.getReference("User").child(uid.toString())

                    val userName = username.text.toString()
                    val emails = email.text.toString().trim()

                    val data = UserData(userName,emails,uid.toString())

                    ref.setValue(data)

                    startActivity(Intent(this, Signin::class.java))

                    dialog.dismiss()
                }
                alertDialog.create().show()

            }
            else{
                val errorMessage = task.exception?.message
            }

        }
    }
}