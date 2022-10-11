package com.project.sharingrestaurants.firebase

import java.io.Serializable
import java.util.*

data class BoardEntity(
    var documentId: String ="",
    var uid: String ="",
    var email: String ="",
    var timestamp: Date = Date(),//기본키 역할//(댓글,답글)은 board의 하위 노드에
    var tilte: String ="",//제목
    var place: String ="",//세부주소(사용자 입력)
    var locate: String ="",//지도에서 선택한 주소
    var priority: Float =0f,//별점
    var body: List<String> = listOf(),//최소 사이즈 = 1
    var image: List<String> = listOf(),//최소 사이즈 = 1(이미지 없으면 index 0 값 = "")//firestore경로는 다 처음에 '/'가 들어가 있어서 substring(1)하고 사용해야 된다.
    var thumb: String = "",//firestore경로는 다 처음에 '/'가 들어가 있어서 substring(1)하고 사용해야 된다.
    var latitude: Double =0.0,//위도
    var longitude: Double =0.0,//경도
//위는 파이어스토어 board컬렉션 필드
    var comments: Int =0,//댓글 수//count컬렉션
    var look: Int = 0,//조회수
    var like: Int =0,//추천 수//count컬렉션
    var nickname: String ="",//auth컬렉션
    var profileImage: String =""//auth컬렉션
): Serializable
