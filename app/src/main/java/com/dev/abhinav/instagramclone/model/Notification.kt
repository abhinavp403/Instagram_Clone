package com.dev.abhinav.instagramclone.model

class Notification (val userid: String, val text: String, val postid: String, val ispost:Boolean = false) {
    constructor() : this("", "", "", false)
}