package com.project.sharingrestaurants.firebase

import java.io.Serializable
import java.util.*

data class ReplyEntity(//답글 board하위 컬렉션
    var documentId: String,
    var uid: String ="",
    var email: String,//답글 작성자 아이디(이메일) 보여줄때는 auth컬렉션에서 닉네임 찾아서 닉네임을 보여준다
    var boardId: String,//본문글의 documentId
    var commentDocumentId: String,//댓글 documentId
    var commentID: String,//댓글 작성자 아이디(이메일)(상대)
    var timestamp: Date,
    var body: String,
    //위는 파이어스토어 필드
    var commentNickname: String ="",//댓글 작성자 닉네임(상대)
    var nickname: String ="",
    var profileImage: String =""
): Serializable