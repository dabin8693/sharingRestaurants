package com.project.sharingrestaurants.firebase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.project.sharingrestaurants.util.ConstValue
import kotlinx.coroutines.tasks.await
import java.lang.Exception

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
        val path: StorageReference = storageRef.child(uid).child(time).child(name)
        val task = path.putBytes(data)
        try {
            task.await()
            return path.path
        } catch (e: Exception) {
            return ConstValue.FALSE
        }
    }

    suspend fun getThumImage(path: String): ByteArray{
        try {
            return storageRef.child(path).getBytes(2048*2048).await()
        }catch (e: Exception){
            Log.d("에러getThumImage",e.toString())
            return ByteArray(1)
        }
    }
}