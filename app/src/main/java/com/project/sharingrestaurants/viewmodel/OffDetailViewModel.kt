package com.project.sharingrestaurants.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.project.sharingrestaurants.data.OffItem
import com.project.sharingrestaurants.room.ItemEntity
import com.project.sharingrestaurants.room.ItemRepository

class OffDetailViewModel(private val repository: ItemRepository) : ViewModel() {

    lateinit var item: ItemEntity
    lateinit var newItem: OffItem
    var position: Int = 10000

    fun getList(): LiveData<List<ItemEntity>> {
        return repository.getList()
    }

    fun delete(itemEntity: ItemEntity) {
        repository.delete(itemEntity)
    }
}