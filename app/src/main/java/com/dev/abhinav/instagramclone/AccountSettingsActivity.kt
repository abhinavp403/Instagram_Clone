package com.dev.abhinav.instagramclone

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.dev.abhinav.instagramclone.model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_account_settings.*
import java.util.*
import kotlin.collections.HashMap

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private var checker: String = ""
    private lateinit var uri: String
    private var imageUri: Uri? = null
    private lateinit var storageProfilePicRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Picture")

        logout_btn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = (Intent(this@AccountSettingsActivity, SignInActivity::class.java))
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        change_image_btn.setOnClickListener {
            checker = "clicked"
            CropImage.activity()
                .setAspectRatio(1,1)
                .start(this@AccountSettingsActivity)
        }

        save_info_profile_btn.setOnClickListener {
            if (checker == "clicked") {
                uploadImageUpdateUserInfo()
            } else {
                updateUserInfo()
            }
        }

        userInfo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            profile_image.setImageURI(imageUri)
        }
    }

    private fun uploadImageUpdateUserInfo() {
        when {
            fullname.text.toString() == "" -> Toast.makeText(this, "Please write your full name", Toast.LENGTH_LONG).show()
            username.text.toString() == "" -> Toast.makeText(this, "Please write your username", Toast.LENGTH_LONG).show()
            bio.text.toString() == "" -> Toast.makeText(this, "Please write your bio", Toast.LENGTH_LONG).show()
            imageUri == null -> Toast.makeText(this, "Please select image", Toast.LENGTH_LONG).show()
            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait, we are updating your profile...")
                progressDialog.show()

                val fileref = storageProfilePicRef.child(firebaseUser.uid + ".jpg")
                val uploadTask: StorageTask<*>
                uploadTask = fileref.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (task.isSuccessful) {
                        task.exception?.let {
                            progressDialog.dismiss()
                            throw it
                        }
                    }
                    return@Continuation fileref.downloadUrl
                }).addOnCompleteListener (OnCompleteListener<Uri> { task ->
                        if (task.isSuccessful) {
                            val downloadUrl = task.result
                            uri = downloadUrl.toString()
                            val userRef = FirebaseDatabase.getInstance().reference.child("Users")
                            val map = HashMap<String, Any>()
                            map["fullname"] = fullname.text.toString().toLowerCase(Locale.ROOT)
                            map["username"] = username.text.toString().toLowerCase(Locale.ROOT)
                            map["bio"] = bio.text.toString().toLowerCase(Locale.ROOT)
                            map["image"] = uri
                            userRef.child(firebaseUser.uid).updateChildren(map)

                            Toast.makeText(this, "Account Information Updated", Toast.LENGTH_LONG).show()
                            val intent = (Intent(this@AccountSettingsActivity, MainActivity::class.java))
                            startActivity(intent)
                            finish()
                            progressDialog.dismiss()
                        }
                        else {
                            progressDialog.dismiss()
                        }
                })
            }
        }
    }

    private fun userInfo() {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        userRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(profile_image)
                    username.setText(user.username)
                    fullname.setText(user.fullname)
                    bio.setText(user.bio)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun updateUserInfo() {
        when {
            fullname.text.toString() == "" -> Toast.makeText(this, "Please write your full name", Toast.LENGTH_LONG).show()
            username.text.toString() == "" -> Toast.makeText(this, "Please write your username", Toast.LENGTH_LONG).show()
            bio.text.toString() == "" -> Toast.makeText(this, "Please write your bio", Toast.LENGTH_LONG).show()
            else -> {
                val userRef = FirebaseDatabase.getInstance().reference.child("Users")
                val map = HashMap<String, Any>()
                map["fullname"] = fullname.text.toString().toLowerCase(Locale.ROOT)
                map["username"] = username.text.toString().toLowerCase(Locale.ROOT)
                map["bio"] = bio.text.toString().toLowerCase(Locale.ROOT)
                userRef.child(firebaseUser.uid).updateChildren(map)

                Toast.makeText(this, "Account Information Updated", Toast.LENGTH_LONG).show()
                val intent = (Intent(this@AccountSettingsActivity, MainActivity::class.java))
                startActivity(intent)
                finish()
            }
        }
    }
}