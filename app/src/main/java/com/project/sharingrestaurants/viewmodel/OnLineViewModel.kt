package com.project.sharingrestaurants.viewmodel

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.*
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.firebase.BoardEntity
import com.project.sharingrestaurants.firebase.FBAuth
import com.project.sharingrestaurants.firebase.FBLogin
import com.project.sharingrestaurants.room.ItemEntity
import com.project.sharingrestaurants.room.ItemRepository
import com.project.sharingrestaurants.util.DataTrans

class OnLineViewModel(private val repository: ItemRepository, private val login: FBLogin): ViewModel() {
    //private val repository : ItemRepository = MyApplication.REPOSITORY//나중에 di사용 Application클래스에서 의존성 관리

    val currentLatitude: MutableLiveData<Double> = MutableLiveData()
    val currentLongitude: MutableLiveData<Double> = MutableLiveData()
    val itemEdit: MutableLiveData<String> = MutableLiveData()

    fun getList(): LiveData<List<BoardEntity>>{
        return repository.getFBList()
    }

    fun getCurrentGPS(activity: Activity?): LiveData<DataTrans.gps> {
        return DataTrans.requestLastLocation(activity!!)
    }

    fun signIn(account: GoogleSignInAccount, activity: Activity?, callback: () -> Unit) {
        login.firebaseAuthWithGoogle(account, activity!!, callback)//FBLogin()이거 Factory로 받아오게 변경
    }
    fun getIsLogin(): Boolean {
        return repository.getAuth().isLogin.value!!
    }
    fun getAuth(): FBAuth {
        return repository.getAuth()
    }

    fun addFBAuth(lifecycleOwner: LifecycleOwner){
        repository.isFBAuth().observe(lifecycleOwner){//회원정보 있으면 닉네임 가져옴
            if (it){//회원정보o
                repository.addFBAuth(getAuth().nickname)
            }else{//회원정보x 닉네임 기본값으로 이메일 추가
                repository.addFBAuth(getAuth().currentUser!!.email!!.split("@").get(0))
            }
        }
    }

}