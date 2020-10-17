package com.dev.abhinav.instagramclone.adapter

import android.app.AlertDialog
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
import com.dev.abhinav.instagramclone.AddStoryActivity
import com.dev.abhinav.instagramclone.R
import com.dev.abhinav.instagramclone.StoryActivity
import com.dev.abhinav.instagramclone.model.Story
import com.dev.abhinav.instagramclone.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class StoryAdapter(private val context: Context, private val story: List<Story>) : RecyclerView.Adapter<StoryAdapter.ViewHolder>() {

    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        //StoryItem
        var storyImageSeen: CircleImageView? = itemView.findViewById(R.id.story_image_seen)
        var storyImage: CircleImageView? = itemView.findViewById(R.id.story_image)
        var storyUsername: TextView? = itemView.findViewById(R.id.story_username)

        //AddStoryItem
        var story_add_btn: ImageView? = itemView.findViewById(R.id.story_add)
        var addStoryText: TextView? = itemView.findViewById(R.id.add_story_text)
    }

    override fun getItemViewType(position: Int): Int {
        return if(position == 0) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if(viewType == 0) {
            val view = LayoutInflater.from(context).inflate(R.layout.add_story_item, parent, false)
            ViewHolder(view)
        }
        else {
            val view = LayoutInflater.from(context).inflate(R.layout.story_item, parent, false)
            ViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return story.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = story[position]
        getUserInfo(holder, story.userid, position)
        if(holder.adapterPosition !== 0) {
            seenStory(holder, story.userid)
        }
        if(holder.adapterPosition === 0) {
            myStories(holder.addStoryText!!, holder.story_add_btn!!, false)
        }
        holder.itemView.setOnClickListener {
            if(holder.adapterPosition === 0) {
                myStories(holder.addStoryText!!, holder.story_add_btn!!, true)
            }
            else {
                val intent = Intent(context, StoryActivity::class.java)
                intent.putExtra("userId", story.userid)
                context.startActivity(intent)
            }
        }
    }

    private fun getUserInfo(viewHolder: ViewHolder, userId: String, position: Int) {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(userId)
        userRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(viewHolder.storyImage)
                    if(position != 0) {
                        Picasso.get().load(user.image).placeholder(R.drawable.profile).into(viewHolder.storyImageSeen)
                        viewHolder.storyUsername!!.text = user.username
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun myStories(textView: TextView, imageView: ImageView, click: Boolean) {
        val storyRef = FirebaseDatabase.getInstance().reference
            .child("Story")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
        storyRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var counter = 0
                val timeCurrent = System.currentTimeMillis()
                for(snapshot in dataSnapshot.children) {
                    val story = snapshot.getValue(Story::class.java)!!
                    if(timeCurrent > 1602727543040 && timeCurrent < 1602813942839) {
                        counter++
                    }
                }
                if(click) {
                    if (counter > 0) {
                        val alertDialog = AlertDialog.Builder(context).create()

                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "View Story") {
                            dialogInterface, _ ->
                            val intent = Intent(context, StoryActivity::class.java)
                            intent.putExtra("userId", FirebaseAuth.getInstance().currentUser!!.uid)
                            context.startActivity(intent)
                            dialogInterface.dismiss()
                        }

                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story") {
                            dialogInterface, _ ->
                            val intent = Intent(context, AddStoryActivity::class.java)
                            intent.putExtra("userId", FirebaseAuth.getInstance().currentUser!!.uid)
                            context.startActivity(intent)
                            dialogInterface.dismiss()
                        }
                        alertDialog.show()
                    }
                    else {
                        val intent = Intent(context, AddStoryActivity::class.java)
                        intent.putExtra("userId", FirebaseAuth.getInstance().currentUser!!.uid)
                        context.startActivity(intent)
                    }
                }
                else {
                    if(counter > 0) {
                        textView.text = "My Story"
                        imageView.visibility = View.GONE
                    }
                    else {
                        textView.text = "Add Story"
                        imageView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun seenStory(viewHolder: ViewHolder, userId: String) {
        val storyRef = FirebaseDatabase.getInstance().reference
            .child("Story")
            .child(userId)
        storyRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var i = 0
                for(snapshot in dataSnapshot.children) {
                    if(!snapshot.child("views")
                            .child(FirebaseAuth.getInstance().currentUser!!.uid).exists() && System.currentTimeMillis() < snapshot.getValue(Story::class.java)!!.timeEnd) {
                        i++
                    }
                }
                if(i > 0) {
                    viewHolder.storyImage!!.visibility = View.VISIBLE
                    viewHolder.storyImageSeen!!.visibility = View.GONE
                }
                else {
                    viewHolder.storyImage!!.visibility = View.GONE
                    viewHolder.storyImageSeen!!.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}