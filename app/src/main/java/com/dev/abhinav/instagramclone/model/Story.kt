package com.dev.abhinav.instagramclone.model

class Story (val imageUrl: String, val timeStart: Long, val timeEnd: Long, val storyid: String, val userid: String) {
    constructor() : this("", 0, 0, "", "")
}