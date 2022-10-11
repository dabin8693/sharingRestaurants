package com.project.sharingrestaurants.firebase


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.time.LocalDateTime
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
    //변화감지
    fun isChangedBoard(): LiveData<Int> {
        val liveData: MutableLiveData<Int> = MutableLiveData()
        fbDatabase.collection("board").addSnapshotListener { value, error ->
            if (value != null){
                for (change in value!!.documentChanges) {
                    when (change.type) {
                        DocumentChange.Type.ADDED -> {
                            liveData.value = 1
                        }
                        DocumentChange.Type.MODIFIED -> {
                            liveData.value = 2
                        }
                        DocumentChange.Type.REMOVED -> {
                            liveData.value = 3
                        }
                    }
                }
            }
            Log.d("에러isChangedBoard",error.toString())
        }
        return liveData
    }

    fun isChangedCount(): LiveData<Int> {
        val liveData: MutableLiveData<Int> = MutableLiveData()
        fbDatabase.collection("count").addSnapshotListener { value, error ->
            if (value != null){
                for (change in value!!.documentChanges) {
                    when (change.type) {
                        DocumentChange.Type.ADDED -> {
                            liveData.value = 1
                        }
                        DocumentChange.Type.MODIFIED -> {
                            liveData.value = 2
                        }
                        DocumentChange.Type.REMOVED -> {
                            liveData.value = 3
                        }
                    }
                }
            }
            Log.d("에러isChangedCount",error.toString())
        }
        return liveData
    }

    //add
    suspend fun addBoard(boardMap: MutableMap<String, Any>): String {
        try {
            val documentRef: DocumentReference = fbDatabase.collection("board").document()
            boardMap.replace("documentId", documentRef.id)//api24이상
            documentRef.set(boardMap).await()
            val time = fbDatabase.collection("board").document(documentRef.id).get()
                .await().data!!.get("timestamp") as Timestamp
            addCount(documentRef.id, time.toDate())
            return documentRef.id
        } catch (e: Exception) {//Timestamp = Timestamp(seconds=1664997712, nanoseconds=650000000)
            Log.d("에러addBoard",e.toString())
            return ""
        }
    }

    suspend fun addCount(boardId: String, timestamp: Date): Boolean {
        //Timestamp.toDate = Thu Oct 06 04:21:52 GMT+09:00 2022
        try {
            fbDatabase.collection("count").document(boardId)
                .set(CountEntity(boardId, timestamp, 0, 0, 0)).await()
            return true
        } catch (e: Exception) {
            Log.d("에러addCount",e.toString())
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
        } catch (e: Exception) {
            user.nickname = user.email.split("@").get(0)//초기값 설정
            Log.d("에러isAuth",e.toString())
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
                    "timestamp" to FieldValue.serverTimestamp(),
                    "likeList" to emptyList<String>(),//좋아요한 글 목록
                    "commentList" to emptyList<String>(),//댓글, 답글 작성한 글 목록
                    "boardList" to emptyList<String>()//작성한 글 목록
                )
            )
        }
    }

    suspend fun addComment(commentMap: MutableMap<String, Any>): String {//해당글에 댓글
        val documentRef: DocumentReference =
            fbDatabase.collection("board").document(commentMap.get("boardId").toString())
                .collection("comment").document()
        commentMap.replace("documentId", documentRef.id)
        documentRef.set(commentMap).await()
        return documentRef.id
    }

    suspend fun addReply(replyMap: MutableMap<String, Any>) {//해당글에 댓글
        val documentRef: DocumentReference =
            fbDatabase.collection("board").document(replyMap.get("boardId").toString())
                .collection("reply").document()
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
        } catch (e: Exception) {
            Log.d("에러insertBoard",e.toString())
            return false
        }
    }

    suspend fun insertComment(commentMap: MutableMap<String, Any>): Boolean {
        try {
            fbDatabase.collection("board").document(commentMap.get("boardId").toString())
                .collection("comment").document(commentMap.get("documentId").toString())
                .update(commentMap).await()
            return true
        } catch (e: Exception) {
            Log.d("에러insertComment",e.toString())
            return false
        }
    }

    suspend fun insertReply(replyMap: MutableMap<String, Any>): Boolean {
        try {
            fbDatabase.collection("board").document(replyMap.get("boardId").toString())
                .collection("reply").document(replyMap.get("documentId").toString())
                .update(replyMap).await()
            return true
        } catch (e: Exception) {
            Log.d("에러insertReply",e.toString())
            return false
        }
    }

    suspend fun incrementLook(boardId: String) {//boardId = documentId
        fbDatabase.collection("count").document(boardId)
            .update("look", FieldValue.increment(1)).await()//조회 수 증가
    }

    suspend fun incrementLike(boardId: String): Boolean {//like = 추천
        try {
            fbDatabase.collection("count").document(boardId)
                .update("like", FieldValue.increment(1)).await()//추천 수 증가
            return true
        } catch (e: Exception){
            Log.d("에러incrementLike",e.toString())
            return false
        }
    }
//댓글 수 증가, 감소 기능 추가

    suspend fun incrementComments(boardId: String) {
        fbDatabase.collection("count").document(boardId)
            .update("comments", FieldValue.increment(1)).await()//댓글 수 증가
    }

    suspend fun decrementLike(boardId: String): Boolean {//like = 추천
        val documentRef: DocumentReference = fbDatabase.collection("count").document(boardId)
            try {
                fbDatabase.runTransaction {
                    val snapshot: DocumentSnapshot = it.get(documentRef)
                    val likes = (snapshot.data!!.get("like") as Long) - 1
                    it.update(documentRef, "like", likes)
                }.await()
                return true
            }catch (e: Exception){
                Log.d("에러decrementLike",e.toString())
                return false
            }
    }

    suspend fun decrementComments(boardId: String) {//댓글 삭제되면 "삭제된 댓글입니다."메시지 띄우기
        val documentRef: DocumentReference = fbDatabase.collection("count").document(boardId)
        //감소 기능은 따로 없어서 트랜젝션 걸고 1,값 조회 2.값 내리기 //트랜젝션 사용시 주의사항! 1. 무조건 조회 -> 수정 순으로 2. 수정중에 누군가 조회시 트랜젝션 다시 함(최대횟수가 정해져 있다)
        try {
            fbDatabase.runTransaction {
                val snapshot: DocumentSnapshot = it.get(documentRef)
                val comments = (snapshot.data!!.get("comments") as Long) - 1
                it.update(documentRef, "comments", comments)
            }.await()//댓글 수 감소
        }catch (e: Exception){
            Log.d("에러decrementComments",e.toString())
        }
    }

    suspend fun insertNicknameAuth(uid: String, nickname: String): Boolean {
        try {
            fbDatabase.collection("auth").document(uid).update("nickname", nickname).await()
            return true
        } catch (e: Exception) {
            Log.d("에러insertNicknameAuth",e.toString())
            return false
        }
    }

    suspend fun insertWriteBoardListAuth(uid: String, list: List<String>): Boolean {//list안의 데이터 - board documentId
        try {
            fbDatabase.collection("auth").document(uid).update("boardList", list).await()
            return true
        } catch (e: Exception) {
            Log.d("에러insertWriteBoardListAuth",e.toString())
            return false
        }
    }

    suspend fun insertWriteCommentListAuth(uid: String, list: List<String>) {//list안의 데이터 - comment, reply documentId
        try {
            fbDatabase.collection("auth").document(uid).update("commentList", list).await()
        } catch (e: Exception) {
            Log.d("에러insertWriteCommentListAuth",e.toString())
        }
    }

    suspend fun insertLikeListAuth(uid: String, list: List<String>): Boolean {//list안의 데이터 - board documentId
        try {
            fbDatabase.collection("auth").document(uid).update("likeList", list).await()
            return true
        } catch (e: Exception) {
            Log.d("insertLikeListAuth",e.toString())
            return false
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //get
    suspend fun getBoardList(): List<BoardEntity> {
        try {
            return fbDatabase.collection("board").orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await().toObjects<BoardEntity>()
        } catch (e: Exception) {
            Log.d("에러getBoardList",e.toString())
            return emptyList()
        }
    }

    suspend fun getCountList(): List<CountEntity> {
        try {
            return fbDatabase.collection("count").orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await().toObjects<CountEntity>()
        } catch (e: Exception) {
            Log.d("에러getCountList",e.toString())
            return emptyList()
        }
    }

    suspend fun getBoard(boardId: String): BoardEntity {
        try {
            return fbDatabase.collection("board").document(boardId).get().await()
                .toObject<BoardEntity>()!!
        } catch (e: Exception) {
            Log.d("에러getBoard",e.toString())
            return BoardEntity()
        }
    }

    suspend fun getCount(boardId: String): CountEntity {
        try {
            return fbDatabase.collection("count").document(boardId).get().await()
                .toObject<CountEntity>()!!
        } catch (e: Exception) {
            Log.d("에러getCount",e.toString())
            return CountEntity()
        }
    }

    suspend fun getUser(email: String): BoardEntity {//Auth컬렉션에서 다른유저 정보 불러올때
        try {
            val snapshot = fbDatabase.collection("auth").whereEqualTo("email", email).get().await()
            val boardEntity = BoardEntity()
            for (data in snapshot) {
                boardEntity.profileImage = data.data.get("image") as String
                boardEntity.nickname = data.data.get("nickname") as String
            }
            return boardEntity
        } catch (e: Exception) {
            Log.d("에러getUser",e.toString())
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
                    .orderBy("timestamp", Query.Direction.ASCENDING).get().await()
            return data.toObjects<CommentEntity>() //비동기 처리후 옵저버 호출
        } catch (e: Exception) {
            Log.d("에러getComment",e.toString())
            return emptyList()
        }
    }

    suspend fun getReply(boardId: String): List<ReplyEntity> {//해당글의 답글 목록
        try {
            val data: QuerySnapshot =
                fbDatabase.collection("board").document(boardId).collection("reply")
                    .orderBy("timestamp", Query.Direction.ASCENDING).get().await()
            return data.toObjects<ReplyEntity>()
        } catch (e: Exception) {
            Log.d("에러getReply",e.toString())
            return emptyList()
        }
    }

    suspend fun getNicknameAuth(email: String): String {//회원 이메일로 닉네임 가져오기 //다른 유저들
        try {
            val snapshot = fbDatabase.collection("auth").whereEqualTo("email", email).get().await()
            var nickname: String = ""
            for (data in snapshot) {//검색결과 데이터가 1개만 나와야됨
                nickname = data.data.get("nickname") as String//닉네임 가져오기
            }
            return nickname
        } catch (e: Exception) {
            Log.d("에러getNicknameAuth",e.toString())
            return ""
        }
    }

    suspend fun getWriteBoardListAuth(email: String): List<String> {
        try {
            val snapshot = fbDatabase.collection("auth").whereEqualTo("email", email).get().await()
            var writeBoardList: List<String> = emptyList()
            for (data in snapshot) {//검색결과 데이터가 1개만 나와야됨
                writeBoardList = data.data.get("boardList") as List<String>//작성글 목록
            }
            return writeBoardList
        } catch (e: Exception){
            Log.d("에러getWriteBoardListAuth",e.toString())
            return emptyList()
        }
    }

    suspend fun getWriteCommentListAuth(email: String): List<String> {
        try {
            val snapshot = fbDatabase.collection("auth").whereEqualTo("email", email).get().await()
            var writeCommentList: List<String> = emptyList()
            for (data in snapshot) {//검색결과 데이터가 1개만 나와야됨
                writeCommentList = data.data.get("commentList") as List<String>//작성글 목록
            }
            return writeCommentList
        } catch (e: Exception){
            Log.d("에러getWriteCommentListAuth",e.toString())
            return emptyList()
        }
    }

    suspend fun getLikeListAuth(email: String): List<String> {
        try {
            val snapshot = fbDatabase.collection("auth").whereEqualTo("email", email).get().await()
            var likeList: List<String> = emptyList()
            for (data in snapshot) {//검색결과 데이터가 1개만 나와야됨
                likeList = data.data.get("likeList") as List<String>//작성글 목록
            }
            return likeList
        } catch (e: Exception){
            Log.d("에러getLikeListAuth",e.toString())
            return emptyList()
        }
    }
}