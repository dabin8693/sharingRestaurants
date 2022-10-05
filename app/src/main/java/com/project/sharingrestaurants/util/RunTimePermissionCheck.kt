package com.project.sharingrestaurants.util

import android.Manifest
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.gun0912.tedpermission.rx3.TedPermission
import com.project.sharingrestaurants.R

object RunTimePermissionCheck {

    // 위치권한 관련 요청
    fun requestPermissions(context: Context) {
        // 내장 위치 추적 기능 사용
        //locationSource =
        //FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        TedPermission.create()
            .setRationaleTitle("위치권한 요청")
            .setRationaleMessage("현재 위치로 이동하기 위해 위치권한이 필요합니다.") // "we need permission for read contact and find your location"
            .setPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            //rxandroid
            .request()
            .subscribe({ tedPermissionResult ->
                if (!tedPermissionResult.isGranted) {
                    Toast.makeText(context,context.getString(R.string.location_permission_denied_msg), Toast.LENGTH_SHORT).show()
                }
            }) { throwable -> Log.e("AAAAAA", throwable.message.toString()) }


    }
}