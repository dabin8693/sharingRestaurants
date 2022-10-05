package com.project.sharingrestaurants.viewmodel

import android.app.Activity
import androidx.arch.core.util.Function
import androidx.lifecycle.*
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.project.sharingrestaurants.firebase.UserEntity
import com.project.sharingrestaurants.room.ItemEntity
import com.project.sharingrestaurants.room.ItemRepository
import com.project.sharingrestaurants.util.DataTrans
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OffLineViewModel(private val repository: ItemRepository) : ViewModel() {

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
        repository.signInGoogle(account, activity!!, callback)//FBLogin()이거 Factory로 받아오게 변경
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


}