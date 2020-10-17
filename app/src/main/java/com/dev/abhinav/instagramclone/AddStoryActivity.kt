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
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlin.collections.HashMap

class AddStoryActivity : AppCompatActivity() {

    private lateinit var url: String
    private var imageUrl: Uri? = null
    private lateinit var storageStoryPicRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)

        storageStoryPicRef = FirebaseStorage.getInstance().reference.child("Story Pictures")

        CropImage.activity()
            .setAspectRatio(9,16)
            .start(this@AddStoryActivity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val result = CropImage.getActivityResult(data)
            imageUrl = result.uri
            uploadStory()
        }
    }

    private fun uploadStory() {
        when {
            imageUrl == null -> Toast.makeText(this, "Please select image", Toast.LENGTH_LONG).show()
            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Adding Story")
                progressDialog.setMessage("Please wait, we are adding your story...")
                progressDialog.show()

                //TODO: file extension function - video 15 around 16:00
                val fileref = storageStoryPicRef.child(System.currentTimeMillis().toString() + ".jpg")
                val uploadTask: StorageTask<*>
                uploadTask = fileref.putFile(imageUrl!!)
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
                        url = downloadUrl.toString()
                        val userRef = FirebaseDatabase.getInstance().reference
                            .child("Story")
                            .child(FirebaseAuth.getInstance().currentUser!!.uid)
                        val map = HashMap<String, Any>()
                        val storyId = userRef.push().key
                        val timeEnd = System.currentTimeMillis() + 86400000 //increment of day later
                        map["userid"] = FirebaseAuth.getInstance().currentUser!!.uid
                        map["timestart"] = ServerValue.TIMESTAMP
                        map["timeend"] = timeEnd
                        map["imageurl"] = url
                        map["storyid"] = storyId.toString()
                        userRef.child(storyId.toString()).updateChildren(map)

                        Toast.makeText(this, "Story Uploaded Successfully", Toast.LENGTH_LONG).show()
                        val intent = (Intent(this@AddStoryActivity, MainActivity::class.java))
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