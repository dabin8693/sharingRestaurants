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

class OnAddViewModel(private val repository: ItemRepository): ViewModel() {

    val documentId: MutableLiveData<String> = MutableLiveData()
    val itemTitle: MutableLiveData<String> = MutableLiveData()
    val itemPriority: MutableLiveData<Float> = MutableLiveData()
    val itemLocate: MutableLiveData<String> = MutableLiveData()
    val itemPlace: MutableLiveData<String> = MutableLiveData()

    val mapDrawable: MutableLiveData<Drawable> = MutableLiveData()
    val imageList: ArrayList<String> = ArrayList()
    val textList: ArrayList<String> = ArrayList()

    val recommends: Int = 0//글 추가면 0 글 수정이면 기존 추천갯수

    lateinit var publicUri: Uri//공용저장소 사진uri(카메라 인텐트 전에 공용저장소에 미리 파일 생성할때 사용)
    lateinit var publicName: String//공용저장소 파일이름(카메라 인텐트 전에 공용저장소에 미리 파일 생성할때 사용)
    //위 변수들 데이터바인딩
    var itemLatitude: Double = 0.0
    var itemLongitude: Double = 0.0
    //private val itemBodys: StringBuilder = StringBuilder()//(구분자 포함된 본문내용)(db저장 형식)
    //private val itemBodys: ArrayList<String> = ArrayList()
    private val itemImages: StringBuilder = StringBuilder()//(구분자 포함된 앱내 이미지 절대주소)(db저장 형식)

    //private val uploadImagePath: StringBuilder = StringBuilder()
    private val uploadImagePath: ArrayList<String> = ArrayList()
    private var uploadThumImagePath: String = ""
    val uploadSuccess: MutableLiveData<Boolean> = MutableLiveData()

    init {
        textList.add("")
    }

    fun setItemImage(uri: String){
        //itemImages.append(uri + DELIMITER)
    }

    fun setItemBody(text: String){
        //itemBodys.append(text + DELIMITER)
        //itemBodys.add(text)
    }

    fun addItem(activity: FragmentActivity, contentResolver: ContentResolver):LiveData<Boolean> {
        val isSuccess: MutableLiveData<Boolean> = MutableLiveData()
        //val imageArr = itemImages.split(DELIMITER) as MutableList//size가 1이면 [0] = ""이다
        if (imageList.size > 1) {//사이즈가 1개 이하일때 제거하면 에러 남
            imageList.removeAt(imageList.lastIndex)
        }
        if (imageList[0] == ""){//이미지 없을때
            dbSave(imageList).observe(activity){ bool ->
                if (bool == true){
                    isSuccess.value = true
                }
            }
        }else{//있으면 파이어스토리지 -> 파이어스토어
            imageSavedPath(imageList, repository.getAuth().currentUser!!.uid, contentResolver).observe(activity){ bool ->
                if (bool){
                    dbSave(uploadImagePath).observe(activity){ bool ->
                        if (bool == true){
                            isSuccess.value = true
                        }else{
                            isSuccess.value = false
                        }
                    }
                }else{
                    isSuccess.postValue(false)
                }
            }
        }
        return isSuccess
    }

    private fun imageSavedPath(imageArr: List<String>, uid: String, contentResolver: ContentResolver): LiveData<Boolean>{
        val liveData: MutableLiveData<Boolean> = MutableLiveData()
        val time: String = getTime()
        val listSize: Int = imageArr.size
        val successList: ArrayList<Boolean> = ArrayList()

        for (image in imageArr){
            if (imageArr.indexOf(image) == 0){
                val imageName = "0" + imageArr.indexOf(image)
                val pathAbs = repository.getFBStorageRef().child(uid).child(time).child(imageName)
                //uploadImagePath.append(pathAbs.path + DELIMITER)
                uploadImagePath.add(pathAbs.path)
                var data = bitmapUpload(image.toUri(), contentResolver)
                Log.d("세이브 코루틴 안안",imageName)
                addImageFBStorage(uid, time, imageName, data, liveData, listSize, successList)
                Log.d("세이브 코루틴 밖밖",imageName)
                val thumbName = "thumbnail"
                val thumbPathAbs = repository.getFBStorageRef().child(uid).child(time).child(thumbName)
                //uploadThumImagePath = thumbPathAbs.path
                uploadThumImagePath = thumbPathAbs.path
                data = thumBitmapUpload(image.toUri(), contentResolver)
                Log.d("세이브 코루틴 안안",imageName)
                addImageFBStorage(uid, time, thumbName, data, liveData, listSize, successList)
                Log.d("세이브 코루틴 밖밖",imageName)
            }else if(imageArr.indexOf(image) < 10){
                val imageName = "0" + imageArr.indexOf(image)
                val pathAbs = repository.getFBStorageRef().child(uid).child(time).child(imageName)
                //uploadImagePath.append(pathAbs.path + DELIMITER)
                uploadImagePath.add(pathAbs.path)
                val data = bitmapUpload(image.toUri(), contentResolver)
                Log.d("세이브 코루틴 안안",imageName)
                addImageFBStorage(uid, time, imageName, data, liveData, listSize, successList)
                Log.d("세이브 코루틴 밖밖",imageName)
            }else if(imageArr.indexOf(image) < 100){
                val imageName = imageArr.indexOf(image).toString()
                val pathAbs = repository.getFBStorageRef().child(uid).child(time).child(imageName)
                //uploadImagePath.append(pathAbs.path + DELIMITER)
                uploadImagePath.add(pathAbs.path)
                val data = bitmapUpload(image.toUri(), contentResolver)
                Log.d("세이브 코루틴 안안",imageName)
                addImageFBStorage(uid, time, imageName, data, liveData, listSize, successList)
                Log.d("세이브 코루틴 밖밖",imageName)
            }

        }
        return liveData
    }

    private fun bitmapUpload(uri: Uri, contentResolver: ContentResolver): ByteArray{
        val bitmap: Bitmap
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val source = ImageDecoder.createSource(contentResolver, uri!!)
            bitmap = ImageDecoder.decodeBitmap(source)
        }else{
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri!!)
        }
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return baos.toByteArray()
    }

    private fun thumBitmapUpload(uri: Uri, contentResolver: ContentResolver): ByteArray{
        var bitmap: Bitmap
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val source = ImageDecoder.createSource(contentResolver, uri!!)
            bitmap = ImageDecoder.decodeBitmap(source)
        }else{
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri!!)
        }
        bitmap = resizeBitmap(bitmap, 4)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return baos.toByteArray()
    }

    private fun addImageFBStorage(uid: String, time: String, name: String, data: ByteArray, liveData: MutableLiveData<Boolean>, listSize: Int, successList: ArrayList<Boolean>){
        CoroutineScope(Dispatchers.IO).launch {
            val value = repository.addImageFBStorage(uid, time, name, data)
            Log.d("세이브 코루틴 안",name)
            syncSave(value, liveData, listSize, successList)
        }
        Log.d("세이브 코루틴 밖",name)
    }

    @Synchronized private fun syncSave(path: String, liveData: MutableLiveData<Boolean>, listSize: Int, successList: ArrayList<Boolean>){
        Log.d("세이브 싱크",path)
        if (path == "FALSE"){
            uploadSuccess.postValue(false)
            successList.add(false)
        }else{
            uploadSuccess.postValue(true)
            successList.add(true)
        }
        if (successList.size == listSize){//모든 사진처리가 끝났을때(실패나 성공)
            var count: Int = 0
            for (bool in successList){
                if (bool == false){
                    count++
                }
            }
            if (count == 0){//실패가 한나도 없으면
                liveData.postValue(true)
            }else {
                liveData.postValue(false)
            }
        }
    }

    private fun dbSave(imageUri: ArrayList<String>): LiveData<Boolean>{
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
                "recommends" to recommends,
                "latitude" to itemLatitude,
                "longitude" to itemLongitude
            )
        )
    }
}