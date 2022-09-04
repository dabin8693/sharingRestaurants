package com.project.sharingrestaurants.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class CameraWork(val context: Context) {//애플리케이션 context

    fun saveToprivate(bitmap: Bitmap, imageFileName: String): String {//글 작성 등록 버튼 눌렀을때
        //var file = File(applicationContext.filesDir, imageFileName)//내장메모리 내 개인앱 저장소에 저장
        var file = context.getDir("Images", Context.MODE_PRIVATE)//images폴더없으면 생성
        file = File(file, imageFileName)//파일 오픈
        //openFileOutput("imageFileName", Context.MODE_PRIVATE).use {  }//내가 원하는 폴더 지정 불가함
        try {
            // Bitmap 파일을 JPEG 형태로 압축해서 출력
            val stream: OutputStream = FileOutputStream(file)//파일출력스트림 열기
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)//압축
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file.absolutePath//room에 경로 저장
    }

    fun resizeBitmap(bitmap: Bitmap): Bitmap {
        var newBitmap = bitmap
        var height = bitmap.height
        var width = bitmap.width
        newBitmap = Bitmap.createScaledBitmap(bitmap, width / 2, height / 2, true)//filter: 픽셀형태 조정
        Log.d("리사이즈1", "ㅇㅇ")
        return newBitmap
    }

    fun saveToMediaStore(CallBack: (String, Uri) -> Unit) {//MediaStore컨텐츠프로바이더 접근해서 공용저장소에 파일 저장하고 컨텐츠uri 가져오기
        val pictureName: String
        val values = ContentValues().apply {
            //viewModel.imageFileNameList.add(imageFileName)
            pictureName = getTime() + ".jpg"
            Log.d("미디어스토어저장1", pictureName)
            put(MediaStore.Images.Media.DISPLAY_NAME, pictureName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/restaurant")
            //put(MediaStore.Images.Media.IS_PENDING, 1)//쓰면 절대 안됨//카메라 앱이 파일에 접근 못 함
        }
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        //val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI//위에꺼랑 똑같음
        val newUri: Uri = context.contentResolver.insert(collection, values)!!
        CallBack(pictureName, newUri)
    }

    fun getTime(): String {
        val mFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        var mNow = System.currentTimeMillis()
        var mDate = Date(mNow)
        return mFormat.format(mDate)
    }

}