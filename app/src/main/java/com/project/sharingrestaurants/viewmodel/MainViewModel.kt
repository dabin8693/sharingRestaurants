package com.project.sharingrestaurants.viewmodel

import android.app.Activity
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.firebase.BoardEntity
import com.project.sharingrestaurants.firebase.FBAuth
import com.project.sharingrestaurants.room.ItemRepository

//Activity나 Fragmemt 또는 View의 Context를 참조해서는 안된다.(메모리 누수)(애플리케이션 context 사용)
open class MainViewModel(private val repository: ItemRepository) : ViewModel() {
    val offDrawable: MutableLiveData<Drawable> = MutableLiveData()
    val onDrawable: MutableLiveData<Drawable> = MutableLiveData()
    val upDrawable: MutableLiveData<Drawable> = MutableLiveData()
    val myDrawable: MutableLiveData<Drawable> = MutableLiveData()

    fun setOffDrawable(activity: Activity){
        offDrawable.value = activity.resources.getDrawable(R.drawable.memo,null)
        onDrawable.value = activity.resources.getDrawable(R.drawable.internet2,null)
        upDrawable.value = activity.resources.getDrawable(R.drawable.like2,null)
        myDrawable.value = activity.resources.getDrawable(R.drawable.my2,null)
    }
    fun setOnDrawable(activity: Activity){
        offDrawable.value = activity.resources.getDrawable(R.drawable.memo2,null)
        onDrawable.value = activity.resources.getDrawable(R.drawable.internet,null)
        upDrawable.value = activity.resources.getDrawable(R.drawable.like2,null)
        myDrawable.value = activity.resources.getDrawable(R.drawable.my2,null)
    }
    fun setUpDrawable(activity: Activity){
        offDrawable.value = activity.resources.getDrawable(R.drawable.memo2,null)
        onDrawable.value = activity.resources.getDrawable(R.drawable.internet2,null)
        upDrawable.value = activity.resources.getDrawable(R.drawable.like,null)
        myDrawable.value = activity.resources.getDrawable(R.drawable.my2,null)
    }
    fun setMyDrawable(activity: Activity){
        offDrawable.value = activity.resources.getDrawable(R.drawable.memo2,null)
        onDrawable.value = activity.resources.getDrawable(R.drawable.internet2,null)
        upDrawable.value = activity.resources.getDrawable(R.drawable.like2,null)
        myDrawable.value = activity.resources.getDrawable(R.drawable.my,null)
    }

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