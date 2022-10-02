package com.project.sharingrestaurants.firebase


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.tasks.await

import kotlin.collections.ArrayList


class FBDatabase {

    private val fbDatabase = Firebase.firestore

    companion object {
        private var INSTANCE: FBDatabase? = null

        fun getInstance(): FBDatabase {//스레드 경합없음으로 synchronized필요없음
            if (INSTANCE == null) {//중복 생성 방지
                INSTANCE = FBDatabase()
            }
            return INSTANCE ?: FBDatabase()//null이면  재생성
        }
    }
    //boardEntity.timestamp = FieldValue.serverTimestamp()
    //boardEntity.documentId = FieldPath.documentId()
    //fbDatabase.collection("").get().addOnSuccessListener { documents -> for (document in documents){ document.id; document.data; document.toObject<BoardEntity>() }; documents.toObjects<BoardEntity>() }
    fun addBoard(boardMap: MutableMap<String, Any>): LiveData<Boolean>{
        val liveData: MutableLiveData<Boolean> = MutableLiveData()
        val documentRef: DocumentReference = fbDatabase.collection("board").document()
        boardMap.replace("documentId", documentRef.id)//api24이상
            documentRef.set(boardMap)
            .addOnSuccessListener { liveData.postValue(true) }
            .addOnFailureListener { liveData.postValue(false) }

        return liveData
    }

    fun addAuth(currentUser: FirebaseUser, nickname: String) {//추가 또는 닉네임 수정
        fbDatabase.collection("auth").document(currentUser.uid).set(
            hashMapOf(
                "uid" to currentUser.uid,
                "email" to currentUser.email,
                "nickname" to nickname,
                "timestamp" to FieldValue.serverTimestamp()
            )
        )
    }

    fun addComment(commentEntity: CommentEntity, boardId: String) {//해당글에 댓글, 답글 달기
        fbDatabase.collection("board").document(boardId).collection("comment").add(commentEntity)

    }

    fun insertBoard(boardMap: MutableMap<String, Any>): LiveData<Boolean>{
        val liveData: MutableLiveData<Boolean> = MutableLiveData()
        fbDatabase.collection("board").document(boardMap.get("documentId").toString()).update(boardMap)
            .addOnSuccessListener { liveData.postValue(true) }
            .addOnFailureListener { liveData.postValue(false) }
        return liveData
    }

    fun getBoard(): LiveData<List<BoardEntity>> {
        Log.d("리스트 호출", "ㄴㅇㄹㄴㅇㄹ")
        val liveData: MutableLiveData<List<BoardEntity>> = MutableLiveData()

        //var list: ArrayList<BoardEntity> = ArrayList()
        fbDatabase.collection("board").get().addOnSuccessListener { documents ->
            /*
            for (document in documents) {
                val boardEntity: BoardEntity = document.toObject<BoardEntity>()
                boardEntity.documentId = document.id//id에 쓰레기값들어가 있어서 진짜 documentId로 저장
                list.add(boardEntity)
                Log.d("도큐먼트id는", document.id)
                Log.d("도큐먼트id list는", document.toObject<BoardEntity>().documentId)
            }
            liveData.value = list
         */
            liveData.value = documents.toObjects<BoardEntity>() as ArrayList<BoardEntity>
        }

        return liveData
    }

    fun getComment(boardId: String): LiveData<List<CommentEntity>> {//해당글의 댓글,답글 목록
        val liveData: MutableLiveData<List<CommentEntity>> = MutableLiveData()
        fbDatabase.collection("board").document(boardId).collection("comment").get()
            .addOnSuccessListener { documents ->

                liveData.value = documents.toObjects<CommentEntity>() //비동기 처리후 옵저버 호출
            }

        return liveData
    }

    fun getAuth(boardId: String): LiveData<List<AuthEntity>> {//회원 목록
        val liveData: MutableLiveData<List<AuthEntity>> = MutableLiveData()
        fbDatabase.collection("auth").get().addOnSuccessListener { documents ->

            liveData.value = documents.toObjects<AuthEntity>() //비동기 처리후 옵저버 호출
        }

        return liveData
    }

    fun isAuth(auth: FBAuth): LiveData<Boolean> {
        val liveData: MutableLiveData<Boolean> = MutableLiveData()
        fbDatabase.collection("auth").whereEqualTo("uid", auth.currentUser!!.uid).get()
            .addOnSuccessListener {
                if (it.isEmpty) {//회원정보가 없다
                    liveData.postValue(false)
                } else {//회원정보가 있다
                    for (data in it) {
                        auth.nickname = data.data.get("nickname") as String//닉네임 가져오기
                    }
                    liveData.postValue(true)
                }
            }.addOnFailureListener {
            liveData.postValue(false)
        }

        return liveData
    }

}