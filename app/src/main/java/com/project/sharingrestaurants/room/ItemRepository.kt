package com.project.sharingrestaurants.room

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.android.gms.auth.api.Auth
import com.google.firebase.auth.FirebaseAuth
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.firebase.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class ItemRepository(application: MyApplication) {//나중에 di사용 Application클래스에서 의존성 관리
    private val itemDatabase = ItemDatabase.getInstance(application)
    private val itemDao = itemDatabase.dao()
    private val Auth: FBAuth = FBAuth.getInstance(application)
    private val fbDatabase: FBDatabase = FBDatabase.getInstance()

    companion object{
        private var INSTANCE: ItemRepository? = null

        fun getInstance(): ItemRepository {//스레드 경합없음으로 synchronized필요없음
            if (INSTANCE == null){//중복 생성 방지
                INSTANCE = ItemRepository(MyApplication.INSTANCE)
            }
            return INSTANCE ?: ItemRepository(MyApplication.INSTANCE)//null이면  재생성
        }
    }

    fun getList(): LiveData<List<ItemEntity>>{//room은 livedata로 반환하면 내부적으로 work스레드에서 call(쿼리처리함수)함수를 처리하고 결과값을 postvalue로 전달함
        return itemDao.getList()//따로 io스레드가 필요없다.
    }

    fun searchTitle(query: String?): LiveData<List<ItemEntity>>{
        if (query != null) {
            Log.d("초기화 쿼리타이틀",query)
        }else{
            Log.d("초기화 쿼리타이틀","널임")
        }
        return itemDao.searchByTitle(query)
    }

    fun searchTitleOrBody(query: String?): LiveData<List<ItemEntity>>{
        if (query != null) {
            Log.d("초기화 쿼리타이틀바디",query)
        }else{
            Log.d("초기화 쿼리타이틀바디","널임")
        }
        return itemDao.searchByTitleOrPlace(query)
    }

    fun insert(itemEntity: ItemEntity){
        try {
            CoroutineScope(Dispatchers.IO).launch {
                itemDao.insert(itemEntity)
            }
        }catch (e: Exception){
            Log.d("room insert에러",e.toString())
        }
    }

    fun delete(itemEntity: ItemEntity){
        try {
            CoroutineScope(Dispatchers.IO).launch {
                itemDao.delete(itemEntity)
            }
        }catch (e: Exception){
            Log.d("room delete에러",e.toString())
        }
    }

    //room
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //firestore database

    fun addFBBoard(boardMap: Map<String, Any>){
        fbDatabase.addBoard(boardMap)
    }

    fun addFBAuth(authEntity: AuthEntity){
        fbDatabase.addAuth(authEntity)
    }

    fun addFBComment(commentEntity: CommentEntity, boardId: String){
        fbDatabase.addComment(commentEntity, boardId)
    }

    fun getFBList(): LiveData<List<BoardEntity>>{
        return fbDatabase.getBoard()
    }

    fun getFBAuth(boardId: String): LiveData<List<AuthEntity>>{
        return fbDatabase.getAuth(boardId)
    }

    fun getFBCommentList(boardId: String): LiveData<List<CommentEntity>>{
        return fbDatabase.getComment(boardId)
    }

    //firestore database
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //fireAuth
    fun getAuth(): FBAuth {
        return Auth
    }

}