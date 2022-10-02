package com.project.sharingrestaurants.viewmodel

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.graphics.drawable.shapes.Shape
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bumptech.glide.load.resource.drawable.DrawableResource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.StorageReference
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.data.OffDetailItem
import com.project.sharingrestaurants.firebase.BoardEntity
import com.project.sharingrestaurants.room.ItemEntity
import com.project.sharingrestaurants.room.ItemRepository
import com.project.sharingrestaurants.ui.off.OffItemAddActivity
import com.project.sharingrestaurants.util.CameraWork
import com.project.sharingrestaurants.util.CameraWork.resizeBitmap
import com.project.sharingrestaurants.util.ConstValue.DELIMITER
import com.project.sharingrestaurants.util.ConstValue.FALSE
import com.project.sharingrestaurants.util.DataTrans.getTime
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class OnAddViewModel(private val repository: ItemRepository) : ViewModel() {

    val itemTitle: MutableLiveData<String> = MutableLiveData()
    val itemPriority: MutableLiveData<Float> = MutableLiveData()
    val itemLocate: MutableLiveData<String> = MutableLiveData()
    val itemPlace: MutableLiveData<String> = MutableLiveData()

    val mapDrawable: MutableLiveData<Drawable> = MutableLiveData()//맵 이동 버튼 background
    val imageList: ArrayList<String> = ArrayList()//마지막 저장할때 담는다 //로컬uri
    val textList: ArrayList<String> = ArrayList()//마지막 저장할때 담는다

    val documentId: MutableLiveData<String> = MutableLiveData()//insert
    var recommends: Int = 0//insert
    lateinit var beforeImageList: List<String>//insert
    var isInserted: Boolean = false//수정창 - true 추가창 - false

    lateinit var publicUri: Uri//공용저장소 사진uri(카메라 인텐트 전에 공용저장소에 미리 파일 생성할때 사용)
    lateinit var publicName: String//공용저장소 파일이름(카메라 인텐트 전에 공용저장소에 미리 파일 생성할때 사용)

    var itemLatitude: Double = 0.0
    var itemLongitude: Double = 0.0

    private val uploadImagePath: ArrayList<String> = ArrayList()//파이어스토리지uri
    private var uploadThumImagePath: String = ""//파이어스토리지uri
    val uploadSuccess: MutableLiveData<Boolean> = MutableLiveData()



    fun setItem(item: BoardEntity) {
        documentId.value = item.documentId
        recommends = item.recommends
        beforeImageList = item.image
        if (!item.locate.equals("")) {
            itemLocate.value = item.locate
        }
        itemTitle.value = item.tilte
        itemPriority.value = item.priority
        itemPlace.value = item.place
        itemLatitude = item.latitude
        itemLongitude = item.longitude
    }

    fun upLoad(activity: FragmentActivity, contentResolver: ContentResolver): LiveData<Boolean> {
        val isSuccess: MutableLiveData<Boolean> = MutableLiveData()
        if (isInserted) {//수정창
            insertItem(activity, contentResolver, isSuccess)
        } else {//추가창
            addItem(activity, contentResolver, isSuccess)
        }
        return isSuccess
    }

    fun addItem(
        activity: FragmentActivity,
        contentResolver: ContentResolver,
        isSuccess: MutableLiveData<Boolean>
    ) {
        if (imageList.size == 0) {//이미지 없을때
            imageList.add("")
        }
        if (imageList[0].equals("")) {//이미지 없을때
            dbSave(imageList).observe(activity) { bool ->
                if (bool == true) {
                    isSuccess.value = true
                }
            }
        } else {//있으면 파이어스토리지 -> 파이어스토어
            imageSavedPath(
                imageList,
                repository.getAuth().currentUser!!.uid,
                contentResolver
            ).observe(activity) { bool ->
                if (bool) {
                    dbSave(uploadImagePath).observe(activity) { bool ->
                        if (bool == true) {
                            isSuccess.value = true
                        } else {
                            isSuccess.value = false
                        }
                    }
                } else {
                    isSuccess.postValue(false)
                }
            }
        }

    }

    fun insertItem(
        activity: FragmentActivity,
        contentResolver: ContentResolver, isSuccess: MutableLiveData<Boolean>
    ) {
        if (imageList.size == 0) {//이미지 없을때
            imageList.add("")
        }
        if (imageList[0].equals("")) {//이미지 없을때
            dbInsert(imageList).observe(activity) { bool ->
                if (bool == true) {
                    isSuccess.value = true
                }
            }
        } else {//있으면 파이어스토리지 -> 파이어스토어
            if (isChanged()) {//사진 변경됨
                imageSavedPath(
                    imageList,
                    repository.getAuth().currentUser!!.uid,
                    contentResolver
                ).observe(activity) { bool ->
                    if (bool) {
                        dbInsert(uploadImagePath).observe(activity) { bool ->
                            if (bool == true) {
                                isSuccess.value = true
                            } else {
                                isSuccess.value = false
                            }
                        }
                    } else {
                        isSuccess.postValue(false)
                    }
                }
            } else {//사진 변경x
                dbInsert(imageList).observe(activity) { bool ->
                    if (bool == true) {
                        isSuccess.value = true
                    }
                }
            }
        }
    }

    fun isChanged(): Boolean {//사진 변경 여부 true = o false = x
        if (imageList.size == beforeImageList.size) {//사진 갯수가 같으면
            var a: Int = 0
            for (index in 0 until imageList.size) {//0~(size-1)
                if (!beforeImageList.get(index).equals(imageList.get(index))) {
                    a++//값이 같지 않으면
                }
            }
            if (a > 0) {
                return true//변경되어있다
            } else {
                return false//변경x
            }

        } else {
            return true//변경되어있다
        }
    }

    fun deleteImage() {//파이어스토리지 이미지 삭제
        //beforeImageList
    }


    private fun imageSavedPath(
        imageArr: List<String>,
        uid: String,
        contentResolver: ContentResolver
    ): LiveData<Boolean> {
        val liveData: MutableLiveData<Boolean> = MutableLiveData()
        val time: String = getTime()
        val successList: ArrayList<Boolean> = ArrayList()

        var num: Int = 0
        for (image in imageArr) {
            if (image.contains(uid)) {//파이어스토리지에 이미 저장된 이미지
                num++
            }
        }
        val imageTotal: Int = imageArr.size - num//파이어스토리지에 저장할 총 이미지 갯수

        for (image in imageArr) {
            if (image.contains(uid)) {//파이어스토리지에 이미 저장된 이미지
                uploadImagePath.add(image)//저장하지 않고 기존 경로 그대로
            } else {
                if (imageArr.indexOf(image) == 0) {
                    val imageName = "0" + imageArr.indexOf(image)
                    val pathAbs =
                        repository.getFBStorageRef().child(uid).child(time).child(imageName)
                    //uploadImagePath.append(pathAbs.path + DELIMITER)
                    uploadImagePath.add(pathAbs.path)
                    var data = bitmapUpload(image.toUri(), contentResolver)
                    Log.d("세이브 코루틴 안안", imageName)
                    addImageFBStorage(uid, time, imageName, data, liveData, imageTotal, successList)
                    Log.d("세이브 코루틴 밖밖", imageName)
                    val thumbName = "thumbnail"
                    val thumbPathAbs =
                        repository.getFBStorageRef().child(uid).child(time).child(thumbName)
                    //uploadThumImagePath = thumbPathAbs.path
                    uploadThumImagePath = thumbPathAbs.path
                    data = thumBitmapUpload(image.toUri(), contentResolver)
                    Log.d("세이브 코루틴 안안", imageName)
                    addImageFBStorage(uid, time, thumbName, data, liveData, imageTotal, successList)
                    Log.d("세이브 코루틴 밖밖", imageName)
                } else if (imageArr.indexOf(image) < 10) {
                    val imageName = "0" + imageArr.indexOf(image)
                    val pathAbs =
                        repository.getFBStorageRef().child(uid).child(time).child(imageName)
                    //uploadImagePath.append(pathAbs.path + DELIMITER)
                    uploadImagePath.add(pathAbs.path)
                    val data = bitmapUpload(image.toUri(), contentResolver)
                    Log.d("세이브 코루틴 안안", imageName)
                    addImageFBStorage(uid, time, imageName, data, liveData, imageTotal, successList)
                    Log.d("세이브 코루틴 밖밖", imageName)
                } else if (imageArr.indexOf(image) < 100) {
                    val imageName = imageArr.indexOf(image).toString()
                    val pathAbs =
                        repository.getFBStorageRef().child(uid).child(time).child(imageName)
                    //uploadImagePath.append(pathAbs.path + DELIMITER)
                    uploadImagePath.add(pathAbs.path)
                    val data = bitmapUpload(image.toUri(), contentResolver)
                    Log.d("세이브 코루틴 안안", imageName)
                    addImageFBStorage(uid, time, imageName, data, liveData, imageTotal, successList)
                    Log.d("세이브 코루틴 밖밖", imageName)
                }
            }
        }
        return liveData
    }

    private fun bitmapUpload(uri: Uri, contentResolver: ContentResolver): ByteArray {
        val bitmap: Bitmap
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val source = ImageDecoder.createSource(contentResolver, uri!!)
            bitmap = ImageDecoder.decodeBitmap(source)
        } else {
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri!!)
        }
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return baos.toByteArray()
    }

    private fun thumBitmapUpload(uri: Uri, contentResolver: ContentResolver): ByteArray {
        var bitmap: Bitmap
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val source = ImageDecoder.createSource(contentResolver, uri!!)
            bitmap = ImageDecoder.decodeBitmap(source)
        } else {
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri!!)
        }
        bitmap = resizeBitmap(bitmap, 4)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return baos.toByteArray()
    }

    private fun addImageFBStorage(
        uid: String,
        time: String,
        name: String,
        data: ByteArray,
        liveData: MutableLiveData<Boolean>,
        imageTotal: Int,
        successList: ArrayList<Boolean>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val value = repository.addImageFBStorage(uid, time, name, data)
            Log.d("세이브 코루틴 안", name)
            syncSave(value, liveData, imageTotal, successList)
        }
        Log.d("세이브 코루틴 밖", name)
    }

    @Synchronized
    private fun syncSave(
        path: String,
        liveData: MutableLiveData<Boolean>,
        imageTotal: Int,
        successList: ArrayList<Boolean>
    ) {
        Log.d("세이브 싱크", path)
        if (path == "FALSE") {
            uploadSuccess.postValue(false)
            successList.add(false)
        } else {
            uploadSuccess.postValue(true)
            successList.add(true)
        }
        if (successList.size == imageTotal) {//모든 사진처리가 끝났을때(실패나 성공)
            var count: Int = 0
            for (bool in successList) {
                if (bool == false) {
                    count++
                }
            }
            if (count == 0) {//실패가 한나도 없으면
                liveData.postValue(true)
            } else {
                liveData.postValue(false)
            }
        }
    }

    private fun dbSave(imageUri: ArrayList<String>): LiveData<Boolean> {
        return repository.addFBBoard(
            hashMapOf(
                "documentId" to "",//데이터베이스 호출부분에서 추가
                "timestamp" to FieldValue.serverTimestamp(),
                "userID" to repository.getAuth().currentUser!!.uid,
                "tilte" to (itemTitle.value ?: ""),
                "place" to (itemPlace.value ?: ""),
                "locate" to (itemLocate.value ?: ""),
                "priority" to (itemPriority.value ?: 0F),
                "body" to textList,
                "image" to imageUri,
                "thumb" to uploadThumImagePath,
                "recommends" to 0,//recommends
                "latitude" to itemLatitude,
                "longitude" to itemLongitude
            )
        )
    }

    private fun dbInsert(imageUri: ArrayList<String>): LiveData<Boolean> {
        return repository.insertFBBoard(
            hashMapOf(
                "documentId" to documentId,//데이터베이스 호출부분에서 추가
                "timestamp" to FieldValue.serverTimestamp(),
                "userID" to repository.getAuth().currentUser!!.uid,
                "tilte" to (itemTitle.value ?: ""),
                "place" to (itemPlace.value ?: ""),
                "locate" to (itemLocate.value ?: ""),
                "priority" to (itemPriority.value ?: 0F),
                "body" to textList,
                "image" to imageUri,
                "thumb" to uploadThumImagePath,
                "recommends" to recommends,//recommends
                "latitude" to itemLatitude,
                "longitude" to itemLongitude
            )
        )
    }
}