package com.dev.abhinav.instagramclone.model

class Post(val postid: String, val postimage: String, val publisher: String, val description: String) {
    constructor() : this("", "", "", "")
}