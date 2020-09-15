package com.dev.abhinav.instagramclone.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.abhinav.instagramclone.AccountSettingsActivity
import com.dev.abhinav.instagramclone.R
import com.dev.abhinav.instagramclone.ShowUserActivity
import com.dev.abhinav.instagramclone.adapter.MyImagesAdapter
import com.dev.abhinav.instagramclone.model.Post
import com.dev.abhinav.instagramclone.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.*
import kotlin.collections.ArrayList

class ProfileFragment : Fragment() {

    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var postList: List<Post>
    private lateinit var postListSaved: List<Post>
    private lateinit var mySavedImage: List<String>
    private lateinit var myImagesAdapter: MyImagesAdapter
    private lateinit var myImagesAdapterSavedImage: MyImagesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view  = inflater.inflate(R.layout.fragment_profile, container, false)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if(pref != null) {
            this.profileId = pref.getString("profileId", "none")!!
        }

        if(profileId == firebaseUser.uid) {
            view.edit_account_btn.text = "Edit Profile"
        }
        else {
            checkFollowAndFollowingButtonStatus()
        }

        //recycler view for uploaded images
        val recyclerViewUploadImages: RecyclerView = view.findViewById(R.id.recycler_view_upload_pic)
        recyclerViewUploadImages.setHasFixedSize(true)
        val linearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewUploadImages.layoutManager = linearLayoutManager
        postList = ArrayList()
        myImagesAdapter = context?.let { MyImagesAdapter(it, postList as ArrayList<Post>) }!!
        recyclerViewUploadImages.adapter = myImagesAdapter

        //recycler view for saved images
        val recyclerViewSavedImages: RecyclerView = view.findViewById(R.id.recycler_view_saved_pic)
        recyclerViewSavedImages.setHasFixedSize(true)
        val linearLayoutManager2 = GridLayoutManager(context, 3)
        recyclerViewSavedImages.layoutManager = linearLayoutManager2
        postListSaved = ArrayList()
        myImagesAdapterSavedImage = context?.let { MyImagesAdapter(it, postListSaved as ArrayList<Post>) }!!
        recyclerViewSavedImages.adapter = myImagesAdapterSavedImage

        recyclerViewSavedImages.visibility = View.GONE
        recyclerViewUploadImages.visibility = View.VISIBLE

        val uploadedImageBtn : ImageButton = view.findViewById(R.id.images_grid_view_btn)
        uploadedImageBtn.setOnClickListener {
            recyclerViewSavedImages.visibility = View.GONE
            recyclerViewUploadImages.visibility = View.VISIBLE
        }

        val savedImageBtn : ImageButton = view.findViewById(R.id.images_save_btn)
        savedImageBtn.setOnClickListener {
            recyclerViewSavedImages.visibility = View.VISIBLE
            recyclerViewUploadImages.visibility = View.GONE
        }

        val totalFollowers : TextView = view.findViewById(R.id.total_followers)
        totalFollowers.setOnClickListener {
            Toast.makeText(context, "yoo", Toast.LENGTH_SHORT).show()
            Log.d("ppp", "yoo")
            val intent = Intent(context, ShowUserActivity::class.java)
            intent.putExtra("id", profileId)
            intent.putExtra("title", "followers")
            startActivity(intent)
        }

        val totalFollowing : TextView = view.findViewById(R.id.total_following)
        totalFollowing.setOnClickListener {
            Toast.makeText(context, "boo", Toast.LENGTH_SHORT).show()
            Log.d("ppp", "boo")
            val intent = Intent(context, ShowUserActivity::class.java)
            intent.putExtra("id", profileId)
            intent.putExtra("title", "following")
            startActivity(intent)
        }

        view.edit_account_btn.setOnClickListener {
            when (view.edit_account_btn.text.toString()) {
                "Edit Profile" -> startActivity(Intent(context, AccountSettingsActivity::class.java))
                "Follow" -> {
                    firebaseUser.uid.let {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it)
                            .child("Following").child(profileId)
                            .setValue(true)
                    }
                    firebaseUser.uid.let {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it)
                            .removeValue()
                    }
                    addNotification()
                }
                "Following" -> {
                    firebaseUser.uid.let {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it)
                            .child("Following").child(profileId)
                            .removeValue()
                    }
                    firebaseUser.uid.let {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it)
                            .setValue(true)
                    }
                }
            }
        }

        getFollowers()
        getFollowings()
        userInfo()
        myPhotos()
        getTotalNumberOfPosts()
        mySaves()

        return view
    }

    private fun checkFollowAndFollowingButtonStatus() {
        val following = firebaseUser.uid.let {
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it)
                .child("Following")
            }
        following.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child(profileId).exists()) {
                    view?.edit_account_btn?.text = "Following"
                } else {
                    view?.edit_account_btn?.text = "Follow"
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getFollowers() {
        val followers = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Followers")
        followers.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    view?.total_followers?.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getFollowings() {
        val followers = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Following")
        followers.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    view?.total_following?.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun myPhotos() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()) {
                    (postList as ArrayList<Post>).clear()
                    for(snapshot in dataSnapshot.children) {
                        val post = snapshot.getValue(Post::class.java)
                        if(post!!.publisher.equals(profileId)) {
                            (postList as ArrayList<Post>).add(post)
                        }
                        Collections.reverse(postList)
                    }
                    myImagesAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun userInfo() {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("Users").child(profileId)
        userRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(view?.profile_image_fragment)
                    view?.profile_fragment_username?.text = user.username
                    view?.full_name?.text = user.fullname
                    view?.bio?.text = user.bio
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun mySaves() {
        mySavedImage = ArrayList()
        val savedRef = FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser.uid)
        savedRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        (mySavedImage as ArrayList<String>).add(snapshot.key!!)
                    }
                    readSavedImagesData()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun readSavedImagesData() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()) {
                    (postListSaved as ArrayList<Post>).clear()
                    for (snapshot in dataSnapshot.children) {
                        val post = snapshot.getValue(Post::class.java)
                        for (key in mySavedImage) {
                            if(post!!.postid == key) {
                                (postListSaved as ArrayList<Post>).add(post)
                            }
                        }
                    }
                    myImagesAdapterSavedImage.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    
    private fun getTotalNumberOfPosts() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()) {
                    var counter = 0
                    for (snapshot in dataSnapshot.children) {
                        val post = snapshot.getValue(Post::class.java)!!
                        if(post.publisher == profileId) {
                            counter++
                        }
                    }
                    total_posts.text = " " + counter
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onStop() {
        super.onStop()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    private fun addNotification() {
        val postRef = FirebaseDatabase.getInstance().reference
            .child("Notifications")
            .child(profileId)

        val map = HashMap<String, Any>()
        map["userid"] = firebaseUser.uid
        map["text"] = "started following you"
        map["postid"] = ""
        map["ispost"] = false
        postRef.push().setValue(map)
    }
}