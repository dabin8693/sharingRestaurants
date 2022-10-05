package com.project.sharingrestaurants.firebase


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.util.*


class FBDatabase {

    private val fbDatabase = Firebase.firestore

    companion object {
        private var INSTANCE: FBDatabase? = null

        fun getInstance(): FBDatabase {//synchronized필요없음
            if (INSTANCE == null) {//중복 생성 방지
                INSTANCE = FBDatabase()
            }
            return INSTANCE ?: FBDatabase()//null이면  재생성
        }
    }
////////////////////////////////////////////////////////////////////////////////////////////
    //add
    suspend fun addBoard(boardMap: MutableMap<String, Any>): Boolean {
        try {
            val documentRef: DocumentReference = fbDatabase.collection("board").document()
            boardMap.replace("documentId", documentRef.id)//api24이상
            documentRef.set(boardMap).await()
            val timestamp: Date = fbDatabase.collection("board").document(documentRef.id).get().await().data!!.get("timestamp") as Date
            return addCount(documentRef.id, timestamp)
        }catch (e: Exception){
            return false
        }
    }

    private suspend fun addCount(boardId: String, timestamp: Date): Boolean{
        try {
            fbDatabase.collection("count").document(boardId)
                .set(CountEntity(boardId, timestamp, 0, 0, 0, listOf())).await()
            return true
        }catch (e: Exception){
            return false
        }
    }

    private suspend fun isAuth(user: UserEntity): Boolean {//현재로그인유저 회원정보있는지 확인
        try {
            val data: QuerySnapshot =
                fbDatabase.collection("auth").whereEqualTo("uid", user.uid).get().await()
            if (data.isEmpty) {
                user.nickname = user.email.split("@").get(0)//초기값 설정
                return false
            } else {
                return true
            }
        }catch (e: Exception){
            user.nickname = user.email.split("@").get(0)//초기값 설정
            return false
        }
    }

    suspend fun addAuth(user: UserEntity) {//추가 //처음 추가할때는 닉네임
        val bool: Boolean = isAuth(user)
        if (!bool) {
            fbDatabase.collection("auth").document(user.uid).set(
                hashMapOf(
                    "uid" to user.uid,
                    "email" to user.email,
                    "image" to user.profileImage,
                    "nickname" to user.nickname,//초기값은 이메일 앞부분이 들어가 있다
                    "timestamp" to FieldValue.serverTimestamp()
                )
            )
        }
    }

    suspend fun addComment(commentMap: MutableMap<String, Any>) {//해당글에 댓글
        val documentRef: DocumentReference =
            fbDatabase.collection("board").document(commentMap.get("boardId").toString()).collection("comment").document()
        commentMap.replace("documentId", documentRef.id)
        documentRef.set(commentMap).await()
    }

    suspend fun addReply(replyMap: MutableMap<String, Any>) {//해당글에 댓글
        val documentRef: DocumentReference =
            fbDatabase.collection("board").document(replyMap.get("boardId").toString()).collection("reply").document()
        replyMap.replace("documentId", documentRef.id)
        documentRef.set(replyMap).await()
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //insert
    suspend fun insertBoard(boardMap: MutableMap<String, Any>): Boolean {
        try {
            fbDatabase.collection("board").document(boardMap.get("documentId").toString())
                .update(boardMap).await()
            return true
        }catch (e: Exception){
            return false
        }
    }

    suspend fun insertComment(commentMap: MutableMap<String, Any>): Boolean {
        try {
            fbDatabase.collection("board").document(commentMap.get("boardId").toString()).collection("comment").document(commentMap.get("documentId").toString())
                .update(commentMap).await()
            return true
        }catch (e: Exception){
            return false
        }
    }

    suspend fun insertReply(replyMap: MutableMap<String, Any>): Boolean {
        try {
            fbDatabase.collection("board").document(replyMap.get("boardId").toString()).collection("reply").document(replyMap.get("documentId").toString())
                .update(replyMap).await()
            return true
        }catch (e: Exception){
            return false
        }
    }

    suspend fun incrementLook(boardId: String){//boardId = documentId
        fbDatabase.collection("count").document(boardId)
            .update("look",FieldValue.increment(1)).await()//조회 수 증가
    }

    suspend fun incrementLike(boardId: String) {//like = 추천
        fbDatabase.collection("count").document(boardId)
            .update("like",FieldValue.increment(1)).await()//추천 수 증가
    }

    suspend fun updateLikeUsers(boardId: String, users: List<String>){
        fbDatabase.collection("count").document(boardId)
            .update("likeUsers", users).await()//추천 유저 목록 업데이트
    }

    suspend fun incrementComments(boardId: String){
        fbDatabase.collection("count").document(boardId)
            .update("comments",FieldValue.increment(1)).await()//댓글 수 증가
    }

    suspend fun decrementComments(boardId: String){//댓글 삭제되면 "삭제된 댓글입니다."메시지 띄우기
        val documentRef: DocumentReference = fbDatabase.collection("count").document(boardId)
        //감소 기능은 따로 없어서 트랜젝션 걸고 1,값 조회 2.값 내리기
        fbDatabase.runTransaction {
            val snapshot: DocumentSnapshot = it.get(documentRef)
            val comments = (snapshot.data!!.get("comments") as Int) - 1
            it.update(documentRef, "comments", comments)
        }.await()//댓글 수 감소
    }

    suspend fun insertNicknameAuth(uid: String, nickname: String): Boolean {
        try {
            fbDatabase.collection("auth").document(uid).update("nickname", nickname).await()
            return true
        }catch (e: Exception){
            return false
        }
    }
///////////////////////////////////////////////////////////////////////////////////////////////////
    //get
    suspend fun getBoardList(): List<BoardEntity> {
        try {
            return fbDatabase.collection("board").orderBy("timestamp", Query.Direction.DESCENDING).get()
                .await().toObjects<BoardEntity>()
        }catch (e: Exception){
            return emptyList()
        }
    }

    suspend fun getCountList(): List<CountEntity>{
        try {
            return fbDatabase.collection("count").orderBy("timestamp", Query.Direction.DESCENDING).get().await().toObjects<CountEntity>()
        }catch (e: Exception){
            return emptyList()
        }
    }

    suspend fun getBoard(boardId: String): BoardEntity{
        try {
            return fbDatabase.collection("board").document(boardId).get().await().toObject<BoardEntity>()!!
        }catch (e: Exception){
            return BoardEntity()
        }
    }

    suspend fun getCount(boardId: String): CountEntity{
        try {
            return fbDatabase.collection("count").document(boardId).get().await().toObject<CountEntity>()!!
        }catch (e: Exception){
            return CountEntity()
        }
    }

    suspend fun getUser(email: String): BoardEntity{
        try {
            val snapshot = fbDatabase.collection("auth").whereEqualTo("email", email).get().await()
            val boardEntity = BoardEntity()
            for (data in snapshot){
                boardEntity.profileImage = data.data.get("image") as String
                boardEntity.nickname = data.data.get("nickname") as String
            }
            return boardEntity
        }catch (e: Exception){
            return BoardEntity()
        }
    }
    //댓글목록을 먼저 다 불러오고 그 다음 답글목록을 전부 불러온다 댓글list에 답글list를 삽입 삽입 순서는 답글 TimeStamp순서로 삽입 위치 결정 방법은 답글 필드의 댓글documentId기준
    //어뎁터에서 댓글 답글 분류는 data class타입 비교로
    //댓글, 답글도 코루틴으로
    suspend fun getComment(boardId: String): List<CommentEntity> {//해당글의 댓글 목록
        try {
            val data: QuerySnapshot =
                fbDatabase.collection("board").document(boardId).collection("comment")
                    .orderBy("timestamp", Query.Direction.DESCENDING).get().await()
            return data.toObjects<CommentEntity>() //비동기 처리후 옵저버 호출
        }catch (e: Exception){
            return emptyList()
        }
    }

    suspend fun getReply(boardId: String): List<ReplyEntity> {//해당글의 답글 목록
        try {
            val data: QuerySnapshot =
                fbDatabase.collection("board").document(boardId).collection("reply")
                    .orderBy("timestamp", Query.Direction.DESCENDING).get().await()
            return data.toObjects<ReplyEntity>()
        }catch (e: Exception){
            return emptyList()
        }
    }

    suspend fun getNicknameAuth(email: String): String {//회원 이메일로 닉네임 가져오기 //다른 유저들
        try {
            //중복 호출 방지하기 위해 viewmodel에서 회원정보들을 hashMap(key:email, value:nickname)으로 저장하고 없으면 호출하는식으로
            //fragment, detailactivy가 hashmap을 공유하고 fragment가 체인지 되기전까지 유지 그 이후는 다시 최신화
            val snapshot = fbDatabase.collection("auth").whereEqualTo("email", email).get().await()
            var nickname: String = ""
            for (data in snapshot) {
                nickname = data.data.get("nickname") as String//닉네임 가져오기
            }
            return nickname
        } catch (e: Exception) {
            return ""
        }
    }


}