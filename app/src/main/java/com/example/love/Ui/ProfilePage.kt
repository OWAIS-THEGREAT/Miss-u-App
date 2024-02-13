package com.example.love.Ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.*
import android.provider.MediaStore.Images.*
import android.provider.MediaStore.Images.Media.*
import android.service.autofill.UserData
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.love.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.net.URI

class ProfilePage : AppCompatActivity() {

    private lateinit var profileImage : ImageView
    private lateinit var name : EditText
    private lateinit var connect : TextView
    private lateinit var camera : FloatingActionButton
    private lateinit var saveBut : Button
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private lateinit var database : FirebaseDatabase
    private lateinit var storage : FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var auth : FirebaseAuth
    private lateinit var ref : DatabaseReference

    private lateinit var sharedPreferences : SharedPreferences

    private lateinit var dialog : Dialog

    private var imageuri : Uri? = null
    companion object {
        const val PICK_IMAGE_REQUEST = 1
        const val CAMERA_REQUEST = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_page)

        profileImage = findViewById(R.id.profilePic)
        name = findViewById(R.id.profileName)
        connect = findViewById(R.id.profileConnectiontext)
        camera = findViewById(R.id.cameraButton)
        saveBut = findViewById(R.id.saveprofile)

        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_loding)
        dialog.show()

        if(dialog.window!=null){
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialog.setCancelable(false)

        sharedPreferences = getSharedPreferences("Owais", MODE_PRIVATE)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        ref = database.getReference("User")
        storageReference = storage.getReference("Images")
        camera.setOnClickListener {
            selectimage()
        }

        inital()

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                onActivityResult(PICK_IMAGE_REQUEST, Activity.RESULT_OK, data)
            }
        }

        saveBut.setOnClickListener {
//            dialog.show()
//            Toast.makeText(this,"hello",Toast.LENGTH_LONG).show()
            if(imageuri!=null)
            uploadImageToStorage(imageuri!!)
            else{
                SavetoDabaseWithoutImage()
            }
        }


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Profile"
    }



    private fun selectimage() {
        val galleryIntent = Intent(Intent.ACTION_PICK, EXTERNAL_CONTENT_URI)
        galleryIntent.type = "image/*"

        val cameraIntent = Intent(ACTION_IMAGE_CAPTURE)

        val chooserIntent = Intent.createChooser(galleryIntent, "Select Image")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

        imagePickerLauncher.launch(chooserIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    val selectedImageUri = data?.data
                    // Handle the selected image URI, e.g., set it to the ImageView
                    profileImage.setImageURI(selectedImageUri)

                    selectedImageUri?.let { uri ->
                        imageuri = uri
                    }
                }
                CAMERA_REQUEST -> {
                    val photo = data?.extras?.get("data") as Bitmap
                    // Handle the captured photo, e.g., set it to the ImageView
                    profileImage.setImageBitmap(photo)

                }
            }
        }
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


    private fun inital() {
//        dialog.show()
        val uid = auth.uid
        ref = ref.child(uid.toString())
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                val username = snapshot.child("username").getValue(String::class.java)
                val connection = snapshot.child("connection").getValue(String::class.java)
                val profileimage = snapshot.child("profileImageUrl").getValue(String::class.java)
                name.setText(username.toString())
                if(!connection.isNullOrEmpty()){
                    connect.text = connection.toString()
                }
                else{
                    connect.text = "Unmatched"
                }
                profileimage?.let {
                    Glide.with(this@ProfilePage)
                        .load(it)
                        .placeholder(R.drawable.baseline_person_24) // Placeholder image while loading // Image to display in case of loading error
                        .into(profileImage)

                }
                dialog.dismiss()
                dialog.hide()

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }


    private fun uploadImageToStorage(imageUri: Uri) {

        dialog.show()
        val uid = auth.uid
        val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/$uid.jpg")
        val uploadTask = storageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            // Image upload success
            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                // Image download URL obtained, save it to Realtime Database
                saveImageUrlToDatabase(downloadUri.toString())
            }
        }.addOnFailureListener { exception ->
            // Handle image upload failure
            Toast.makeText(this, "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageUrlToDatabase(imageUrl: String) {
        val uid = auth.uid

        val userRef = database.getReference("User").child(uid.toString())
        userRef.child("profileImageUrl").setValue(imageUrl)
        userRef.child("username").setValue(name.text.toString())
        dialog.hide()
        dialog.dismiss()
    }

    private fun SavetoDabaseWithoutImage() {
        val uid = auth.uid

        val userRef = database.getReference("User").child(uid.toString())
        userRef.child("username").setValue(name.text.toString())
        Toast.makeText(this@ProfilePage,"Saved",Toast.LENGTH_LONG).show()
        dialog.hide()
        dialog.dismiss()
    }

    fun drawableResToUri(context: Context, drawableId: Int): Uri {
        return Uri.parse("android.resource://" + context.packageName + "/" + drawableId)
    }
}