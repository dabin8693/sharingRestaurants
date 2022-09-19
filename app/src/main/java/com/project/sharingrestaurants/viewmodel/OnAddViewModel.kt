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
    //val repository : ItemRepository = MyApplication.REPOSITORY//나중에 di사용 Application클래스에서 의존성 관리

    val itemId: MutableLiveData<Long> = MutableLiveData()//프라이머 키
    val itemTitle: MutableLiveData<String> = MutableLiveData()
    val itemPriority: MutableLiveData<Float> = MutableLiveData()
    val itemLocate: MutableLiveData<String> = MutableLiveData()
    val itemPlace: MutableLiveData<String> = MutableLiveData()
    //위 변수들 데이터바인딩
    var itemLatitude: Double = 0.0
    var itemLongitude: Double = 0.0
    private val itemBodys: StringBuilder = StringBuilder()//(구분자 포함된 본문내용)(db저장 형식)
    private val itemImages: StringBuilder = StringBuilder()//(구분자 포함된 앱내 이미지 절대주소)(db저장 형식)

    lateinit var publicUri: Uri//공용저장소 사진uri(카메라 인텐트 전에 공용저장소에 미리 파일 생성할때 사용)
    lateinit var publicName: String//공용저장소 파일이름(카메라 인텐트 전에 공용저장소에 미리 파일 생성할때 사용)
    private val tempBitmapUriList: ArrayList<Any> = ArrayList()//저장한적 없는 이미지 = (bitmap, 사진이름) 저장한적 있는 이미지 = String(내앱저장소uri경로)


    //ViewModel은 view, Lifecycle, 혹은 activity context의 참조를 들고있는 어떤 클래스도 참조하지 않아야 한다!!!화면회전할때 기존 액티비티가 종료되어도 gc에 수거가 안됨
    var viewList: ArrayList<View> ?= null //에디트, 이미지뷰 저장(동적으로 생성한 뷰들)(리스너, 포커스, 데이터 추가할때 필요)
    var nowEditText: EditText ?= null //현재 포커싱 에디트(이미지 추가할때 위치지정에 필요)
    init {
        viewList = ArrayList()
    }


    var nowEditPosition: Int = 0 //초기값
    var childCount: Int = 0 //최초 레이아웃 자식 갯수

    fun referenceClear(){//화면회전으로 인한 액티비티 종료시 참조해제 해야됨
        viewList = null
        nowEditText = null
    }


    fun addImageUri(any: Any){
        tempBitmapUriList.add(any)
    }

    fun addImageBitmap(position: Int, any: Any){
        tempBitmapUriList.add(position, any)
    }

    fun deleteImageBitmap(position: Int){
        tempBitmapUriList.removeAt(position)
    }

    fun getImageList() = tempBitmapUriList

    fun setItemImage(uri: String){
        itemImages.append(uri + DELIMITER)
    }

    fun setItemBody(text: String){
        itemBodys.append(text + DELIMITER)
    }

    fun setItem(offDetailItem: OffDetailItem){
        itemId.value = offDetailItem.id
        itemLocate.value = offDetailItem.locate
        itemPlace.value = offDetailItem.place?: ""
        itemTitle.value = offDetailItem.title?: ""
        itemPriority.value = offDetailItem.priority

        if(offDetailItem.imageURL.size != 0) {
            if (offDetailItem.imageURL.get(0) != "") {
                for (i in offDetailItem.imageURL) {
                    addImageUri(i)//string타입 uri
                }
            }
        }
    }

    fun insert(itemEntity: ItemEntity) {
        repository.insert(itemEntity)
    }

    fun delete(itemEntity: ItemEntity) {
        repository.delete(itemEntity)
    }

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

        insert(newItem)
    }
}