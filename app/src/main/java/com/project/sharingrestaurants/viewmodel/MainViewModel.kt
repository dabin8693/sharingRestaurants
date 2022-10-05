package com.project.sharingrestaurants.viewmodel

import android.app.Activity
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.firebase.BoardEntity
import com.project.sharingrestaurants.firebase.UserEntity
import com.project.sharingrestaurants.room.ItemRepository


class MainViewModel(private val repository: ItemRepository) : ViewModel() {
    val offDrawable: MutableLiveData<Drawable> = MutableLiveData()
    val onDrawable: MutableLiveData<Drawable> = MutableLiveData()
    val likeDrawable: MutableLiveData<Drawable> = MutableLiveData()
    val myDrawable: MutableLiveData<Drawable> = MutableLiveData()

    fun setOffDrawable(activity: Activity){
        offDrawable.value = activity.resources.getDrawable(R.drawable.memo,null)
        onDrawable.value = activity.resources.getDrawable(R.drawable.internet2,null)
        likeDrawable.value = activity.resources.getDrawable(R.drawable.like2,null)
        myDrawable.value = activity.resources.getDrawable(R.drawable.my2,null)
    }
    fun setOnDrawable(activity: Activity){
        offDrawable.value = activity.resources.getDrawable(R.drawable.memo2,null)
        onDrawable.value = activity.resources.getDrawable(R.drawable.internet,null)
        likeDrawable.value = activity.resources.getDrawable(R.drawable.like2,null)
        myDrawable.value = activity.resources.getDrawable(R.drawable.my2,null)
    }
    fun setUpDrawable(activity: Activity){
        offDrawable.value = activity.resources.getDrawable(R.drawable.memo2,null)
        onDrawable.value = activity.resources.getDrawable(R.drawable.internet2,null)
        likeDrawable.value = activity.resources.getDrawable(R.drawable.like,null)
        myDrawable.value = activity.resources.getDrawable(R.drawable.my2,null)
    }
    fun setMyDrawable(activity: Activity){
        offDrawable.value = activity.resources.getDrawable(R.drawable.memo2,null)
        onDrawable.value = activity.resources.getDrawable(R.drawable.internet2,null)
        likeDrawable.value = activity.resources.getDrawable(R.drawable.like2,null)
        myDrawable.value = activity.resources.getDrawable(R.drawable.my,null)
    }

    fun getAuth(): UserEntity {
        return repository.getAuth()
    }

    fun getIsLogin(): Boolean{
        return repository.getIsLogin()
    }

    fun addFBBoard(boardMap: Map<String, Any>){
        //repository.addFBBoard(boardMap)
    }

    fun getFBList(): LiveData<List<BoardEntity>> {
        return repository.getFBList()
    }
}