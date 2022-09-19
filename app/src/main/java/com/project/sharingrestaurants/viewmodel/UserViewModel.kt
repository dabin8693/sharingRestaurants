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

class UserViewModel(private val repository: ItemRepository): ViewModel() {
    //val repository : ItemRepository = MyApplication.REPOSITORY//나중에 di사용 Application클래스에서 의존성 관리

    fun signOut(){
        FBLogin(repository.getAuth()).signOut()
    }
    fun signIn(account: GoogleSignInAccount, activity: Activity?, callback: () -> Unit) {
        FBLogin(repository.getAuth()).firebaseAuthWithGoogle(account, activity!!, callback)
    }
    fun getIsLogin(): Boolean {
        return repository.getAuth().isLogin.value!!
    }
    fun getAuth(): FBAuth {
        return repository.getAuth()
    }

}