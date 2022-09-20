package com.project.sharingrestaurants.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.data.OffDetailItem
import com.project.sharingrestaurants.room.ItemEntity
import com.project.sharingrestaurants.room.ItemRepository
import com.project.sharingrestaurants.ui.off.OffItemAddActivity
import com.project.sharingrestaurants.ui.off.OffItemAddActivity.Companion.DELIMITER
import retrofit2.http.Body
import java.lang.StringBuilder

class OnAddViewModel(private val repository: ItemRepository): ViewModel() {

    val itemId: MutableLiveData<Long> = MutableLiveData()//프라이머 키
    val itemTitle: MutableLiveData<String> = MutableLiveData()
    val itemPriority: MutableLiveData<Float> = MutableLiveData()
    val itemLocate: MutableLiveData<String> = MutableLiveData()
    val itemPlace: MutableLiveData<String> = MutableLiveData()
    val imageList: ArrayList<String> = ArrayList()
    val textList: ArrayList<String> = ArrayList()
    //위 변수들 데이터바인딩
    var itemLatitude: Double = 0.0
    var itemLongitude: Double = 0.0
    private val itemBodys: StringBuilder = StringBuilder()//(구분자 포함된 본문내용)(db저장 형식)
    private val itemImages: StringBuilder = StringBuilder()//(구분자 포함된 앱내 이미지 절대주소)(db저장 형식)



    fun addItem() {
        val newItem = ItemEntity(
            id = itemId.value,  // 새로운 Item room에서 자동 값 적용
            title = itemTitle.value?: "",
            locate = itemLocate.value?: "",
            place = itemPlace.value?: "",
            priority = itemPriority.value?: 0F,
            body = itemBodys.toString()?: "",
            imageURL = itemImages.toString() ?: "",
            latitude = itemLatitude?: 0.0,
            longitude = itemLongitude?: 0.0
        )

        //insert(newItem)
    }
}