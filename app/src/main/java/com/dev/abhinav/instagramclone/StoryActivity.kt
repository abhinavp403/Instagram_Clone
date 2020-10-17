package com.dev.abhinav.instagramclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.dev.abhinav.instagramclone.model.Story
import com.dev.abhinav.instagramclone.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import jp.shts.android.storiesprogressview.StoriesProgressView
import kotlinx.android.synthetic.main.activity_story.*

class StoryActivity : AppCompatActivity(), StoriesProgressView.StoriesListener {

    private lateinit var currentUserId: String
    private lateinit var userId: String
    private lateinit var imageList: List<String>
    private lateinit var storyIdList: List<String>
    private lateinit var storiesProgressView: StoriesProgressView
    private var counter = 0
    private var pressTime = 0L
    private var limit = 500L
    private val onTouchListener = View.OnTouchListener { view, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                pressTime = System.currentTimeMillis()
                storiesProgressView.pause()
                return@OnTouchListener false
            }
            MotionEvent.ACTION_UP-> {
                val now = System.currentTimeMillis()
                storiesProgressView.resume()
                return@OnTouchListener limit < now - pressTime
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        userId = intent.getStringExtra("userId")!!

        storiesProgressView = findViewById(R.id.stories_progress)

        layout_seen.visibility = View.GONE
        story_delete.visibility = View.GONE

        if(userId == currentUserId) {
            layout_seen.visibility = View.VISIBLE
            story_delete.visibility = View.VISIBLE
        }

        getStories(userId)
        getUserInfo(userId)

        val reverse: View = findViewById(R.id.reverse)
        reverse.setOnClickListener { storiesProgressView.reverse() }
        reverse.setOnTouchListener(onTouchListener)

        val skip: View = findViewById(R.id.skip)
        skip.setOnClickListener { storiesProgressView.skip() }
        skip.setOnTouchListener(onTouchListener)

        seen_number.setOnClickListener {
            val intent = Intent(this@StoryActivity, ShowUserActivity::class.java)
            intent.putExtra("id", userId)
            intent.putExtra("storyid", storyIdList[counter])
            intent.putExtra("title", "views")
            startActivity(intent)
        }

        story_delete.setOnClickListener {
            val ref = FirebaseDatabase.getInstance().reference
                .child("Story")
                .child(userId)
                .child(storyIdList[counter])
            ref.removeValue().addOnCompleteListener {
                if(it.isSuccessful)
                    Toast.makeText(this@StoryActivity, "Story Deleted", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getStories(userId: String) {
        imageList = ArrayList()
        storyIdList = ArrayList()
        val ref = FirebaseDatabase.getInstance().reference
            .child("Story")
            .child(userId)
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (imageList as ArrayList<String>).clear()
                (storyIdList as ArrayList<String>).clear()
                for(snapshot in dataSnapshot.children) {
                    val story = snapshot.getValue<Story>(Story::class.java)
                    val timeCurrent = System.currentTimeMillis()
                    Log.d("ppp", story!!.timeStart.toString())
                    Log.d("ppp", story!!.timeEnd.toString())
                    if(timeCurrent>story!!.timeStart && timeCurrent<story.timeEnd) {
                        (imageList as ArrayList<String>).add(story.imageUrl)
                        (storyIdList as ArrayList<String>).add(story.storyid)
                    }
                }
                storiesProgressView.setStoriesCount((imageList as ArrayList<String>).size)
                storiesProgressView.setStoryDuration(5000L)
                storiesProgressView.setStoriesListener(this@StoryActivity)
                storiesProgressView.startStories(counter)
                Picasso.get().load(imageList[counter]).placeholder(R.drawable.profile).into(image_story)

                addViewToStory(storyIdList[counter])
                seenNumber(storyIdList[counter])
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun addViewToStory(storyId: String) {
        FirebaseDatabase.getInstance().reference
            .child("Story")
            .child(userId)
            .child(storyId)
            .child("views")
            .child(currentUserId)
            .setValue(true)
    }

    private fun seenNumber(storyId: String) {
        val ref = FirebaseDatabase.getInstance().reference
            .child("Story")
            .child(userId)
            .child(storyId)
            .child("views")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                seen_number.text = "" + dataSnapshot.childrenCount
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getUserInfo(userId: String) {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(userId)
        userRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(story_image_profile)
                    story_username.text = user.username
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onNext() {
        Picasso.get().load(imageList[++counter]).placeholder(R.drawable.profile).into(image_story)
        addViewToStory(storyIdList[counter])
        seenNumber(storyIdList[counter])
    }

    override fun onPrev() {
        if(counter - 1 < 0)
            return
        Picasso.get().load(imageList[--counter]).placeholder(R.drawable.profile).into(image_story)
        seenNumber(storyIdList[counter])
    }

    override fun onComplete() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        storiesProgressView.destroy()
    }

    override fun onPause() {
        super.onPause()
        storiesProgressView.pause()
    }

    override fun onResume() {
        super.onResume()
        storiesProgressView.resume()
    }
}