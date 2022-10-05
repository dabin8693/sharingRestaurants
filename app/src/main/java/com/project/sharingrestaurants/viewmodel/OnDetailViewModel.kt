package com.project.sharingrestaurants.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.StorageReference
import com.project.sharingrestaurants.firebase.BoardEntity
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

    fun getIsLogin(): Boolean{
        return repository.getIsLogin()
    }

    fun getStorageRef(): StorageReference{
        return repository.getFBStorageRef()
    }

    fun getLoadBodyData(): LiveData<BoardEntity>{//프로필 이미지, 닉네임, 조회수, 댓글수
        val liveData: MutableLiveData<BoardEntity> = MutableLiveData()
        CoroutineScope(Dispatchers.Main).launch {

        }
        return liveData
    }
    fun getLoadLookData(): LiveData<BoardEntity>{//조회수, 댓글수
        val liveData: MutableLiveData<BoardEntity> = MutableLiveData()
        CoroutineScope(Dispatchers.Main).launch {

            //liveData.postValue()
        }
        return liveData
    }
    fun getLoadCommentData(): LiveData<List<Any>>{//댓글, 답글 //Any = CommentEntity, ReplyEntity
        val liveData: MutableLiveData<List<Any>> = MutableLiveData()
        CoroutineScope(Dispatchers.Main).launch {

            //liveData.postValue()
        }
        return liveData
    }

    fun incrementLook(boardId: String){
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