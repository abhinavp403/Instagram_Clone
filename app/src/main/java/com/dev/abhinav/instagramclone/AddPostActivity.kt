package com.dev.abhinav.instagramclone

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_add_post.*
import java.util.*
import kotlin.collections.HashMap

class AddPostActivity : AppCompatActivity() {

    private lateinit var uri: String
    private var imageUri: Uri? = null
    private lateinit var storagePostPicRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        storagePostPicRef = FirebaseStorage.getInstance().reference.child("Posts Pictures")

        save_new_post_btn.setOnClickListener {
            uploadImage()
        }

        CropImage.activity()
            .setAspectRatio(1,1)
            .start(this@AddPostActivity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            image_post.setImageURI(imageUri)
        }
    }

    private fun uploadImage() {
        when {
            desc_post.text.toString() == "" -> Toast.makeText(this, "Please write your description", Toast.LENGTH_LONG).show()
            imageUri == null -> Toast.makeText(this, "Please select image", Toast.LENGTH_LONG).show()
            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Adding New Post")
                progressDialog.setMessage("Please wait, we are adding your post...")
                progressDialog.show()

                //TODO: file extension function - video 15 around 16:00
                val fileref = storagePostPicRef.child(System.currentTimeMillis().toString() + ".jpg")
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
                }).addOnCompleteListener(OnCompleteListener<Uri> { task ->
                    if (task.isSuccessful) {
                        val downloadUrl = task.result
                        uri = downloadUrl.toString()
                        val userRef = FirebaseDatabase.getInstance().reference.child("Posts")
                        val map = HashMap<String, Any>()
                        val postKey = userRef.push().key!!
                        map["postid"] = postKey
                        map["description"] = desc_post.text.toString().toLowerCase(Locale.ROOT)
                        map["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                        map["postimage"] = uri
                        userRef.child(postKey).updateChildren(map)

                        Toast.makeText(this, "Post Uploaded Successfully", Toast.LENGTH_LONG).show()
                        val intent = (Intent(this@AddPostActivity, MainActivity::class.java))
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()
                    } else {
                        progressDialog.dismiss()
                    }
                })
            }
        }
    }
}