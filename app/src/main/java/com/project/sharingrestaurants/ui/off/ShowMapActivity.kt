package com.project.sharingrestaurants.ui.off

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.gun0912.tedpermission.rx3.TedPermission

import com.naver.maps.geometry.LatLng
import com.project.sharingrestaurants.R
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.project.sharingrestaurants.databinding.ActivityShowMapBinding
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

//fab_tracking뷰 현재 위치 장소로 이동 기능 추가
//위치 검색기능 추가
class ShowMapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityShowMapBinding
    private val mapView: MapView by lazy { binding.map }
    private lateinit var fusedLocationClient: FusedLocationProviderClient//구글산 //LOCATION_SERVICE에서 해결하지 못한 GPS와 NETWORK 위치 제공자의 간극을 매워준다
    private lateinit var locationSource: FusedLocationSource//네이버산(내부적으로 위에꺼 씀)
    private lateinit var instanceMap: NaverMapSdk
    private lateinit var naverMap: NaverMap
    private lateinit var marker: Marker
    private lateinit var currentLocation: Location
    private var lastLatitude: Double = 0.0
    private var lastLongitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initStart()

        instanceMap = NaverMapSdk.getInstance(this)
        instanceMap.client = NaverMapSdk.NaverCloudPlatformClient("0t1amhii5n")
        instanceMap.setOnAuthFailedListener { e -> Log.d("맵 에러",e.errorCode.toString()) }

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    private fun initStart() {
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_show_map
        )
        binding.lifecycleOwner = this
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)//마지막으로 알려진 위치 가져옴
        requestPermissions()

        binding.btnConfirm.setOnClickListener{
            Log.d("위도1ㅇㅇㅎ",lastLongitude.toString())
            Log.d("경도1ㅇㅇㅎ",lastLatitude.toString())
            val intent = Intent(this, OffItemAddActivity::class.java)
            intent.putExtra("address", binding.tvLocation.text.toString())
            intent.putExtra("longitude", lastLongitude)
            intent.putExtra("latitude", lastLatitude)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
    // 위치권한 관련 요청
    private fun requestPermissions() {
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
                    Toast.makeText(this,getString(R.string.location_permission_denied_msg),Toast.LENGTH_SHORT).show()
                }
            }) { throwable -> Log.e("AAAAAA", throwable.message.toString()) }


    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: NaverMap) {
        this.naverMap = p0

        // 내장 위치 추적 기능 사용
        //naverMap.locationSource = locationSource

        // 빨간색 표시 마커 (네이버맵 현재 가운데에 항상 위치)
        marker = Marker()
        marker.position = LatLng(
            naverMap.cameraPosition.target.latitude,
            naverMap.cameraPosition.target.longitude
        )
        marker.icon = OverlayImage.fromResource(com.naver.maps.map.R.drawable.navermap_default_marker_icon_green)
        marker.map = naverMap

        // 카메라의 움직임에 대한 이벤트 리스너 인터페이스.
        // 참고 : https://navermaps.github.io/android-map-sdk/reference/com/naver/maps/map/package-summary.html
        naverMap.addOnCameraChangeListener { reason, animated ->
            Log.i("NaverMap", "카메라 변경 - reson: $reason, animated: $animated")
            marker.position = LatLng(
                // 현재 보이는 네이버맵의 정중앙 가운데로 마커 이동
                naverMap.cameraPosition.target.latitude,
                naverMap.cameraPosition.target.longitude
            )
            // 주소 텍스트 세팅 및 확인 버튼 비활성화
            binding.tvLocation.run {
                text = "위치 이동 중"
                setTextColor(Color.parseColor("#c4c4c4"))
            }
            binding.btnConfirm.run {
                setBackgroundResource(R.drawable.rectangleshapegray)
                setTextColor(Color.parseColor("#ffffff"))
                isEnabled = false
            }
        }

        // 카메라의 움직임 종료에 대한 이벤트 리스너 인터페이스.
        naverMap.addOnCameraIdleListener {
            marker.position = LatLng(
                naverMap.cameraPosition.target.latitude,
                naverMap.cameraPosition.target.longitude
            )
            // 좌표 -> 주소 변환 텍스트 세팅, 버튼 활성화
            binding.tvLocation.run {
                text = getAddress(
                    naverMap.cameraPosition.target.latitude,
                    naverMap.cameraPosition.target.longitude
                )
                setTextColor(Color.parseColor("#2d2d2d"))
            }
            lastLatitude = naverMap.cameraPosition.target.latitude
            lastLongitude = naverMap.cameraPosition.target.longitude
            binding.btnConfirm.run {
                setBackgroundResource(R.drawable.rectangleshape)
                setTextColor(Color.parseColor("#FF000000"))
                isEnabled = true
            }
        }

        if (ActivityCompat.checkSelfPermission(//gps 퍼미션 체크
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // 사용자 현재 위치 받아오기
        getCurrentLocation()



        binding.fabTracking.setOnClickListener{
            Log.d("dd","위치1")
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            getCurrentLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(){

        fusedLocationClient.lastLocation//마지막으로 캐싱된 위치정보
            .addOnSuccessListener{ location: Location? ->
                currentLocation = location!!
                // 위치 오버레이의 가시성은 기본적으로 false로 지정되어 있습니다. 가시성을 true로 변경하면 지도에 위치 오버레이가 나타납니다.
                // 파랑색 점, 현재 위치 표시
                naverMap.locationOverlay.run {
                    isVisible = true
                    position = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
                }

                // 카메라 현재위치로 이동
                val cameraUpdate = CameraUpdate.scrollTo(
                    LatLng(
                        currentLocation!!.latitude,
                        currentLocation!!.longitude
                    )
                )
                Log.d("dd","위치2")
                naverMap.moveCamera(cameraUpdate)

                // 빨간색 마커 현재위치로 변경
                marker.position = LatLng(
                    naverMap.cameraPosition.target.latitude,
                    naverMap.cameraPosition.target.longitude
                )
            }
    }

    // 네이버맵 불러오기가 완료되면 콜백


    // 좌표 -> 주소 변환
    private fun getAddress(lat: Double, lng: Double): String {
        val geoCoder = Geocoder(this, Locale.KOREA)
        val address: ArrayList<Address>
        var addressResult = "주소를 가져 올 수 없습니다."
        try {
            //세번째 파라미터는 좌표에 대해 주소를 리턴 받는 갯수로
            //한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위해 최대갯수 설정
            address = geoCoder.getFromLocation(lat, lng, 1) as ArrayList<Address>
            if (address.size > 0) {
                // 주소 받아오기
                val currentLocationAddress = address[0].getAddressLine(0)
                    .toString()
                addressResult = currentLocationAddress

            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addressResult
    }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}