package com.dev.abhinav.instagramclone.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dev.abhinav.instagramclone.AccountSettingsActivity
import com.dev.abhinav.instagramclone.R
import kotlinx.android.synthetic.main.fragment_profile.view.*

class ProfileFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view  = inflater.inflate(R.layout.fragment_profile, container, false)
        view.edit_account_btn.setOnClickListener {
            startActivity(Intent(context, AccountSettingsActivity::class.java))
        }
        return view
    }
}