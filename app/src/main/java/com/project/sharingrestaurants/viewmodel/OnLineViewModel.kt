package com.project.sharingrestaurants.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.project.sharingrestaurants.room.ItemRepository

class OnLineViewModel() : ViewModel() {
    val itemEdit: MutableLiveData<String> = MutableLiveData()
}