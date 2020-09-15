package com.dev.abhinav.instagramclone.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.dev.abhinav.instagramclone.MainActivity
import com.dev.abhinav.instagramclone.R
import com.dev.abhinav.instagramclone.fragments.ProfileFragment
import com.dev.abhinav.instagramclone.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter (private val context: Context, private val user: List<User>, private val isFragment: Boolean = false) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userNameTextView: TextView = itemView.findViewById(R.id.user_name_search)
        var userFullnameTextView: TextView = itemView.findViewById(R.id.user_fullname_search)
        var userProfileImage: CircleImageView = itemView.findViewById(R.id.user_profile_image_search)
        var followButton: Button = itemView.findViewById(R.id.follow_btn_search)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return user.size
    }

    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
        val user = user[position]
        holder.userNameTextView.text = user.username
        holder.userFullnameTextView.text= user.fullname
        Picasso.get().load(user.image).placeholder(R.drawable.profile).into(holder.userProfileImage)

        checkFollowingStatus(user.uid, holder.followButton)

        holder.itemView.setOnClickListener(View.OnClickListener {
            if(isFragment) {
                val pref = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                pref.putString("profileId", user.uid)
                pref.apply()

                (context as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment())
                    .commit()
            }
            else {
                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("publisherId", user.uid)
                context.startActivity(intent)
            }
        })

        holder.followButton.setOnClickListener {
            if(holder.followButton.text.toString() == "Follow") {
                firebaseUser.uid.let {
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it)
                        .child("Following").child(user.uid)
                        .setValue(true)
                        .addOnCompleteListener {task->
                            if(task.isSuccessful) {
                                firebaseUser.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.uid)
                                        .child("Followers").child(it1)
                                        .setValue(true)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }
                                        }
                                }
                            }
                        }
                }
                addNotification(user.uid)
            }
            else {
                firebaseUser.uid.let {
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it)
                        .child("Following").child(user.uid)
                        .removeValue()
                        .addOnCompleteListener {task->
                            if(task.isSuccessful) {
                                firebaseUser.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.uid)
                                        .child("Followers").child(it1)
                                        .removeValue()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }
                                        }
                                }
                            }
                        }
                }
            }
        }

    }

    private fun checkFollowingStatus(uid: String, followButton: Button) {
        val following = firebaseUser.uid.let {
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it)
                .child("Following")
            }

        following.addValueEventListener(object: ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child(uid).exists()) {
                    followButton.text = "Following"
                }
                else {
                    followButton.text = "Follow"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun addNotification(userId: String) {
        val postRef = FirebaseDatabase.getInstance().reference
            .child("Notifications")
            .child(userId)

        val map = HashMap<String, Any>()
        map["userid"] = firebaseUser.uid
        map["text"] = "started following you"
        map["postid"] = ""
        map["ispost"] = false
        postRef.push().setValue(map)
    }
}