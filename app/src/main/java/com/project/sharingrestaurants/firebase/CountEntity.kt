package com.project.sharingrestaurants.firebase

import java.util.*

data class CountEntity(
    var boardId: String ="",//board documentId로
    var timestamp: Date =Date(),//board time으로
    var comments: Int =0,//댓글 수
    var look: Int = 0,//조회 수
    var like: Int =0,//추천 수
)
