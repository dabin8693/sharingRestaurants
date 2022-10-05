package com.project.sharingrestaurants.viewmodel

import androidx.lifecycle.ViewModel
import com.project.sharingrestaurants.firebase.UserEntity
import com.project.sharingrestaurants.room.ItemRepository

class UserViewModel(private val repository: ItemRepository): ViewModel() {

    fun signOut(){
        repository.signOut()
    }

    fun getIsLogin(): Boolean {
        return repository.getIsLogin()
    }
    fun getAuth(): UserEntity {
        return repository.getAuth()
    }

}