package com.project.sharingrestaurants.firebase

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.project.sharingrestaurants.util.DataTrans
import java.io.ByteArrayOutputStream

class FBStorage {
    private val storage = Firebase.storage("gs://restaurantapp-3152b.appspot.com")
    private val storageRef = storage.reference

    fun addImageSaved(imageArr: List<String>, uid: String, contentResolver: ContentResolver): LiveData<String>{
        val liveData: MutableLiveData<String> = MutableLiveData()
        for (image in imageArr) {
            var path: String = ""
            if (imageArr.indexOf(image) < 10) {
                path = uid + "/" + "0" + imageArr.indexOf(image)
                storageRef.child(path)
            }else if (imageArr.indexOf(image) < 100){
                path = uid + "/" + imageArr.indexOf(image)
                storageRef.child(path)
            }
            bitmapUpload(image.toUri(), contentResolver, liveData, path)
        }

        return liveData
    }

    fun bitmapUpload(uri: Uri, contentResolver: ContentResolver, liveData: MutableLiveData<String>, path: String){
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

        var uploadTask = storageRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            liveData.postValue(path)
        }

    }
}