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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.project.sharingrestaurants.ui.off.OffItemAddActivity
import com.project.sharingrestaurants.util.ConstValue.DELIMITER
import com.project.sharingrestaurants.util.ConstValue.FALSE
import com.project.sharingrestaurants.util.DataTrans
import com.project.sharingrestaurants.util.DataTrans.getTime
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.lang.StringBuilder

class FBStorage {
    private val storage = Firebase.storage("gs://restaurantapp-3152b.appspot.com")
    private val storageRef = storage.reference

    companion object {
        private var INSTANCE: FBStorage? = null

        fun getInstance(): FBStorage {//스레드 경합없음으로 synchronized필요없음
            if (INSTANCE == null) {//중복 생성 방지
                INSTANCE = FBStorage()
            }
            return INSTANCE ?: FBStorage()//null이면  재생성
        }
    }

    fun getFBStorageRef(): StorageReference = this.storageRef



    suspend fun addImage(uid: String, time: String, name: String, data: ByteArray): String {
        Log.d("세이브 add 안안", name)
        val path: StorageReference = storageRef.child(uid).child(time).child(name)
        val task = path.putBytes(data)
        try {
            Log.d("세이브 add 안", name)
            task.await()
            Log.d("세이브 add 밖", name)
            return path.path
        } catch (e: Exception) {
            Log.d("세이브 add 밖", name)
            return "FALSE"
        }
        Log.d("세이브 add 밖밖", name)
    }
}