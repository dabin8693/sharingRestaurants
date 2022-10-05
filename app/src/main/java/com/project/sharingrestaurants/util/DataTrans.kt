package com.project.sharingrestaurants.util

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.project.sharingrestaurants.data.OffItem
import com.project.sharingrestaurants.room.ItemEntity
import com.project.sharingrestaurants.util.ConstValue.DELIMITER
import java.lang.Math.asin
import java.lang.Math.sqrt
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

object DataTrans {

    fun itemTrans(items: ItemEntity): OffItem{
        val bodyArr = items.body.split(DELIMITER) as MutableList//없으면 인덱스2개
        val imageArr = items.imageURL.split(DELIMITER) as MutableList//없으면 인덱스1개
        bodyArr.removeAt(bodyArr.lastIndex)
        if (imageArr.size > 1) {//사이즈가 1개 이하일때 제거하면 에러 남
            imageArr.removeAt(imageArr.lastIndex)
        }
        Log.d("data사이즈image",imageArr.size.toString())
        Log.d("data사이즈body",bodyArr.size.toString())
        Log.d("data사이즈image값",imageArr.get(0).toString())
        return OffItem(items.id!!, items.title, items.locate, items.place, items.priority, bodyArr, imageArr)
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

    fun calDist(lat1:Double, lon1:Double, lat2:Double, lon2:Double) : String{
        val R = 6372.8 * 1000
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2).pow(2.0) + Math.sin(dLon / 2).pow(2.0) * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
        val c = 2 * asin(sqrt(a))
        val meter = (R * c).toInt()
        val first = meter/1000
        val second = meter%1000
        val kilometer = first.toString() + "." + second.toString() + "km"
        return kilometer
    }

    data class gps(
        val latitude: Double,
        val longitude: Double
    )
}