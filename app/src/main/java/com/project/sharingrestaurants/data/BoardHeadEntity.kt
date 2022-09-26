package com.project.sharingrestaurants.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable
import java.util.*

data class BoardHeadEntity(
    var tilte: String ="",//제목
    var place: String ="",//세부주소(사용자 입력)
    var locate: String ="",//지도에서 선택한 주소
    var priority: Float =0f,//별점
): Serializable
