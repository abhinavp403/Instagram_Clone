package com.dev.abhinav.instagramclone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.dev.abhinav.instagramclone.R
import com.dev.abhinav.instagramclone.fragments.PostDetailsFragment
import com.dev.abhinav.instagramclone.fragments.ProfileFragment
import com.dev.abhinav.instagramclone.model.Notification
import com.dev.abhinav.instagramclone.model.Post
import com.dev.abhinav.instagramclone.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class NotificationAdapter(private val context: Context, private val notification: List<Notification>) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var postImage: ImageView = itemView.findViewById(R.id.notification_post_image)
        var profileImage: CircleImageView = itemView.findViewById(R.id.notification_profile_image)
        var username: TextView = itemView.findViewById(R.id.username_notification)
        var text: TextView = itemView.findViewById(R.id.comment_notification)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.notifications_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return notification.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notification[position]

        if(notification.text.equals("started following you")) {
            holder.text.text = "started following you"
        }
        else if(notification.text.equals("liked your post")) {
            holder.text.text = "liked your post"
        }
        else if(notification.text.contains("commented:")) {
            holder.text.text = notification.text.replace("commented:", "commented: ")
        }
        else {
            holder.text.text = notification.text
        }


        userInfo(holder.profileImage, holder.username, notification.userid)

        if(notification.ispost) {
            holder.postImage.visibility = View.VISIBLE
            getPostImage(holder.postImage, notification.postid)
        }
        else {
            holder.postImage.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if(notification.ispost) {
                val editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                editor.putString("postId", notification.postid)
                editor.apply()
                (context as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, PostDetailsFragment())
                    .commit()
            }
            else {
                val editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                editor.putString("profileId", notification.userid)
                editor.apply()
                (context as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment())
                    .commit()
            }
        }
    }

    private fun userInfo(imageView: ImageView, username: TextView, publisherId: String) {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("Users").child(publisherId)
        userRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(imageView)
                    username.text = user.username
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getPostImage(imageView: ImageView, postId: String) {
        val postRef = FirebaseDatabase.getInstance().reference
            .child("Posts")
            .child(postId)
        postRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val post = snapshot.getValue<Post>(Post::class.java)
                    Picasso.get().load(post!!.postimage).placeholder(R.drawable.profile).into(imageView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}