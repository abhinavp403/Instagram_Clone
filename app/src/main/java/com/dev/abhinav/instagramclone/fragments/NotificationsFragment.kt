package com.dev.abhinav.instagramclone.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.abhinav.instagramclone.R
import com.dev.abhinav.instagramclone.adapter.NotificationAdapter
import com.dev.abhinav.instagramclone.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

class NotificationsFragment : Fragment() {

    private lateinit var notificationsList: List<Notification>
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.fragment_notifications, container, false)

        val recyclerView:RecyclerView = view.findViewById(R.id.recycler_view_notifications)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        notificationsList = ArrayList()
        notificationAdapter = context?.let { NotificationAdapter(it, notificationsList as ArrayList<Notification>)}!!
        recyclerView.adapter = notificationAdapter

       readNotifications()

        return view
    }

    private fun readNotifications() {
        val notificationRef = FirebaseDatabase.getInstance().reference
            .child("Notifications")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
        notificationRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()) {
                    (notificationsList as ArrayList<Notification>).clear()
                    for (snapshot in dataSnapshot.children) {
                        val notification = snapshot.getValue(Notification::class.java)
                        (notificationsList as ArrayList<Notification>).add(notification!!)
                    }
                    Collections.reverse(notificationsList)
                    notificationAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}