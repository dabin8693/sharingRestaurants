package com.project.sharingrestaurants.viewmodel

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.arch.core.util.Function
import androidx.lifecycle.*
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.custom.CustomDialog
import com.project.sharingrestaurants.firebase.AuthEntity
import com.project.sharingrestaurants.firebase.FBAuth
import com.project.sharingrestaurants.firebase.FBLogin
import com.project.sharingrestaurants.room.ItemEntity
import com.project.sharingrestaurants.room.ItemRepository
import com.project.sharingrestaurants.util.DataTrans

class OffLineViewModel(private val repository: ItemRepository, private val login: FBLogin) : ViewModel() {

    val spinnerName: MutableLiveData<String> = MutableLiveData()
    val searchText: MutableLiveData<String> = MutableLiveData()
    val sarchTextDelay: MutableLiveData<String> = MutableLiveData()
    val currentLatitude: MutableLiveData<Double> = MutableLiveData()
    val currentLongitude: MutableLiveData<Double> = MutableLiveData()



    fun getItemList(spinnerItemTitle: String): LiveData<List<ItemEntity>> {
        //sarchTextDelay.value가 null이면 getList쿼리 날리고 새로 받은 Livedata를 반환 아니면 search쿼리
        return Transformations.switchMap<String, List<ItemEntity>>(
            sarchTextDelay, Function<String, LiveData<List<ItemEntity>>> { query: String ->
                if (query == "") {
                    return@Function repository.getList()//LiveData<Y>타입
                } else {
                    if (spinnerName.value == spinnerItemTitle) {
                        return@Function repository.searchTitle("%$query%")//양옆의 %는 query가 포함되어 있는가? 의미이다
                    }else{
                        return@Function repository.searchTitleOrBody("%$query%")
                    }
                }
            }
        )
    }
    fun getList(): LiveData<List<ItemEntity>>{
        return repository.getList()
    }

    fun insert(itemEntity: ItemEntity) {
        repository.insert(itemEntity)
    }

    fun delete(itemEntity: ItemEntity) {
        repository.delete(itemEntity)
    }

    fun getCurrentGPS(activity: Activity?): LiveData<DataTrans.gps>{
        return DataTrans.requestLastLocation(activity!!)
    }

    fun signIn(account: GoogleSignInAccount, activity: Activity?, callback: () -> Unit){
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

    fun deleteReference(){

    }


}