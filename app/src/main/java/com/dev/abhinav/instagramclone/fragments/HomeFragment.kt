package com.dev.abhinav.instagramclone.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.abhinav.instagramclone.R
import com.dev.abhinav.instagramclone.adapter.PostAdapter
import com.dev.abhinav.instagramclone.adapter.StoryAdapter
import com.dev.abhinav.instagramclone.model.Post
import com.dev.abhinav.instagramclone.model.Story
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private lateinit var postAdapter: PostAdapter
    private lateinit var postList: MutableList<Post>
    private lateinit var followingList: MutableList<String>
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var storyList: MutableList<Story>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.fragment_home, container, false)

        val recyclerViewPost: RecyclerView = view.findViewById(R.id.recycler_view_home)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerViewPost.layoutManager = linearLayoutManager
        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it, postList as ArrayList<Post>) }!!
        recyclerViewPost.adapter = postAdapter

        val recyclerViewStory: RecyclerView = view.findViewById(R.id.recycler_view_story)
        recyclerViewPost.setHasFixedSize(true)
        val linearLayoutManager2 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewStory.layoutManager = linearLayoutManager2
        storyList = ArrayList()
        storyAdapter = context?.let { StoryAdapter(it, storyList as ArrayList<Story>) }!!
        recyclerViewStory.adapter = storyAdapter

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
                    retrieveStories()
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
                (postList as ArrayList<Post>).clear()
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

    private fun retrieveStories() {
        val storyRef = FirebaseDatabase.getInstance().reference.child("Story")
        storyRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val timeCurrent = System.currentTimeMillis()
                (storyList as ArrayList<Story>).clear()
                (storyList as ArrayList<Story>).add(Story("", 0, 0, "", FirebaseAuth.getInstance().currentUser!!.uid))
                for(id in followingList) {
                    Log.d("abc2222", id)
                    var countStory = 0
                    var story: Story? = null
                    for(snapshot in dataSnapshot.child(id).children) {
                        story = snapshot.getValue(Story::class.java)
                        Log.d("abc", story!!.userid.toString())
                        Log.d("abc", story.storyid.toString())
                        Log.d("abc", story.timeStart.toString())
                        Log.d("abc", story.timeEnd.toString())
                        if(timeCurrent > 1602727543040 && timeCurrent < 1602813942839) {
                            countStory++
                        }
                    }
                    Log.d("here", countStory.toString())
                    if(countStory > 0) {
                        (storyList as ArrayList<Story>).add(story!!)
                    }
                    //Log.d("here", story!!.timeEnd.toString())
                }
                storyAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}