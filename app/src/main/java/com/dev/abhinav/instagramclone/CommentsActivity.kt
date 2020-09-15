package com.dev.abhinav.instagramclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.abhinav.instagramclone.adapter.CommentsAdapter
import com.dev.abhinav.instagramclone.model.Comment
import com.dev.abhinav.instagramclone.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_comments.*
import java.util.*
import kotlin.collections.HashMap

class CommentsActivity : AppCompatActivity() {

    private lateinit var postId: String
    private lateinit var publisherId: String
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var commentAdapter: CommentsAdapter
    private lateinit var commentList: MutableList<Comment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val intent = intent
        postId = intent.getStringExtra("postId")!!
        publisherId = intent.getStringExtra("publisherId")!!

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view_comments)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        recyclerView.layoutManager = linearLayoutManager

        commentList = ArrayList()
        commentAdapter = CommentsAdapter(this, commentList)
        recyclerView.adapter = commentAdapter

        userInfo()
        readComments()
        getPostImage()

        post_comment.setOnClickListener{
            if(add_comment.text.toString() == "")
                Toast.makeText(this@CommentsActivity, "Please write comment", Toast.LENGTH_LONG).show()
            else
                addComment()
        }
    }

    private fun addComment() {
        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments")
            .child(postId)
        val map = HashMap<String, Any>()
        map["comment"] = add_comment.text.toString()
        map["publisher"] = firebaseUser.uid
        commentsRef.push().setValue(map)

        addNotification()

        add_comment.text.clear()
    }

    private fun userInfo() {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        userRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(profile_image_comment)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun readComments() {
        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments")
            .child(postId)
        commentsRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()) {
                    commentList.clear()
                    for(snapshot in dataSnapshot.children) {
                        val comment = snapshot.getValue(Comment::class.java)
                        commentList.add(comment!!)
                    }
                    commentAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getPostImage() {
        val postRef = FirebaseDatabase.getInstance().reference
            .child("Posts")
            .child(postId)
            .child("postimage")
        postRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val image = snapshot.value.toString()
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(post_image_comment)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun addNotification() {
        val postRef = FirebaseDatabase.getInstance().reference
            .child("Notifications")
            .child(publisherId)

        val map = HashMap<String, Any>()
        map["userid"] = firebaseUser.uid
        map["text"] = "commented: " + add_comment!!.text.toString()
        map["postid"] = postId
        map["ispost"] = true
        postRef.push().setValue(map)
    }
}