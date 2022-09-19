package com.project.sharingrestaurants.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.sharingrestaurants.room.ItemRepository

class ViewModelFactory(private val repository: ItemRepository): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)){
            return MainViewModel(repository) as T
        }else if(modelClass.isAssignableFrom(OffAddViewModel::class.java)){
            return OffAddViewModel(repository) as T
        }else if(modelClass.isAssignableFrom(OffDetailViewModel::class.java)){
            return OffDetailViewModel(repository) as T
        }else if(modelClass.isAssignableFrom(OffLineViewModel::class.java)){
            return OffLineViewModel(repository) as T
        }else if(modelClass.isAssignableFrom(OnAddViewModel::class.java)){
            return OnAddViewModel(repository) as T
        }else if(modelClass.isAssignableFrom(OnDetailViewModel::class.java)){
            return OnDetailViewModel(repository) as T
        }else if(modelClass.isAssignableFrom(OnLineViewModel::class.java)){
            return OnLineViewModel(repository) as T
        }else if(modelClass.isAssignableFrom(UserViewModel::class.java)){
            return UserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown View Model Class")
    }
}