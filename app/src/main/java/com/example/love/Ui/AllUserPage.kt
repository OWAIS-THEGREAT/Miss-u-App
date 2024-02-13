package com.example.love.Ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.love.Adapters.UserAdapter
import com.example.love.Modals.RequestUserData
import com.example.love.R
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AllUserPage : AppCompatActivity() {

    private lateinit var recyclerView : RecyclerView
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseref : DatabaseReference
    private lateinit var auth : FirebaseAuth

    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    private lateinit var currentuserData : RequestUserData

    private lateinit var userlist : MutableList<RequestUserData>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_user_page)

        recyclerView = findViewById(R.id.recyclerViewUsers)

        database = FirebaseDatabase.getInstance()
        databaseref = database.getReference("User")
        auth = FirebaseAuth.getInstance()
        userlist = mutableListOf()


        databaseref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot in snapshot.children){
                    val name = datasnapshot.child("username").getValue(String::class.java)
                    val image = datasnapshot.child("profileImageUrl").getValue(String::class.java)
                    val token = datasnapshot.key

                    if(auth.uid!=token)
                        userlist.add(RequestUserData(name.toString(),image.toString(),token.toString()))
                    else
                        currentuserData = RequestUserData(name.toString(),image.toString(),token.toString())
                }

                recyclerView.layoutManager = LinearLayoutManager(this@AllUserPage,LinearLayoutManager.VERTICAL,false)
                recyclerView.adapter = UserAdapter(this@AllUserPage,userlist,currentuserData)

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


        drawerLayout  =findViewById(R.id.drawerlayoutuser)
        navigationView = findViewById(R.id.navViewuser)

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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

//    override fun onRestart() {
//        super.onRestart()
//        if(auth.currentUser!=null) {
//            val databasref = FirebaseDatabase.getInstance().getReference("User").child(auth.uid.toString())
//
//            databasref.addListenerForSingleValueEvent(object :ValueEventListener{
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val connect = snapshot.child("connection").getValue(String::class.java)
//
//                    if(connect.isNullOrEmpty()){
//                        startActivity(Intent(this@AllUserPage, AllUserPage::class.java))
//                        finish()
//                    }
//                    else{
//                        startActivity(Intent(this@AllUserPage, MainActivity::class.java))
//                        finish()
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//
//                }
//
//            })
//
//
//        }
//    }
    override fun onDestroy() {
        super.onDestroy()
        finish()
    }
}