package com.project.sharingrestaurants.viewmodel

import android.app.Activity
import androidx.lifecycle.*
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.storage.StorageReference
import com.project.sharingrestaurants.firebase.BoardEntity
import com.project.sharingrestaurants.firebase.UserEntity
import com.project.sharingrestaurants.room.ItemRepository
import com.project.sharingrestaurants.util.DataTrans
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OnLineViewModel(private val repository: ItemRepository): ViewModel() {

    val currentLatitude: MutableLiveData<Double> = MutableLiveData()
    val currentLongitude: MutableLiveData<Double> = MutableLiveData()

    fun getList(): LiveData<List<BoardEntity>>{
        val liveData: MutableLiveData<List<BoardEntity>> = MutableLiveData()
        CoroutineScope(Dispatchers.Main).launch {
            liveData.value = repository.getFBList()
        }
        return liveData
    }

    fun getCurrentGPS(activity: Activity?): LiveData<DataTrans.gps> {
        return DataTrans.requestLastLocation(activity!!)
    }

    fun signIn(account: GoogleSignInAccount, activity: Activity?, callback: () -> Unit) {
        repository.signInGoogle(account, activity!!, callback)
    }
    fun getIsLogin(): Boolean {
        return repository.getIsLogin()
    }
    fun getAuth(): UserEntity {
        return repository.getAuth()
    }

    fun addFBAuth(){
        CoroutineScope(Dispatchers.Main).launch {
            repository.addFBAuth()//회원정보 추가
        }
    }

    fun getGoogleSignInClient(): GoogleSignInClient {
        return repository.getGoogleSignInClient()
    }

    fun getStorageRef(): StorageReference{
        return repository.getFBStorageRef()
    }
}