package com.project.sharingrestaurants.firebase

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class
CommentEntity(//댓글, 답글 //board하위 컬렉션
    @DocumentId
    var documentId: String,
    var userID: String,//댓글, 답글 작성자 아이디(이메일) 보여줄때는 auth컬렉션에서 닉네임 찾아서 닉네임을 보여준다
    var boardId: String,//본문글의 documentId
    var isReply: Boolean,//true-답글, false-댓글
    var replyId: String,//답글 달 상대의(documentId)
    @ServerTimestamp
    var timestamp: Date,
    var body: String,
)
