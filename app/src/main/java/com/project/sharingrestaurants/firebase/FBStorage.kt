package com.project.sharingrestaurants.firebase

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.project.sharingrestaurants.ui.off.OffItemAddActivity
import com.project.sharingrestaurants.util.ConstValue.DELIMITER
import com.project.sharingrestaurants.util.ConstValue.FALSE
import com.project.sharingrestaurants.util.DataTrans
import com.project.sharingrestaurants.util.DataTrans.getTime
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.lang.StringBuilder

class FBStorage {//싱글톤으로 비즈니스 로직 다 뷰모델로
    private val storage = Firebase.storage("gs://restaurantapp-3152b.appspot.com")
    private val storageRef = storage.reference
    private var tempPath: StringBuilder ?= null
    private var completeNumber: ArrayList<Boolean> ?= null
    private var liveData: MutableLiveData<String> ?= null

    fun initStart(){
        liveData = MutableLiveData()
        tempPath = StringBuilder()
        completeNumber = ArrayList()
    }

    fun addImageSaved(imageArr: List<String>, uid: String, contentResolver: ContentResolver): LiveData<String>{
        Log.d("이미지 저장","ㅁㄴㅇ")
        initStart()
        val time: String = getTime()
        //tempPath!!.append(storageRef.path+time)
        for (image in imageArr) {
            var path: String = ""
            var storegeRefChild: StorageReference = storageRef
            if (imageArr.indexOf(image) < 10) {
                path = uid + "/" + "0" + imageArr.indexOf(image)
                storegeRefChild = storageRef.child(time).child(path)
            }else if (imageArr.indexOf(image) < 100){
                path = uid + "/" + imageArr.indexOf(image)
                storegeRefChild = storageRef.child(time).child(path)
            }
            tempPath!!.append(storageRef.path+time + path + DELIMITER)
            bitmapUpload(image.toUri(), contentResolver, path, storegeRefChild, imageArr.size)
        }

        return liveData as LiveData<String>
    }

    private fun bitmapUpload(uri: Uri, contentResolver: ContentResolver, path: String, storegeRefChild: StorageReference, imagaeArrSize: Int){
        Log.d("비트맵 업로드","ㅁㄴㅇ")
        Log.d("비트맵 uri:",uri.toString())
        Log.d("비트맵 패스",path)

        val bitmap: Bitmap
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val source = ImageDecoder.createSource(contentResolver, uri!!)
            bitmap = ImageDecoder.decodeBitmap(source)
        }else{
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri!!)
        }
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = storegeRefChild.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            //liveData.postValue(path)
            Log.d("파이어스토어 업로드 성공","ㅁㄴㅇ")
            //completeNumber.add(true)
            synchronizedMethod(completeNumber!!, true)
            Log.d("컴플리트사이즈ㅁㄴㅇ",completeNumber!!.size.toString())
            if (completeNumber!!.size == imagaeArrSize){
                upLoadCompleteCallback()
            }
        }.addOnFailureListener{
            Log.d("파이어스토어 업로드 실패","ㅁㄴㅇ")
            //completeNumber.add(false)
            synchronizedMethod(completeNumber!!, false)
            if (completeNumber!!.size == imagaeArrSize){
                upLoadCompleteCallback()
            }
        }

    }

    @Synchronized private fun synchronizedMethod(completeNumber: ArrayList<Boolean>, bool: Boolean) = completeNumber.add(bool)

    private fun upLoadCompleteCallback(){
        Log.d("업로드 콜백","ㅁㄴㅇ")
        for (bool in completeNumber!!){
            if (bool == false){
                (liveData as MutableLiveData).postValue(FALSE)
                Log.d("업로드 콜백 실패","ㅁㄴㅇ")
                valClear()
                return
            }
        }
        Log.d("업로드 주소들ㅁㄴㅇ",tempPath!!.toString())
        (liveData as MutableLiveData).postValue(tempPath!!.toString())
        valClear()
    }

    fun valClear(){
        completeNumber!!.clear()
        liveData = null
        completeNumber = null
        tempPath = null
    }
}