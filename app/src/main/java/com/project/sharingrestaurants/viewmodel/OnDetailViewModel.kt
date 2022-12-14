package com.project.sharingrestaurants.viewmodel

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.StorageReference
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.firebase.BoardEntity
import com.project.sharingrestaurants.firebase.CommentEntity
import com.project.sharingrestaurants.firebase.ReplyEntity
import com.project.sharingrestaurants.firebase.UserEntity
import com.project.sharingrestaurants.room.ItemRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OnDetailViewModel(private val repository: ItemRepository) : ViewModel() {

    val likeDrawable: MutableLiveData<Drawable> = MutableLiveData()
    val itemComment: MutableLiveData<String> = MutableLiveData()
    val likes: MutableLiveData<String> = MutableLiveData()
    val comments: MutableLiveData<String> = MutableLiveData()

    val comment: MutableLiveData<Boolean> = MutableLiveData()
    val nicknameMap: MutableMap<String, String> = hashMapOf()//key - email, value - nickname
    val profileImageMap: MutableMap<String, String> = hashMapOf()//key - email, value - profileImage
    var isLike: Boolean = false
    val likeIsUpdate: MutableLiveData<Boolean> = MutableLiveData()
    var writeCommentList: List<String> = emptyList()
    var likeListAuth: List<String> = emptyList()

    init {
        likeIsUpdate.value = true
    }

    fun setLikeDrawable(context: Context, bool: Boolean) {//true = 이미 추천함
        if (!bool) {//추천x
            likeDrawable.value = context.resources.getDrawable(R.drawable.like2, null)
        } else {
            likeDrawable.value = context.resources.getDrawable(R.drawable.like, null)
        }
    }

    fun getAuth(): UserEntity {
        return repository.getAuth()
    }

    fun getWriteCommentList() {//로그인상태일때만 호출
        CoroutineScope(Dispatchers.Main).launch {
            writeCommentList = repository.getWriteCommentListAuth(getAuth().email)
        }
    }

    fun getLikeListAuth(context: Context) {//로그인상태일때만 호출
        CoroutineScope(Dispatchers.Main).launch {
            likeListAuth = repository.getLikeListAuth(getAuth().email)
            likeViewInit(context)
        }
    }

    private fun likeViewInit(context: Context){
        for (user in likeListAuth){
            if (getAuth().email.equals(user)){//추천한적 있다
                isLike = true
                setLikeDrawable(context,true)
            }else{
                isLike = false
                setLikeDrawable(context,false)
            }
        }
    }

    fun getIsLogin(): Boolean {
        return repository.getIsLogin()
    }

    fun getStorageRef(): StorageReference {
        return repository.getFBStorageRef()
    }

    suspend fun getBoard(boardId: String): BoardEntity {
        return repository.getBoard(boardId)
    }

    fun getBoardUser(email: String): LiveData<BoardEntity> {//프로필 이미지, 닉네임
        val liveData: MutableLiveData<BoardEntity> = MutableLiveData()
        CoroutineScope(Dispatchers.Main).launch {
            val boardEntity = repository.getUserInform(email)
            if (boardEntity.nickname.equals("")) {
                //실패
            } else {
                nicknameMap.set(email, boardEntity.nickname)
                profileImageMap.set(email, boardEntity.profileImage)
                liveData.value = boardEntity
            }
        }
        return liveData
    }

    fun getCommentObserver(): LiveData<Boolean> {
        return comment
    }

    fun getLoadCommentData(boardId: String): LiveData<List<Any>> {//댓글, 답글 //Any = CommentEntity, ReplyEntity
        val liveData: MutableLiveData<List<Any>> = MutableLiveData()
        CoroutineScope(Dispatchers.Main).launch {
            val replyList = repository.getReplyList(boardId) as ArrayList
            val commentList = repository.getCommentList(boardId) as ArrayList
            var a = 0
            for (comment in commentList) {//닉네임 불러오고 저장
                if (!nicknameMap.containsKey(comment.email)) {//없으면
                    val user = repository.getUserInform(comment.email)
                    comment.nickname = user.nickname
                    comment.profileImage = user.profileImage
                    commentList.set(a, comment)
                    nicknameMap.set(comment.email, user.nickname)
                    profileImageMap.set(comment.email, user.profileImage)
                } else {
                    comment.nickname = nicknameMap.get(comment.email).toString()
                    comment.profileImage = profileImageMap.get(comment.email).toString()
                    commentList.set(a, comment)
                }
                a++
            }
            var b = 0
            for (reply in replyList) {//닉네임 불러오고 저장
                if (!nicknameMap.containsKey(reply.email)) {
                    val user = repository.getUserInform(reply.email)
                    reply.nickname = user.nickname
                    reply.profileImage = user.profileImage
                    replyList.set(b, reply)
                    nicknameMap.set(reply.email, user.nickname)
                    profileImageMap.set(reply.email, user.profileImage)
                } else {
                    reply.nickname = nicknameMap.get(reply.email).toString()
                    reply.profileImage = profileImageMap.get(reply.email).toString()
                    replyList.set(a, reply)
                }
                b++
            }
            val allComment: ArrayList<Any> = ArrayList()
            allComment.addAll(commentList)
            var index = 0
            for (comment in commentList) {//댓글 답글 합치기
                for (reply in replyList) {
                    if (comment.email.equals(reply.commentId)) {
                        index++
                        allComment.add(index, reply)
                    }
                }
                index++
            }

            liveData.value = allComment
        }
        return liveData
    }

    fun incrementLook(boardId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            repository.incrementLook(boardId)
        }
    }

    fun incrementLike(boardId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            var bool: Boolean = false
            likeIsUpdate.value = false
            bool = repository.incrementLike(boardId)
            if (bool) {
                addLikeListAuth()
            }
            likeIsUpdate.value = bool
        }
    }

    fun decrementLike(boardId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            var bool: Boolean = false
            likeIsUpdate.value = false
            bool = repository.decrementLike(boardId)
            if (bool) {
                bool = removeLikeListAuth()
            }
            likeIsUpdate.value = bool
        }
    }


    private suspend fun addLikeListAuth() {
        val list = likeListAuth.toMutableList()
        list.add(getAuth().email)
        repository.insertLikeListAuth(list)
    }

    private suspend fun removeLikeListAuth(): Boolean {
        val list = likeListAuth.toMutableList()
        list.remove(getAuth().email)
        return repository.insertLikeListAuth(list)
    }

    fun addComment(boardId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val commentId = repository.addComment(
                hashMapOf(
                    "documentId" to "",
                    "uid" to getAuth().uid,
                    "email" to getAuth().email,
                    "boardId" to boardId,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "body" to (itemComment.value ?: ""),
                    "isDelete" to false,
                )
            )
            if (!commentId.equals("")) {
                insertWriteCommentListAuth(commentId)
            }
            comment.value = true
        }
    }

    private suspend fun insertWriteCommentListAuth(commentId: String) {//로그인상태일때만 호출
        val list = writeCommentList.toMutableList()
        list.add(commentId)
        repository.insertWriteCommentListAuth(list)
    }
}