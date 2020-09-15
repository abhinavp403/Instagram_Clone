package com.dev.abhinav.instagramclone.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.abhinav.instagramclone.R
import com.dev.abhinav.instagramclone.adapter.PostAdapter
import com.dev.abhinav.instagramclone.model.Post
import com.dev.abhinav.instagramclone.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private lateinit var postAdapter: PostAdapter
    private lateinit var postList: MutableList<Post>
    private lateinit var followingList: MutableList<Post>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.fragment_home, container, false)

        val recyclerView: RecyclerView?
        recyclerView = view.findViewById(R.id.recycler_view_home)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it, postList as ArrayList<Post>) }!!
        recyclerView.adapter = postAdapter

        checkFollowing()

        return view
    }

    private fun checkFollowing() {
        followingList = ArrayList()
        val followingRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("Following")
        followingRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()) {
                    (followingList as ArrayList<String>).clear()
                    for(snapshot in dataSnapshot.children) {
                        snapshot.key?.let { (followingList as ArrayList<String>).add(it) }
                    }
                    retrievePosts()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun retrievePosts() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postList.clear()
                for(snapshot in dataSnapshot.children) {
                    val post = snapshot.getValue(Post::class.java)
                    for(id in (followingList as ArrayList<String>)) {
                        if(post!!.publisher == id) {
                            postList.add(post)
                        }
                        postAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}