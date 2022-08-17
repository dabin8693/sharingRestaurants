package com.project.sharingrestaurants.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.room.ItemRepository

//Activity나 Fragmemt 또는 View의 Context를 참조해서는 안된다.(메모리 누수)(애플리케이션 context 사용)
open class MainViewModel : ViewModel() {
    val repository : ItemRepository = MyApplication.REPOSITORY

    var a : MutableLiveData<Int> = MutableLiveData<Int>().apply { value = 1 }
    var b : MutableLiveData<Int> = MutableLiveData()

}