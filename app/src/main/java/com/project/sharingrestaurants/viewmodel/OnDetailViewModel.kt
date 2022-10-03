package com.project.sharingrestaurants.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.StorageReference
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.data.OffDetailItem
import com.project.sharingrestaurants.firebase.BoardEntity
import com.project.sharingrestaurants.firebase.FBAuth
import com.project.sharingrestaurants.room.ItemEntity
import com.project.sharingrestaurants.room.ItemRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OnDetailViewModel(private val repository: ItemRepository) : ViewModel() {

    val nicknamMap: MutableMap<String, String> = hashMapOf()//key - email, value - nickname

    fun getAuth(): FBAuth {
        return repository.getAuth()
    }

    fun getStorageRef(): StorageReference{
        return repository.getFBStorageRef()
    }

    fun getLoadBodyData(): LiveData<BoardEntity>{//프로필 이미지, 닉네임, 조회수, 댓글수
        val liveData: MutableLiveData<BoardEntity> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {

            //liveData.postValue()
        }
        return liveData
    }
    fun getLoadLookData(): LiveData<BoardEntity>{//조회수, 댓글수
        val liveData: MutableLiveData<BoardEntity> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {

            //liveData.postValue()
        }
        return liveData
    }
    fun getLoadCommentData(): LiveData<List<Any>>{//댓글, 답글 //Any = CommentEntity, ReplyEntity
        val liveData: MutableLiveData<List<Any>> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {

            //liveData.postValue()
        }
        return liveData
    }

    fun incrementLookBoard(boardId: String){
        repository.incrementLookBoard(boardId)
    }

    fun incrementRecommendsBoard(boardId: String) {
        repository.incrementRecommendsBoard(boardId)
    }
}