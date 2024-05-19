package com.example.db_wifi

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.example.db_wifi.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var naverMapInfo: List<NaverMapData>? = null
    private var naverMapList: NaverMapItem? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationSource: FusedLocationSource

    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 1000

    private lateinit var naverMap: NaverMap
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var markerInfoText: TextView
    private lateinit var findRouteButton: Button

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                requestLocationOnce()
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }

    private var currentLocation: LatLng? = null
    private var currentLocationMarker: Marker? = null
    private var selectedMarker: Marker? = null

    private val targetLocations = listOf(
        TargetLocation("전주시청", LatLng(35.824223, 127.147953)),
        TargetLocation("전북도청", LatLng(35.821171, 127.124111))
    )

    data class TargetLocation(val name: String, val latLng: LatLng)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()

        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient("t0031hl9ab")

        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }
        mapFragment.getMapAsync(this)

        drawerLayout = findViewById(R.id.main)
        markerInfoText = findViewById(R.id.marker_info_text)
        findRouteButton = findViewById(R.id.find_route_button)

        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        findRouteButton.setOnClickListener {
            openNaverMapAppForDirections()
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                requestLocationOnce()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationOnce() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    currentLocation = LatLng(latitude, longitude)

                    val cameraUpdate = CameraUpdate.scrollTo(currentLocation!!)
                    naverMap.moveCamera(cameraUpdate)

                    if (currentLocationMarker == null) {
                        currentLocationMarker = Marker().apply {
                            position = currentLocation as LatLng
                            map = naverMap
                            icon = MarkerIcons.BLACK
                            iconTintColor = Color.BLUE
                            width = Marker.SIZE_AUTO
                            height = Marker.SIZE_AUTO
                            setOnClickListener {
                                selectedMarker = this
                                Log.v("마커설정", "현재 위치 마커가 선택되었습니다: $selectedMarker")
                                Toast.makeText(
                                    this@MainActivity,
                                    "현재 위치 마커가 선택되었습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                true
                            }
                        }
                    } else {
                        currentLocationMarker?.position = currentLocation as LatLng
                    }

                    Toast.makeText(
                        this,
                        "현재 위치: 위도 $latitude, 경도 $longitude",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.v("위치설정", "현재 위치 설정 완료: $currentLocation")
                } else {
                    Log.e("Location", "Location is null")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "위치 정보를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("Location", "Failed to get location: ${e.message}")
            }
    }

    private fun openDrawerWithMarkerInfo(markerInfo: String) {
        markerInfoText.text = markerInfo
        drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun openNaverMapAppForDirections() {
        val currentLocationSafe = currentLocation
        val selectedMarkerSafe = selectedMarker

        if (currentLocationSafe != null && selectedMarkerSafe != null) {
            val uri = Uri.parse("nmap://route/walk?slat=${currentLocationSafe.latitude}&slng=${currentLocationSafe.longitude}&dlat=${selectedMarkerSafe.position.latitude}&dlng=${selectedMarkerSafe.position.longitude}&appname=com.example.db_wifi")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.setPackage("com.nhn.android.nmap")  // 네이버 지도 앱의 패키지 이름 지정

            val packageManager = packageManager
            val activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            val isIntentSafe = activities.isNotEmpty()

            if (isIntentSafe) {
                startActivity(intent)
            } else {
                // 네이버 지도 앱이 설치되어 있지 않으면 Play Store로 유도
                val playStoreUri = Uri.parse("market://details?id=com.nhn.android.nmap")
                val playStoreIntent = Intent(Intent.ACTION_VIEW, playStoreUri)
                playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(playStoreIntent)
                Toast.makeText(this, "네이버 지도 앱이 설치되어 있지 않습니다. 설치를 위해 Play Store로 이동합니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "현재 위치 또는 마커가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show()
            if (currentLocationSafe == null) {
                Log.v("경로찾기", "현재 위치가 설정되지 않았습니다.")
            }
            if (selectedMarkerSafe == null) {
                Log.v("경로찾기", "선택된 마커가 없습니다.")
            }
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        val naverMapApiInterface = NaverMapRequest.getClient().create(NaverMapApiInterface::class.java)
        val query = "wifi"
        val coordinate = "127.1054328,37.3595963"
        val filter = "radius"

        val call: Call<NaverMapItem> = naverMapApiInterface.getMapData(query, coordinate, filter)

        Log.v("Debug중", "API 요청을 시작합니다.")

        call.enqueue(object : Callback<NaverMapItem> {
            override fun onResponse(call: Call<NaverMapItem>, response: Response<NaverMapItem>) {
                if (response.isSuccessful) {
                    Log.v("디버깅중", "성공!!!!!")
                    naverMapList = response.body()
                    naverMapInfo = naverMapList?.jjwifi

                    naverMapInfo?.let { infoList ->
                        infoList.forEach { item ->
                            val latitude = item.latitude
                            val longitude = item.longitude

                            val marker = Marker()
                            marker.position = LatLng(latitude, longitude)
                            marker.map = naverMap
                            marker.icon = MarkerIcons.BLACK
                            marker.iconTintColor = Color.RED
                            marker.width = Marker.SIZE_AUTO
                            marker.height = Marker.SIZE_AUTO
                            marker.setOnClickListener { overlay ->
                                val info = "위치: ${item.latitude}, ${item.longitude}\n주소: ${item.address}"
                                openDrawerWithMarkerInfo(info)

                                selectedMarker = marker
                                Log.v("마커설정", "마커가 선택되었습니다: $selectedMarker")
                                true
                            }
                        }
                    }

                    Toast.makeText(
                        this@MainActivity,
                        naverMapInfo?.get(1)?.address,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Log.v("디버깅중", "응답이 성공적이지 않음, 코드: ${response.code()}, 메시지: ${response.message()}")
                    Log.v("디버깅중", "응답 내용: ${response.errorBody()?.string()}")
                    Toast.makeText(this@MainActivity, "데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<NaverMapItem>, t: Throwable) {
                Log.v("디버깅중", "실패!!!!! ${t.message}")
                Toast.makeText(this@MainActivity, "데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })

        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        naverMap.uiSettings.isLocationButtonEnabled = true

        // 목표 위치 마커 설정
        targetLocations.forEach { target ->
            val marker = Marker()
            marker.position = target.latLng
            marker.map = naverMap
            marker.icon = MarkerIcons.BLACK
            marker.iconTintColor = Color.RED
            marker.width = Marker.SIZE_AUTO
            marker.height = Marker.SIZE_AUTO
            marker.setOnClickListener {
                val positionString = "위치: ${target.latLng.latitude}, ${target.latLng.longitude}\n목표: ${target.name}"
                openDrawerWithMarkerInfo(positionString)
                true
            }
        }

        // 테스트를 위한 기본 마커 설정
        val marker = Marker()
        marker.position = LatLng(37.5670135, 126.9783740)
        marker.map = naverMap
        marker.icon = MarkerIcons.BLACK
        marker.iconTintColor = Color.RED
        marker.width = Marker.SIZE_AUTO
        marker.height = Marker.SIZE_AUTO
        marker.setOnClickListener {
            val cameraPosition = naverMap.cameraPosition
            val positionString = "위도: ${cameraPosition.target.latitude}, 경도: ${cameraPosition.target.longitude}"
            openDrawerWithMarkerInfo(positionString)
            true
        }
    }
}
