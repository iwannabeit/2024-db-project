package com.example.db_wifi

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.health.connect.datatypes.units.Length
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.requestLocationUpdates
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var naverMapInfo: List<NaverMapData>? = null
    private var naverMapList: NaverMapItem? = null
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons
import android.Manifest
import android.location.Geocoder
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.util.FusedLocationSource
import okhttp3.Address
import okio.IOException
import java.util.Locale


class MainActivity : FragmentActivity(), OnMapReadyCallback {

    // FusedLocationProviderClient는 manifest에서 위치권한 얻은 후 사용할 수 있습니다!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationSource: FusedLocationSource

    //위치 권한 요청을 위한 코드
    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 1000

    private lateinit var naverMap: NaverMap
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var markerInfoText: TextView

    // 위치 권한 요청
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // 권한이 허용되었을 때 위치 정보 요청
                requestLocationUpdates()
            } else {
                // 권한이 거부되었을 때 메시지 표시
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 위치 권한 확인
        checkLocationPermission()

        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient("f5wddcflyd")

        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }
        mapFragment.getMapAsync(this)

        drawerLayout = findViewById(R.id.main)
        markerInfoText = findViewById(R.id.marker_info_text)

        //
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)


        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // MapFragment를 통해 지도 초기화
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().add(R.id.map_fragment, it).commit()
            }
        mapFragment.getMapAsync(this)

    }
        override fun onMapReady(naverMap: NaverMap) {

            //클라이언트 객체 생성
            val naverMapApiInterface = NaverMapRequest.getClient().create(NaverMapApiInterface::class.java)

            //응답 받을 콜백 구현
//            val call = naverMapApiInterface.getNaverMapData()
            val call: Call<NaverMapItem> = naverMapApiInterface.getMapData()

            Log.v("Debug중","디버그중입니다.")

            //클라이언트 객체가 제공하는 enqueue로 통신에 대한 요청, 응답 처리 방법 명시
            call.enqueue(object : Callback<NaverMapItem> {
                override fun onResponse(call: Call<NaverMapItem>, response: Response<NaverMapItem>) {
                    if(response.isSuccessful){
                        Log.v("디버깅중", "성공!!!!!")
                        naverMapList = response.body()
                        naverMapInfo = naverMapList?.jjwifi

                        Toast.makeText(this@MainActivity, naverMapInfo?.get(1)?.address, Toast.LENGTH_LONG).show()

                    }

                }
                override fun onFailure(call: Call<NaverMapItem>, t: Throwable) {
                    // 통신 실패 시 처리할 코드
                    Log.v("디버깅중", "실패!!!!!")
                }
            })


            val marker = Marker()
            marker.position = LatLng(37.5670135, 126.9783740)
            marker.map = naverMap

        }
}

    }

    // 위치 권한 확인 및 요청
    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // 권한이 이미 허용되어 있으면 위치 정보 요청
                requestLocationUpdates()
            }
            else -> {
                // 권한이 없을 경우 권한 요청
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        // 현재 위치 요청
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                // 위치가 성공적으로 가져와졌을 때 처리
                val latitude = location?.latitude ?: 0.0
                val longitude = location?.longitude ?: 0.0
                Toast.makeText(
                    this,
                    "현재 위치: 위도 $latitude, 경도 $longitude",
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener { e ->
                // 위치 가져오기 실패시 처리
                Toast.makeText(this, "위치 정보를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("Location", "Failed to get location: ${e.message}")
            }
    }    // 위치 정보 요청


    private fun openDrawerWithMarkerInfo(markerInfo: String) {
        markerInfoText.text = markerInfo // 슬라이딩 드로어에 마커 정보 설정
        drawerLayout.openDrawer(GravityCompat.START) // 슬라이딩 드로어 열기
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap // naverMap 변수 초기화

        naverMap.locationSource = locationSource // 기존에 설정된 위치 소스 초기화
        naverMap.locationTrackingMode = LocationTrackingMode.Follow // 위치 추적 모드를 Follow로 설정
        naverMap.uiSettings.isLocationButtonEnabled = true // 현위치 버튼

        //실내지도 활성화
//        naverMap.isIndoorEnabled = true
//        naverMap.uiSettings.isIndoorLevelPickerEnabled = true // 실내지도 층 버튼

        // 마커를 클릭했을 때의 동작 설정
        val marker = Marker()
        marker.position = LatLng(37.5670135, 126.9783740)
        marker.map = naverMap
        marker.icon = MarkerIcons.BLACK
        marker.iconTintColor = Color.RED
        marker.width = Marker.SIZE_AUTO
        marker.height = Marker.SIZE_AUTO
        marker.setOnClickListener {
            val cameraPosition = naverMap.cameraPosition

            val latitudeString = cameraPosition.target.latitude.toString() // 위도를 문자열로 변환
            val longitudeString = cameraPosition.target.longitude.toString() // 경도를 문자열로 변환
            val positionString : String = "$latitudeString $longitudeString" // 위도와 경도를 합쳐서 위치를 나타내는 문자열 생성

            openDrawerWithMarkerInfo(positionString) // 마커에 대한 정보를 슬라이딩 드로어에 표시
            true
        }


    }

//    companion object {
//        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
//    }
}