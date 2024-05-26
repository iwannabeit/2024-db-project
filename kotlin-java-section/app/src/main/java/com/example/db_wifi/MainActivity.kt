package com.example.db_wifi

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapOptions
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.clustering.Clusterer
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.overlay.PolygonOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.naver.maps.map.widget.LocationButtonView
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Call as Call1

class MainActivity : FragmentActivity(), OnMapReadyCallback {
    private var naverMapInfo: List<NaverMapData>? = null
    private var naverMapList: NaverMapItem? = null

    // FusedLocationProviderClient는 manifest에서 위치권한 얻은 후 사용할 수 있습니다!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationSource: FusedLocationSource

    //위치 권한 요청을 위한 코드
    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 1000

    private lateinit var naverMap: NaverMap

    private var currentLatLng : LatLng? = null

    private lateinit var markerInfoText: TextView
    private lateinit var search_loadBtn : Button
    private lateinit var scaleBtn : Button
    private lateinit var non_scaleBtn : Button

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private var latitudeString : String? = null
    private var longitudeString : String? = null
    private lateinit var targetPostion : LatLng

    private var currentLocation: LatLng? = null
    private var selectedMarker: Marker? = null

    // 클러스터링 구조
    private var clusterer: Clusterer<ItemKey> = Clusterer.Builder<ItemKey>()
        .minZoom(5) // 클러스터링이 시작되는 최소 줌 레벨
        .maxZoom(13) // 클러스터링이 끝나는 최대 줌 레벨
        .screenDistance(50.0)
        .build()

    // 위치 권한 요청
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // 권한이 허용되었을 때 위치 정보 요청
                //requestLocationUpdates()
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
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        // 위치 권한 확인
        checkLocationPermission()

        // 첫 fetchCurrentLocation() 실행 시, null로 들어가는 오류 때메 미리 실행
        fetchCurrentLocation()

        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient("f5wddcflyd")

        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }
        mapFragment.getMapAsync(this)


        // Bottom Sheet 초기화
        val bottomSheet = findViewById<LinearLayout>(R.id.bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        markerInfoText = findViewById(R.id.marker_info_text)
        search_loadBtn = findViewById(R.id.search_loadBtn)
        scaleBtn = findViewById(R.id.scaleBtn)
        non_scaleBtn = findViewById(R.id.non_scaleBtn)



        enableEdgeToEdge()
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
    }


    // 현재위치의 LatLng값을 얻어오는 함수입니다. 필요시 사용하세요! (currentLatLng이 이름으로 변수 선언 후 사용하시면 됩니다)
    private fun fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    // 위치 정보를 가져오면 LatLng 객체로 변환
                    currentLatLng = LatLng(it.latitude, it.longitude)
                    // 여기서 currentLatLng을 사용하여 작업을 수행할 수 있습니다.

                } ?: run {
                    Toast.makeText(this, "위치 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "위치 정보를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
    }
    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // 권한이 이미 허용되어 있으면 위치 정보 요청
                //requestLocationUpdates()
            }
            else -> {
                // 권한이 없을 경우 권한 요청
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // 현재 위치의 위도와 경도를 가져온 후 toast로 출력하는 함수
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
        markerInfoText.text = markerInfo // Bottom Sheet 영역에 마커 정보 설정
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
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


    private lateinit var circle : CircleOverlay
    private fun drawCircle(center: LatLng) {
        if (::circle.isInitialized) {
            //기존에 있던 범위 제거 후 생성
            circle.map = null

            circle = CircleOverlay().apply {
                this.center = center
                this.radius = 30.0
                this.color = Color.argb(33, 0, 0, 255)
                this.map = naverMap
            }
        } else {
            circle = CircleOverlay().apply {
                this.center = center
                this.radius = 30.0
                this.color = Color.argb(33, 0, 0, 255)
                this.map = naverMap
            }
        }
    }

    // 원을 지도에서 제거하는 함수
    private fun removeCircle() {
        // 원이 null이 아니면 지도에서 제거
        if (::circle.isInitialized) {
            circle.map = null
        } else {
            // 원이 초기화되지 않았다면 로그를 출력
            Log.d("MainActivity", "Circle is not initialized")
            Toast.makeText(this, "선택된 범위가 없습니다", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap // naverMap 변수 초기화

        //클라이언트 객체 생성
        val naverMapApiInterface = NaverMapRequest.getClient().create(NaverMapApiInterface::class.java)


        //응답 받을 콜백 구현
//            val call = naverMapApiInterface.getNaverMapData()
        val call: Call1<NaverMapItem> = naverMapApiInterface.getMapData()

        Log.v("Debug중","디버그중입니다.")

        //클라이언트 객체가 제공하는 enqueue로 통신에 대한 요청, 응답 처리 방법 명시
        call.enqueue(object : Callback<NaverMapItem> {
            override fun onResponse(call: Call1<NaverMapItem>, response: Response<NaverMapItem>) {
                if(response.isSuccessful){
                    Log.v("디버깅중", "성공!!!!!")
                    naverMapList = response.body()
                    naverMapInfo = naverMapList?.jjwifi

                    Toast.makeText(this@MainActivity, naverMapInfo?.get(1)?.address, Toast.LENGTH_LONG).show()

                }

            }
            override fun onFailure(call: Call1<NaverMapItem>, t: Throwable) {
                // 통신 실패 시 처리할 코드
                Log.v("디버깅중", "실패!!!!!")
            }
        })

        //실내지도 활성화
//            naverMap.isIndoorEnabled = true
//            naverMap.uiSettings.isIndoorLevelPickerEnabled = true // 실내지도 층 버튼

//            naverMap.locationSource = locationSource // 기존에 설정된 위치 소스 초기화
        naverMap.locationSource = FusedLocationSource(this, 1000)
        naverMap.locationTrackingMode = LocationTrackingMode.Follow // 위치 추적 모드를 Follow로 설정
        naverMap.uiSettings.isLocationButtonEnabled = true // 현위치 버튼


// 좌표 리스트
        val coordinates = listOf(
            LatLng(35.8414219, 127.0748137),
            LatLng(35.8424219, 127.0768137),
            LatLng(35.8434219, 127.0778137)
            // 필요한 만큼 LatLng 객체를 추가
        )

// 마커 리스트
        val markers = mutableListOf<Marker>()

        var markerPosition : LatLng? = null
// 마커 생성 및 지도에 추가
        for (coordinate in coordinates) {
            val marker = Marker().apply {
                position = coordinate
                map = naverMap
                icon = MarkerIcons.BLACK
                iconTintColor = Color.RED
                width = Marker.SIZE_AUTO
                height = Marker.SIZE_AUTO
                alpha = 0.0F
            }

            // 마커 클릭 리스너 설정
            marker.setOnClickListener {
                markerPosition = marker.position
                val latitudeString = markerPosition!!.latitude.toString() // 위도를 문자열로 변환
                val longitudeString = markerPosition!!.longitude.toString() // 경도를 문자열로 변환
                val positionString = "$latitudeString $longitudeString" // 위도와 경도를 합쳐서 위치를 나타내는 문자열 생성

                // 테스트용 Toast출력
//                Toast.makeText(this, positionString, Toast.LENGTH_SHORT).show()

                openDrawerWithMarkerInfo(positionString) // 마커에 대한 정보를 슬라이딩 드로어에 표시
                true
            }
            marker.captionMinZoom = 14.0
            marker.minZoom = 14.0
//            marker.captionMaxZoom = 16.0
//            marker.maxZoom = 16.0

            // 마커 리스트에 추가
            markers.add(marker)
        }

        clusterer.add(ItemKey(1, LatLng(35.8414219, 127.0748137)), null)
        clusterer.add(ItemKey(2, LatLng(35.8424219, 127.0768137)), null)
        clusterer.add(ItemKey(3, LatLng(35.8434219, 127.0778137)), null)

        val keyTagMap = mapOf(
            ItemKey(1, LatLng(35.8414219, 127.0748137)) to null,
            ItemKey(2, LatLng(35.8424219, 127.0768137)) to null,
            ItemKey(3, LatLng(35.8434219, 127.0778137)) to null,
        )
        clusterer.addAll(keyTagMap)

        clusterer.map = naverMap







        val jeonjuBoundary = listOf(
            LatLng(35.893238, 127.000492), // 좌표1
            LatLng(35.902305, 127.133935), // 좌표2
            LatLng(35.854854, 127.171349), // 좌표3
            LatLng(35.860415, 127.196296), // 좌표4
            LatLng(35.821860, 127.231094), // 좌표5
            LatLng(35.774740, 127.193898), // 좌표6
            LatLng(35.722784, 127.060043), // 좌표7
            LatLng(35.756684, 127.048948), // 좌표8
            LatLng(35.849477, 127.047553), // 좌표9
            LatLng(35.861196, 127.008753)  // 좌표10
        )

// 폴리곤 생성
        val polygon = PolygonOverlay().apply {
            coords = jeonjuBoundary
            color = Color.argb(33, 255, 0, 0) // 폴리곤 색상 설정 (fillColor)
            outlineColor = Color.argb(153, 255, 0, 0) // 폴리곤 테두리 색상 설정 (strokeColor)
            outlineWidth = 3 // 폴리곤 테두리 두께 설정 (strokeWeight)
            map = naverMap // 지도에 폴리곤 표시
        }

        // 지도 확대 수준에 따라 폴리곤 가시성 조정
        naverMap.addOnCameraChangeListener { reason, animated ->
            val zoomLevel = naverMap.cameraPosition.zoom
            // 원하는 확대 수준 설정 (예: 15 이상에서 폴리곤 숨기기)
            polygon.map = if (zoomLevel >= 13) null else naverMap
        }


        scaleBtn.setOnClickListener{
            markerPosition?.let { it1 -> drawCircle(it1) }
        }
        non_scaleBtn.setOnClickListener{
            removeCircle()
        }
        search_loadBtn.setOnClickListener{
                fetchCurrentLocation()
//            openNaverMapAppForDirections()
//            Toast.makeText(this, "길찾기", Toast.LENGTH_SHORT).show()
            // 추가 부분(테스트 용)
            markerPosition
            val latitude = currentLatLng?.latitude ?: 0.0
            val longitude = currentLatLng?.longitude ?: 0.0
            val positionString = "Latitude: $latitude, Longitude: $longitude"
            Toast.makeText(this, positionString, Toast.LENGTH_SHORT).show()
            //여기까지가 추가 부분
        }
    }




    //    companion object {
//        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
//    }

}
