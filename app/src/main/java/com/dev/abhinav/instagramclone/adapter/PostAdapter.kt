package com.dev.abhinav.instagramclone.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.dev.abhinav.instagramclone.CommentsActivity
import com.dev.abhinav.instagramclone.MainActivity
import com.dev.abhinav.instagramclone.R
import com.dev.abhinav.instagramclone.ShowUserActivity
import com.dev.abhinav.instagramclone.model.Post
import com.dev.abhinav.instagramclone.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_comments.*

class PostAdapter(private val context: Context, private val post: List<Post>) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView = itemView.findViewById(R.id.user_profile_image_post)
        var postImage: ImageView = itemView.findViewById(R.id.post_image_home)
        var likeButton: ImageView = itemView.findViewById(R.id.post_image_like_btn)
        var commentButton: ImageView = itemView.findViewById(R.id.post_image_comment_btn)
        var saveButton: ImageView = itemView.findViewById(R.id.post_save_comment_btn)
        var userName: TextView = itemView.findViewById(R.id.user_name_post)
        var likes: TextView = itemView.findViewById(R.id.likes)
        var publisher: TextView = itemView.findViewById(R.id.publisher)
        var description: TextView = itemView.findViewById(R.id.description)
        var comments: TextView = itemView.findViewById(R.id.comments)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.post_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return post.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = post[position]
        Picasso.get().load(post.postimage).into(holder.postImage)

        if(post.description.equals("")) {
            holder.description.visibility = View.GONE
        }
        else {
            holder.description.visibility = View.VISIBLE
            holder.description.setText(post.description)
        }

        publisherInfo(holder.profileImage, holder.userName, holder.publisher, post.publisher)
        isLikes(post.postid, holder.likeButton)
        numberofLikes(post.postid, holder.likes)
        numberofComments(post.postid, holder.comments)
        checkSavedStatus(post.postid, holder.saveButton)

        holder.likeButton.setOnClickListener {
            if(holder.likeButton.tag == "Like") {
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.postid)
                    .child(firebaseUser.uid)
                    .setValue(true)

                addNotification(post.publisher, post.postid)
            }
            else {
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.postid)
                    .child(firebaseUser.uid)
                    .removeValue()
                //TODO: Fix refresh issue on unliking post
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            }
        }

        holder.likes.setOnClickListener {
            val intent = Intent(context, ShowUserActivity::class.java)
            intent.putExtra("id", post.postid)
            intent.putExtra("title", "likes")
            context.startActivity(intent)
        }

        holder.commentButton.setOnClickListener {
            val intent = Intent(context, CommentsActivity::class.java)
            intent.putExtra("postId", post.postid)
            intent.putExtra("publisherId", post.publisher)
            context.startActivity(intent)
        }

        holder.comments.setOnClickListener {
            val intent = Intent(context, CommentsActivity::class.java)
            intent.putExtra("postId", post.postid)
            intent.putExtra("publisherId", post.publisher)
            context.startActivity(intent)
        }

        holder.saveButton.setOnClickListener {
            if(holder.saveButton.tag == "Save") {
                FirebaseDatabase.getInstance().reference
                    .child("Saves")
                    .child(firebaseUser.uid)
                    .child(post.postid)
                    .setValue(true)
            }
            else {
                FirebaseDatabase.getInstance().reference
                    .child("Saves")
                    .child(firebaseUser.uid)
                    .child(post.postid)
                    .removeValue()
            }
        }
    }

    private fun isLikes(postid: String, likeButton: ImageView) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val likesRef = FirebaseDatabase.getInstance().reference
            .child("Likes")
            .child(postid)
        likesRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child(firebaseUser!!.uid).exists()) {
                    likeButton.setImageResource(R.drawable.heart_clicked)
                    likeButton.tag = "Liked"
                }
                else {
                    likeButton.setImageResource(R.drawable.heart_not_clicked)
                    likeButton.tag = "Like"
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun numberofLikes(postid: String, likes: TextView) {
        val like = FirebaseDatabase.getInstance().reference
            .child("Likes")
            .child(postid)
        like.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    likes.text = snapshot.childrenCount.toString() + " likes"
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun numberofComments(postid: String, comments: TextView) {
        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments")
            .child(postid)
        commentsRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    comments.text = "View all " + snapshot.childrenCount.toString() + " comments"
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun publisherInfo(profileImage: CircleImageView, userName: TextView, publisher: TextView, publisherID: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherID)
        userRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(profileImage)
                    userName.text = user.username
                    publisher.text = user.fullname
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun checkSavedStatus(postid: String, imageView: ImageView) {
        val savesRef = FirebaseDatabase.getInstance().reference
            .child("Saves")
            .child(firebaseUser.uid)
        savesRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child(postid).exists()) {
                    imageView.setImageResource(R.drawable.save_large_icon)
                    imageView.tag = "Saved"
                }
                else {
                    imageView.setImageResource(R.drawable.save_unfilled_large_icon)
                    imageView.tag = "Save"
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun addNotification(userId: String, postId: String) {
        val postRef = FirebaseDatabase.getInstance().reference
            .child("Notifications")
            .child(userId)

        val map = HashMap<String, Any>()
        map["userid"] = firebaseUser.uid
        map["text"] = "liked your post"
        map["postid"] = postId
        map["ispost"] = true
        postRef.push().setValue(map)
    }
}