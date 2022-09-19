package com.project.sharingrestaurants.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.data.OffDetailItem
import com.project.sharingrestaurants.room.ItemEntity
import com.project.sharingrestaurants.room.ItemRepository

class OffDetailViewModel(private val repository: ItemRepository) : ViewModel() {
    //val repository : ItemRepository = MyApplication.REPOSITORY//나중에 di사용 Application클래스에서 의존성 관리

    lateinit var item: ItemEntity
    lateinit var newItem: OffDetailItem
    var position: Int = 10000

    fun getList(): LiveData<List<ItemEntity>> {
        return repository.getList()
    }

    fun delete(itemEntity: ItemEntity) {
        repository.delete(itemEntity)
    }
}