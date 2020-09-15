package com.dev.abhinav.instagramclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.abhinav.instagramclone.adapter.UserAdapter
import com.dev.abhinav.instagramclone.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShowUserActivity : AppCompatActivity() {

    private lateinit var id: String
    private lateinit var title: String
    private lateinit var userAdapter: UserAdapter
    private lateinit var userList: List<User>
    private lateinit var idList: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_user)

        val intent = intent
        id = intent.getStringExtra("id")!!
        title = intent.getStringExtra("title")!!

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userList = ArrayList()
        userAdapter = UserAdapter(this, userList as ArrayList<User>, false)
        recyclerView.adapter = userAdapter

        idList = ArrayList()
        when(title) {
            "likes" -> getLikes()
            "following" -> getFollowing()
            "followers" -> getFollowers()
            "views" -> getViews()
        }
    }

    private fun getLikes() {
        val likesRef = FirebaseDatabase.getInstance().reference
            .child("Likes")
            .child(id)
        likesRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()) {
                    (idList as ArrayList<String>).clear()
                    for(snapshot in dataSnapshot.children) {
                        (idList as ArrayList<String>).add(snapshot.key!!)
                    }
                    showUsers()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun showUsers() {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users")
        userRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (userList as ArrayList<User>).clear()
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)
                    for (id in idList) {
                        if(user!!.uid == id) {
                            (userList as ArrayList<User>).add(user)
                        }
                    }
                }
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getFollowing() {
        val followers = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id)
            .child("Following")
        followers.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (idList as ArrayList<String>).clear()
                for(snapshot in dataSnapshot.children) {
                    (idList as ArrayList<String>).add(snapshot.key!!)
                }
                showUsers()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getFollowers() {
        val followers = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id)
            .child("Following")
        followers.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (idList as ArrayList<String>).clear()
                for(snapshot in dataSnapshot.children) {
                    (idList as ArrayList<String>).add(snapshot.key!!)
                }
                showUsers()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getViews() {

    }
}