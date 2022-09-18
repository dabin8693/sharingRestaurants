package com.project.sharingrestaurants.firebase


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.project.sharingrestaurants.MyApplication
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class FBDatabase {

    private val fbDatabase = Firebase.firestore

    companion object{
        private var INSTANCE: FBDatabase? = null

        fun getInstance(): FBDatabase {//스레드 경합없음으로 synchronized필요없음
            if (INSTANCE == null){//중복 생성 방지
                INSTANCE = FBDatabase()
            }
            return INSTANCE ?: FBDatabase()//null이면  재생성
        }
    }

    fun addBoard(boardMap: Map<String, Any>){
        //boardEntity.timestamp = FieldValue.serverTimestamp()
        //boardEntity.documentId = FieldPath.documentId()
        fbDatabase.collection("board").add(boardMap)
        //fbDatabase.collection("").get().addOnSuccessListener { documents -> for (document in documents){ document.id; document.data; document.toObject<BoardEntity>() }; documents.toObjects<BoardEntity>() }
    }

    fun addAuth(authEntity: AuthEntity){
        fbDatabase.collection("auth").add(authEntity)
    }

    fun addComment(commentEntity: CommentEntity, boardId: String){//해당글에 댓글, 답글 달기
        fbDatabase.collection("board").document(boardId).collection("comment").add(commentEntity)

    }

    fun getBoard(): LiveData<List<BoardEntity>>{
        Log.d("리스트 호출","ㄴㅇㄹㄴㅇㄹ")
        val liveData: MutableLiveData<List<BoardEntity>> = MutableLiveData()
        val list: ArrayList<BoardEntity> = ArrayList()
        fbDatabase.collection("board").get().addOnSuccessListener { documents ->
            for (document in documents){
                val boardEntity: BoardEntity = document.toObject<BoardEntity>()
                boardEntity.documentId = document.id//id에 쓰레기값들어가 있어서 진짜 documentId로 저장
                list.add(boardEntity)
                Log.d("도큐먼트id는",document.id)
                Log.d("도큐먼트id list는",document.toObject<BoardEntity>().documentId)
                }
            liveData.value = list
            //liveData.value = documents.toObjects<BoardEntity>() //비동기 처리후 옵저버 호출
        }

        return liveData
    }

    fun getComment(boardId: String): LiveData<List<CommentEntity>>{//해당글의 댓글,답글 목록
        val liveData: MutableLiveData<List<CommentEntity>> = MutableLiveData()
        fbDatabase.collection("board").document(boardId).collection("comment").get().addOnSuccessListener { documents ->

            liveData.value = documents.toObjects<CommentEntity>() //비동기 처리후 옵저버 호출
        }

        return liveData
    }

    fun getAuth(boardId: String): LiveData<List<AuthEntity>>{//해당글의 댓글,답글 목록
        val liveData: MutableLiveData<List<AuthEntity>> = MutableLiveData()
        fbDatabase.collection("auth").get().addOnSuccessListener { documents ->

            liveData.value = documents.toObjects<AuthEntity>() //비동기 처리후 옵저버 호출
        }

        return liveData
    }

}