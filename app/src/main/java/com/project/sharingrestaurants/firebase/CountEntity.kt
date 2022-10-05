package com.project.sharingrestaurants.firebase

data class CountEntity(
    var documentId: String ="",
    var comments: Int =0,//댓글 수
    var like: Int =0,//추천 수
    var likeUsers: List<String> = listOf(),//추천 한 유저목록
)
