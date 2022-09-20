package com.project.sharingrestaurants.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.firebase.BoardEntity
import com.project.sharingrestaurants.firebase.FBAuth
import com.project.sharingrestaurants.room.ItemRepository

//Activity나 Fragmemt 또는 View의 Context를 참조해서는 안된다.(메모리 누수)(애플리케이션 context 사용)
open class MainViewModel(private val repository: ItemRepository) : ViewModel() {


    fun getAuth(): FBAuth{
        return repository.getAuth()
    }

    fun addFBBoard(boardMap: Map<String, Any>){
        //repository.addFBBoard(boardMap)
    }

    fun getFBList(): LiveData<List<BoardEntity>> {
        return repository.getFBList()
    }
}