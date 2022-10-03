package com.project.sharingrestaurants.firebase

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable
import java.util.*

data class BoardEntity(
    var documentId: String ="",
    var uid: String ="",
    var email: String ="",//닉네임은 이걸로 auth컬렉션 조회(회원이 닉네임 바꿨다고 모든 글의 닉네임을 바꿀 수는 없다)
    var timestamp: Date = Date(),//기본키 역할//(댓글,답글)은 board의 하위 노드에
    var tilte: String ="",//제목
    var place: String ="",//세부주소(사용자 입력)
    var locate: String ="",//지도에서 선택한 주소
    var priority: Float =0f,//별점
    var body: List<String> = listOf(),//최소 사이즈 = 1
    var image: List<String> = listOf(),//최소 사이즈 = 1(이미지 없으면 index 0 값 = "")
    var thumb: String = "",
    var recommends: Int =0,//추천 수
    var latitude: Double =0.0,//위도
    var longitude: Double =0.0,//경도
    var look: Int = 0,//조회수
//위는 파이어스토어 필드
    var comments: Int =0,//댓글 수//세부글에서 쿼리
    var nickname: String ="",//세부글에서 쿼리
    var profileImage: String =""//세부글에서 쿠
): Serializable
