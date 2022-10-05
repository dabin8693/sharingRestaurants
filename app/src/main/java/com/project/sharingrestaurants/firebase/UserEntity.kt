package com.project.sharingrestaurants.firebase

import java.io.Serializable

data class UserEntity(//auth컬렉션
    var uid: String ="",
    var email: String ="",
    var nickname: String ="",
    var profileImage: String =""
): Serializable
