package com.project.sharingrestaurants.room

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.storage.StorageReference
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.firebase.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class ItemRepository(application: MyApplication) {
    private val itemDatabase = ItemDatabase.getInstance(application)
    private val itemDao = itemDatabase.dao()
    private val Auth: FBAuth = FBAuth.getInstance(application)//파이어베이스 중에 제일 먼저 초기화
    private val fbDatabase: FBDatabase = FBDatabase.getInstance()
    private val fbStorage: FBStorage = FBStorage.getInstance()

    companion object{
        private var INSTANCE: ItemRepository? = null

        fun getInstance(): ItemRepository {//synchronized필요없음
            if (INSTANCE == null){//중복 생성 방지
                INSTANCE = ItemRepository(MyApplication.INSTANCE)
            }
            return INSTANCE ?: ItemRepository(MyApplication.INSTANCE)//null이면  재생성
        }
    }

    fun getList(): LiveData<List<ItemEntity>>{//room은 반환값을 livedata로 하면 내부적으로 work스레드에서 call(쿼리처리함수)함수를 처리하고 결과값을 postvalue로 전달함
        return itemDao.getList()//따로 io스레드가 필요없다.
    }

    fun searchTitle(query: String?): LiveData<List<ItemEntity>>{
        return itemDao.searchByTitle(query)
    }

    fun searchTitleOrBody(query: String?): LiveData<List<ItemEntity>>{
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

    fun addFBBoard(boardMap: MutableMap<String, Any>): LiveData<Boolean>{
        return fbDatabase.addBoard(boardMap)
    }

    fun insertFBBoard(boardMap: MutableMap<String, Any>): LiveData<Boolean>{
        return fbDatabase.insertBoard(boardMap)
    }

    fun insertNicknameAuth(nickname: String): LiveData<Boolean>{
        return fbDatabase.insertNicknameAuth(getAuth().nickname, nickname)
    }

    fun incrementLook(boardId: String){
        fbDatabase.incrementLook(boardId)
    }

    fun incrementLike(boardId: String) {
        fbDatabase.incrementLike(boardId)
    }

    fun addFBAuth(){
        fbDatabase.addAuth(getAuth())
    }

    fun isFBAuth(): LiveData<Boolean>{//false = 회원정보x
        return fbDatabase.isAuth(getAuth())
    }

    fun addFBComment(commentMap: MutableMap<String, Any>, boardId: String){
        fbDatabase.addComment(commentMap, boardId)
    }

    fun addReply(replyMap: MutableMap<String, Any>, boardId: String){
        fbDatabase.addReply(replyMap, boardId)
    }

    fun getFBList(): LiveData<List<BoardEntity>>{
        return fbDatabase.getBoard()
    }

    suspend fun getFBNicknameAuth(email: String): String{
        return fbDatabase.getNicknameAuth(email)
    }

    fun getFBCommentList(boardId: String): LiveData<List<CommentEntity>>{
        return fbDatabase.getComment(boardId)
    }

    fun getFBReplyList(boardId: String): LiveData<List<ReplyEntity>>{
        return fbDatabase.getReply(boardId)
    }

    //firestore database
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //firestore storage
    fun getFBStorageRef(): StorageReference = fbStorage.getFBStorageRef()

    suspend fun addImageFBStorage(uid: String, time: String, name: String, data: ByteArray): String{
        return fbStorage.addImage(uid, time, name, data)
    }
    //firestore storage
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //fireAuth
    fun getAuth(): UserEntity {
        return Auth.getUser()
    }

    fun getIsLogin(): Boolean{
        return Auth.getIsLogin()
    }

    fun signOut() {
        return Auth.signOut()
    }

    fun signInGoogle(acct: GoogleSignInAccount, context: Activity, callback: () -> Unit){
        Auth.firebaseAuthWithGoogle(acct, context, callback)
    }

    fun getGoogleSignInClient(): GoogleSignInClient {
        return Auth.getGoogleSignInClient()
    }

}