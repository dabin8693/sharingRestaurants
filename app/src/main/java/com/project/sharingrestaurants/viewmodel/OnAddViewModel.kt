package com.project.sharingrestaurants.viewmodel

import android.app.Activity
import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.data.OffDetailItem
import com.project.sharingrestaurants.room.ItemEntity
import com.project.sharingrestaurants.room.ItemRepository
import com.project.sharingrestaurants.ui.off.OffItemAddActivity
import com.project.sharingrestaurants.util.ConstValue.DELIMITER
import com.project.sharingrestaurants.util.ConstValue.FALSE
import retrofit2.http.Body
import java.lang.StringBuilder

class OnAddViewModel(private val repository: ItemRepository): ViewModel() {

    val documentId: MutableLiveData<String> = MutableLiveData()
    val itemTitle: MutableLiveData<String> = MutableLiveData()
    val itemPriority: MutableLiveData<Float> = MutableLiveData()
    val itemLocate: MutableLiveData<String> = MutableLiveData()
    val itemPlace: MutableLiveData<String> = MutableLiveData()
    val imageList: ArrayList<String> = ArrayList()
    val textList: ArrayList<String> = ArrayList()

    val recommends: Int = 0//글 추가면 0 글 수정이면 기존 추천갯수

    lateinit var publicUri: Uri//공용저장소 사진uri(카메라 인텐트 전에 공용저장소에 미리 파일 생성할때 사용)
    lateinit var publicName: String//공용저장소 파일이름(카메라 인텐트 전에 공용저장소에 미리 파일 생성할때 사용)
    //위 변수들 데이터바인딩
    var itemLatitude: Double = 0.0
    var itemLongitude: Double = 0.0
    private val itemBodys: StringBuilder = StringBuilder()//(구분자 포함된 본문내용)(db저장 형식)
    private val itemImages: StringBuilder = StringBuilder()//(구분자 포함된 앱내 이미지 절대주소)(db저장 형식)

    init {
        textList.add("")
    }
    fun setItemImage(uri: String){
        itemImages.append(uri + DELIMITER)
    }

    fun setItemBody(text: String){
        itemBodys.append(text + DELIMITER)
    }

    fun addItem(activity: FragmentActivity, contentResolver: ContentResolver):LiveData<Boolean> {
        val isSuccess: MutableLiveData<Boolean> = MutableLiveData()
        val imageArr = itemImages.split(DELIMITER) as MutableList//size가 1이면 [0] = ""이다
        if (imageArr.size > 1) {//사이즈가 1개 이하일때 제거하면 에러 남
            imageArr.removeAt(imageArr.lastIndex)
        }
        if (imageArr[0] == ""){//이미지 없을때
            dbSave(imageArr[0]).observe(activity){ bool ->
                if (bool == true){
                    isSuccess.value = true
                }
            }
        }else{//있으면 파이어스토리지 -> 파이어스토어
            storageSave(imageArr, contentResolver).observe(activity){ storageUri ->
                if (storageUri.equals(FALSE)){//파이어스토리지 저장실패
                    isSuccess.postValue(false)
                }else{
                    dbSave(storageUri).observe(activity){ bool ->
                        if (bool == true){
                            isSuccess.value = true
                        }else{
                            isSuccess.value = false
                        }
                    }
                }
            }
        }
        return isSuccess
    }

    private fun storageSave(imageArr: List<String>, contentResolver: ContentResolver): LiveData<String>{
        return repository.addFBImage(imageArr, contentResolver)
    }
    private fun dbSave(imageUri: String): LiveData<Boolean>{
        return repository.addFBBoard(
            hashMapOf(
                "documentId" to "",
                "timestamp" to FieldValue.serverTimestamp(),
                "userID" to repository.getAuth().currentUser!!.uid,
                "tilte" to itemTitle.value!!,
                "place" to itemPlace.value!!,
                "locate" to itemLocate.value!!,
                "priority" to (itemPriority.value ?: 0F),
                "body" to itemBodys.toString(),
                "image" to imageUri,
                "recommends" to recommends,
                "latitude" to itemLatitude,
                "longitude" to itemLongitude
            )
        )
    }
}