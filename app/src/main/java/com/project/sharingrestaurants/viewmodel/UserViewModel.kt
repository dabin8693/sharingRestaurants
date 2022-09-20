package com.project.sharingrestaurants.viewmodel

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.firebase.FBAuth
import com.project.sharingrestaurants.firebase.FBLogin
import com.project.sharingrestaurants.room.ItemRepository

class UserViewModel(private val repository: ItemRepository, private val login: FBLogin): ViewModel() {

    fun signOut(){
        FBLogin(repository.getAuth()).signOut()
    }

    fun getIsLogin(): Boolean {
        return repository.getAuth().isLogin.value!!
    }
    fun getAuth(): FBAuth {
        return repository.getAuth()
    }

}