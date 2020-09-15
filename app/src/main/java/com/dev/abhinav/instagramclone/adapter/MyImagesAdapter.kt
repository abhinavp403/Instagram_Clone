package com.dev.abhinav.instagramclone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.dev.abhinav.instagramclone.R
import com.dev.abhinav.instagramclone.fragments.PostDetailsFragment
import com.dev.abhinav.instagramclone.model.Post
import com.squareup.picasso.Picasso

class MyImagesAdapter(private val context: Context, post: List<Post>) : RecyclerView.Adapter<MyImagesAdapter.ViewHolder>() {

    private var post: List<Post>? = null
    init {
        this.post = post
    }

    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var postImage: ImageView = itemView.findViewById(R.id.post_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.images_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return post!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = post!![position]
        Picasso.get().load(post.postimage).placeholder(R.drawable.profile).into(holder.postImage)

        holder.postImage.setOnClickListener {
            val editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            editor.putString("postId", post.postid)
            editor.apply()
            (context as FragmentActivity).supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, PostDetailsFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}