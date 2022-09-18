package com.project.sharingrestaurants.util

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.project.sharingrestaurants.data.OffDetailItem
import com.project.sharingrestaurants.room.ItemEntity
import com.project.sharingrestaurants.ui.off.OffItemAddActivity.Companion.DELIMITER
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object DataTrans {

    fun itemTrans(items: ItemEntity): OffDetailItem{
        val bodyArr = items.body.split(DELIMITER) as MutableList//없으면 인덱스2개
        val imageArr = items.imageURL.split(DELIMITER) as MutableList//없으면 인덱스1개
        bodyArr.removeAt(bodyArr.lastIndex)
        if (imageArr.size > 1) {//사이즈가 1개 이하일때 제거하면 에러 남
            imageArr.removeAt(imageArr.lastIndex)
        }
        Log.d("data사이즈image",imageArr.size.toString())
        Log.d("data사이즈body",bodyArr.size.toString())
        Log.d("data사이즈image값",imageArr.get(0).toString())
        return OffDetailItem(items.id!!, items.title, items.locate, items.place, items.priority, bodyArr, imageArr)
    }//body 최소 사이즈 1 //image 최소 사이즈 1

    fun getTime(): String {
        val mFormat = SimpleDateFormat("yyyyMMddhhmmss")
        var mNow = System.currentTimeMillis()
        var mDate = Date(mNow)
        return mFormat.format(mDate)
    }

    fun requestLastLocation(activity: Activity): LiveData<gps> {
        val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
        val liveData: MutableLiveData<gps> = MutableLiveData()
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        }
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location ->
                liveData.postValue(gps(location.latitude, location.longitude))
            }
        return liveData
    }

    fun calDist(lat1:Double, lon1:Double, lat2:Double, lon2:Double) : Long{
        val EARTH_R = 6371000.0
        val rad = Math.PI / 180
        val radLat1 = rad * lat1
        val radLat2 = rad * lat2
        val radDist = rad * (lon1 - lon2)

        var distance = Math.sin(radLat1) * Math.sin(radLat2)
        distance = distance + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(radDist)
        val ret = EARTH_R * Math.acos(distance)
        Log.d("거리",Math.round(ret).toString())
        return Math.round(ret)/1000 // 킬로미터로 변환
    }

    data class gps(
        val latitude: Double,
        val longitude: Double
    )
}