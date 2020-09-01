package com.dev.abhinav.instagramclone.model

class User (val username: String, val fullname: String, val bio: String, val image: String, val uid: String) {
    constructor() : this("", "", "", "", "")
}