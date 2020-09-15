package com.dev.abhinav.instagramclone.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.abhinav.instagramclone.R
import com.dev.abhinav.instagramclone.adapter.UserAdapter
import com.dev.abhinav.instagramclone.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_search.view.*

class SearchFragment : Fragment() {

    private lateinit var userAdapter: UserAdapter
    private lateinit var user: MutableList<User>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.fragment_search, container, false)

        val recyclerView:RecyclerView = view.findViewById(R.id.recycler_view_search)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        user = ArrayList()
        userAdapter = context?.let { UserAdapter(it, user as ArrayList<User>, true) }!!
        recyclerView.adapter = userAdapter

        view.search_edit_text.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(view.search_edit_text.text.toString() == "") {

                }
                else {
                    recyclerView.visibility = View.VISIBLE
                    retrieveUsers()
                    searchUsers(s.toString().toLowerCase())
                }
            }
        })

        return view
    }

    private fun retrieveUsers() {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users")
        userRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(view!!.search_edit_text.text.toString() == "") {
                    user.clear()
                    for(snapshot in dataSnapshot.children) {
                        val u = snapshot.getValue(User::class.java)
                        if(u != null) {
                            user.add(u)
                        }
                    }
                    userAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun searchUsers(input: String) {
        val query = FirebaseDatabase.getInstance().reference.child("Users")
            .orderByChild("fullname")
            .startAt(input)
            .endAt(input + "\uf8ff")
        query.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                user.clear()
                for(snapshot in dataSnapshot.children) {
                    val u = snapshot.getValue(User::class.java)
                    if(u != null) {
                        user.add(u)
                    }
                }
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}