@file:Suppress("DEPRECATION")

package com.dev.abhinav.instagramclone

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.*
import kotlin.collections.HashMap

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        signin_btn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
        
        signup_btn.setOnClickListener {
            createAccount()
        }
    }

    private fun createAccount() {
        val fullname = fullname_signup.text.toString()
        val username = username_signup.text.toString()
        val email = email_signup.text.toString()
        val password = password_signup.text.toString()

        when {
            TextUtils.isEmpty(fullname) -> Toast.makeText(this, "Full Name is required", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(username) -> Toast.makeText(this, "Username is required", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(email) -> Toast.makeText(this, "Email is required", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this, "Password is required", Toast.LENGTH_LONG).show()

            else -> {
                val progressDialog = ProgressDialog(this@SignUpActivity)
                progressDialog.setTitle("SignUp")
                progressDialog.setMessage("Please wait...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val auth = FirebaseAuth.getInstance()
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            saveUserInfo(fullname, username, email, progressDialog)
                        }
                        else {
                            Toast.makeText(this, "Error: " + task.exception.toString(), Toast.LENGTH_LONG).show()
                            auth.signOut()
                            progressDialog.dismiss()
                        }
                    }
            }
        }
    }

    private fun saveUserInfo(fullname: String, username: String, email: String, progressDialog: ProgressDialog) {
        val userID = FirebaseAuth.getInstance().currentUser!!.uid
        val userRef = FirebaseDatabase.getInstance().reference.child("Users")
        val map = HashMap<String, Any>()
        map["uid"] = userID
        map["fullname"] = fullname.toLowerCase(Locale.ROOT)
        map["username"] = username.toLowerCase(Locale.ROOT)
        map["email"] = email
        map["bio"] = ""
        map["image"] = "https://firebasestorage.googleapis.com/v0/b/instagram-clone-8ae8c.appspot.com/o/Default%20Images%2Fprofile.png?alt=media&token=2517bbf1-ad8d-4198-8625-bb8cc3d29481"

        userRef.child(userID).setValue(map)
            .addOnCompleteListener { task ->
            if(task.isSuccessful) {
                progressDialog.dismiss()
                //Toast.makeText(this, "Error: " + "Account Created!", Toast.LENGTH_LONG).show()

                FirebaseDatabase.getInstance().reference
                    .child("Follow").child(userID)
                    .child("Following").child(userID)
                    .setValue(true)

                val intent = (Intent(this@SignUpActivity, MainActivity::class.java))
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            else {
                Toast.makeText(this, "Error: " + task.exception.toString(), Toast.LENGTH_LONG).show()
                FirebaseAuth.getInstance().signOut()
                progressDialog.dismiss()
            }
        }
    }
}