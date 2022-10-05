package com.project.sharingrestaurants.firebase


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.lang.Exception


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

    fun addBoard(boardMap: MutableMap<String, Any>): LiveData<Boolean> {
        val liveData: MutableLiveData<Boolean> = MutableLiveData()
        val documentRef: DocumentReference = fbDatabase.collection("board").document()
        boardMap.replace("documentId", documentRef.id)//api24이상
        documentRef.set(boardMap)
            .addOnSuccessListener { liveData.value = true }
            .addOnFailureListener { liveData.value = false }

        return liveData
    }

    fun addAuth(user: UserEntity) {//추가 //처음 추가할때는 닉네임
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

    fun addComment(commentMap: MutableMap<String, Any>, boardId: String) {//해당글에 댓글
        val documentRef: DocumentReference =
            fbDatabase.collection("board").document(boardId).collection("comment").document()
        commentMap.replace("documentId", documentRef.id)
        documentRef.set(commentMap)
    }

    fun addReply(replyMap: MutableMap<String, Any>, boardId: String) {//해당글에 댓글
        val documentRef: DocumentReference =
            fbDatabase.collection("board").document(boardId).collection("reply").document()
        replyMap.replace("documentId", documentRef.id)
        documentRef.set(replyMap)
    }

    fun insertBoard(boardMap: MutableMap<String, Any>): LiveData<Boolean> {
        val liveData: MutableLiveData<Boolean> = MutableLiveData()
        fbDatabase.collection("board").document(boardMap.get("documentId").toString())
            .update(boardMap)
            .addOnSuccessListener { liveData.value = true }
            .addOnFailureListener { liveData.value = false }
        return liveData
    }

    fun incrementLook(boardId: String){//boardId = documentId
        fbDatabase.collection("count").document(boardId)
            .update("look",FieldValue.increment(1))//조회수 증가
            .addOnSuccessListener {  }
            .addOnFailureListener {  }
    }

    fun incrementLike(boardId: String) {//like = 추천
        fbDatabase.collection("count").document(boardId)
            .update("like",FieldValue.increment(1))//추천수 증가
            .addOnSuccessListener {  }
            .addOnFailureListener {  }
    }

    fun insertNicknameAuth(uid: String, nickname: String): LiveData<Boolean> {
        val liveData: MutableLiveData<Boolean> = MutableLiveData()
        fbDatabase.collection("auth").document(uid).update("nickname", nickname)
            .addOnSuccessListener { liveData.value = true }
            .addOnFailureListener { liveData.value = false }
        return liveData
    }

    fun getBoard(): LiveData<List<BoardEntity>> {
        val liveData: MutableLiveData<List<BoardEntity>> = MutableLiveData()
        fbDatabase.collection("board").get()
            .addOnSuccessListener { documents ->
            liveData.value = documents.toObjects<BoardEntity>()
        }

        return liveData
    }
    //댓글목록을 먼저 다 불러오고 그 다음 답글목록을 전부 불러온다 댓글list에 답글list를 삽입 삽입 순서는 답글 TimeStamp순서로 삽입 위치 결정 방법은 답글 필드의 댓글documentId기준
    //어뎁터에서 댓글 답글 분류는 data class타입 비교로
    //댓글, 답글도 코루틴으로
    fun getComment(boardId: String): LiveData<List<CommentEntity>> {//해당글의 댓글 목록
        val liveData: MutableLiveData<List<CommentEntity>> = MutableLiveData()
        fbDatabase.collection("board").document(boardId).collection("comment").get()
            .addOnSuccessListener { documents ->
                liveData.value = documents.toObjects<CommentEntity>() //비동기 처리후 옵저버 호출
            }

        return liveData
    }

    fun getReply(boardId: String): LiveData<List<ReplyEntity>> {//해당글의 답글 목록
        val liveData: MutableLiveData<List<ReplyEntity>> = MutableLiveData()
        fbDatabase.collection("board").document(boardId).collection("reply").get()
            .addOnSuccessListener { documents ->
                liveData.value = documents.toObjects<ReplyEntity>() //비동기 처리후 옵저버 호출
            }

        return liveData
    }

    suspend fun getNicknameAuth(email: String): String {//회원 이메일로 닉네임 가져오기 //다른 유저들
        try {
            //중복 호출 방지하기 위해 viewmodel에서 회원정보들을 hashMap(key:email, value:nickname)으로 저장하고 없으면 호출하는식으로
            //fragment, detailactivy가 hashmap을 공유하고 fragment가 체인지 되기전까지 유지 그 이후는 다시 최신화
            val task = fbDatabase.collection("auth").whereEqualTo("email", email).get().await()
            var nickname: String = ""
            for (data in task) {
                nickname = data.data.get("nickname") as String//닉네임 가져오기
            }
            return nickname
        } catch (e: Exception) {
            return ""
        }
    }

    fun isAuth(user: UserEntity): LiveData<Boolean> {//현재로그인유저 회원정보있는지 확인
        val liveData: MutableLiveData<Boolean> = MutableLiveData()
        Log.d("uid는",user.uid)
        fbDatabase.collection("auth").whereEqualTo("uid", user.uid).get()
            .addOnSuccessListener {
                if (it.isEmpty) {//회원정보가 없다
                    user.nickname = user.email.split("@").get(0)//초기값 설정
                    liveData.value = false
                } else {//회원정보가 있다
                    for (data in it) {
                        user.nickname = data.data.get("nickname") as String//닉네임 가져오기
                    }
                    liveData.value = true
                }
            }.addOnFailureListener {
                user.nickname = user.email.split("@").get(0)//초기값 설정
                liveData.value = false
            }

        return liveData
    }

}