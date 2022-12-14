package com.project.sharingrestaurants.firebase

import java.io.Serializable
import java.util.*

data class CommentEntity(//댓글 board하위 컬렉션
    var documentId: String ="",
    var uid: String ="",
    var email: String ="",//댓글 작성자 아이디(이메일) 보여줄때는 auth컬렉션에서 닉네임 찾아서 닉네임을 보여준다
    var boardId: String ="",//본문글의 documentId
    var timestamp: Date = Date(),
    var body: String ="",
    var isDelete: Boolean =false,
    //위는 파이어스토어 comment컬렉션 필드
    var nickname: String ="",//auth컬렉션
    var profileImage: String =""//auth컬렉션
): Serializable
