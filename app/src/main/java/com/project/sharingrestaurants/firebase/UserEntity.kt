package com.project.sharingrestaurants.firebase

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable
import java.util.*

data class UserEntity(
    var uid: String ="",
    var email: String ="",
    var nickname: String ="",
    var profileImage: String =""
): Serializable
