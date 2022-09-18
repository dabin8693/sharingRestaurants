package com.project.sharingrestaurants.firebase

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class AuthEntity(
    var userID: String,
    var nickname: String,
    @ServerTimestamp
    var timestamp: Date
    //var profileImage: String,//파이어스토리지uri
)
