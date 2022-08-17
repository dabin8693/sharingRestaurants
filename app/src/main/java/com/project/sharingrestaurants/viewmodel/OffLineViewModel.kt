package com.project.sharingrestaurants.viewmodel

import android.text.TextUtils
import android.util.Log
import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.room.ItemEntity
import com.project.sharingrestaurants.room.ItemRepository

class OffLineViewModel : ViewModel() {
    val repository : ItemRepository = MyApplication.REPOSITORY

    val spinnerName : MutableLiveData<String> = MutableLiveData()
    val searchText : MutableLiveData<String> = MutableLiveData()
    val sarchTextDelay : MutableLiveData<String> = MutableLiveData()



    fun getItemList(): LiveData<List<ItemEntity>> {
        //sarchTextDelay.value가 null이면 getList쿼리 날리고 새로 받은 Livedata를 반환 아니면 search쿼리
        return Transformations.switchMap<String, List<ItemEntity>>(
            sarchTextDelay, Function<String, LiveData<List<ItemEntity>>> { query: String ->
                if (query == null) {
                    return@Function repository.getList()//LiveData<Y>타입
                } else {
                    if (spinnerName.value == "제목") {
                        return@Function repository.searchTitle("*$query*")//LiveData<Y>타입
                    }else{
                        return@Function repository.searchTitleOrBody("*$query*")
                    }
                }
            }
        )
    }

    fun insert(itemEntity: ItemEntity) {
        repository.insert(itemEntity)
    }

    fun delete(itemEntity: ItemEntity) {
        repository.delete(itemEntity)
    }
}