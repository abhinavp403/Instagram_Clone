package com.dev.abhinav.instagramclone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.dev.abhinav.instagramclone.R
import com.dev.abhinav.instagramclone.model.Comment
import com.dev.abhinav.instagramclone.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentsAdapter(private val context: Context, private val comment: MutableList<Comment>) : RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView = itemView.findViewById(R.id.user_profile_image_comment)
        var userName: TextView = itemView.findViewById(R.id.user_name_comment)
        var comments: TextView = itemView.findViewById(R.id.comment_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.comments_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return comment.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser =  FirebaseAuth.getInstance().currentUser
        val comment = comment[position]
        holder.comments.text = comment.comment
        getUserInfo(holder.profileImage, holder.userName, comment.publisher)
    }

    private fun getUserInfo(profileImage: CircleImageView, userName: TextView, publisher: String) {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(publisher)
        userRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(profileImage)
                    userName.text = user.username
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}