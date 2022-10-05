package com.project.sharingrestaurants.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.StorageReference
import com.project.sharingrestaurants.firebase.BoardEntity
import com.project.sharingrestaurants.firebase.CommentEntity
import com.project.sharingrestaurants.firebase.ReplyEntity
import com.project.sharingrestaurants.firebase.UserEntity
import com.project.sharingrestaurants.room.ItemRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OnDetailViewModel(private val repository: ItemRepository) : ViewModel() {

    val nicknameMap: MutableMap<String, String> = hashMapOf()//key - email, value - nickname
    var isRecomment: Boolean = false//이게 true면 추천수 불러올때 추가로 1더하기

    fun getAuth(): UserEntity {
        return repository.getAuth()
    }

    fun getIsLogin(): Boolean {
        return repository.getIsLogin()
    }

    fun getStorageRef(): StorageReference {
        return repository.getFBStorageRef()
    }

    suspend fun getBoard(boardId: String): BoardEntity{
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
                liveData.value = boardEntity
            }
        }
        return liveData
    }


    fun getLoadCommentData(boardId: String): LiveData<List<Any>> {//댓글, 답글 //Any = CommentEntity, ReplyEntity
        val liveData: MutableLiveData<List<Any>> = MutableLiveData()
        CoroutineScope(Dispatchers.Main).launch {
            val replyList = repository.getReplyList(boardId) as ArrayList
            val commentList = repository.getCommentList(boardId) as ArrayList
            var a = 0
            for (comment in commentList) {//닉네임 불러오고 저장
                if (nicknameMap.get(comment.email) == null) {
                    val user = repository.getUserInform(comment.email)
                    comment.nickname = user.nickname
                    comment.profileImage = user.profileImage
                    commentList.set(a, comment)
                    nicknameMap.set(comment.email, user.nickname)
                }
                a++
            }
            var b = 0
            for (reply in replyList) {//닉네임 불러오고 저장
                if (nicknameMap.get(reply.email) == null) {
                    val user = repository.getUserInform(reply.email)
                    reply.nickname = user.nickname
                    reply.profileImage = user.profileImage
                    replyList.set(b, reply)
                    nicknameMap.set(reply.email, user.nickname)
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
            repository.incrementLike(boardId)
        }
    }
}