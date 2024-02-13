package com.example.love.Ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.love.Adapters.AcceptanceAdapter
import com.example.love.Modals.RequestUserData
import com.example.love.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Acceptance_User : AppCompatActivity() {

    private lateinit var recyclerView : RecyclerView
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseref : DatabaseReference
    private lateinit var databaseref1 : DatabaseReference

    private lateinit var auth : FirebaseAuth

    private lateinit var userlist : MutableList<RequestUserData>
    private lateinit var currentuserData : RequestUserData


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acceptance_user)

        recyclerView = findViewById(R.id.recyclerViewUsersaccept)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseref = database.getReference("Accepts").child(auth.uid.toString())
        databaseref1 = database.getReference("User").child(auth.uid.toString())

        databaseref1.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("username").getValue(String::class.java)
                val image = snapshot.child("profileImageUrl").getValue(String::class.java)
                val token = snapshot.child("token").getValue(String::class.java)

                currentuserData = RequestUserData(name.toString(),image.toString(),token.toString())
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


        userlist = mutableListOf()


        databaseref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot in snapshot.children){
                    val name = datasnapshot.child("username").getValue(String::class.java)
                    val image = datasnapshot.child("image").getValue(String::class.java)
                    val token = datasnapshot.key

                    if(auth.uid!=token)
                        userlist.add(RequestUserData(name.toString(),image.toString(),token.toString()))

                }

                recyclerView.layoutManager = LinearLayoutManager(this@Acceptance_User,
                    LinearLayoutManager.VERTICAL,false)
                recyclerView.adapter = AcceptanceAdapter(this@Acceptance_User,userlist,currentuserData)

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Requests"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Handle the Up button click
                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}