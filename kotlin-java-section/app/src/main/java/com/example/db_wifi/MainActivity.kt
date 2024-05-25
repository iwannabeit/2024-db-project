package com.example.db_wifi

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.clustering.Clusterer
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.naver.maps.map.util.MarkerIcons
import android.Manifest
import android.location.Location
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import com.example.db_wifi.NaverAPI
import com.example.db_wifi.ResultPath
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.overlay.PolygonOverlay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//import com.naver.maps.map.CameraUpdate
class MainActivity : FragmentActivity(), OnMapReadyCallback {

    private var naverMapInfo: List<NaverMapData>? = null
    private var naverMapList: NaverMapItem? = null

    // FusedLocationProviderClient는 manifest에서 위치권한 얻은 후 사용할 수 있습니다!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationSource: FusedLocationSource // 위치관련 클래스(타입)

    //위치 권한 요청을 위한 코드
    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 1000

    private lateinit var naverMap: NaverMap

    private lateinit var markerInfoText: TextView
    private lateinit var search_loadBtn : Button
    private lateinit var scaleBtn : Button
    private lateinit var non_scaleBtn : Button

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private var latitudeString : String? = null
    private var longitudeString : String? = null
    private lateinit var targetPostion : LatLng
    private var currentLatLng : LatLng? = null
    private var currentLatitude :Double = 0.0
    private var currentLongitude :Double = 0.0

    private var currentLocation: LatLng? = null
    private var selectedMarker: Marker? = null

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

    // 클러스터링 구조
    private var clusterer: Clusterer<ItemKey> = Clusterer.Builder<ItemKey>()
        .minZoom(3) // 클러스터링이 시작되는 최소 줌 레벨
        .maxZoom(13) // 클러스터링이 끝나는 최대 줌 레벨
        .screenDistance(50.0)
        .build()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

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
                    // 예를 들어, 현재 위치를 사용하여 네이버 지도에 마커를 추가하거나 경로를 그릴 수 있습니다.
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
        // 권한 확인 함수
        when {
            ContextCompat.checkSelfPermission( // 권한이 부여되어있는지 확인하는 메소드
                this, // 현재 액티비티에서
                Manifest.permission.ACCESS_FINE_LOCATION // GPS접근 권한이 있는가
            ) == PackageManager.PERMISSION_GRANTED -> { // 권한이 허락되었음을 알리는 함수, 위의 값과 동일하면 권한을 허락한 것
                requestLocationUpdates() // 현재위치 불러오는 함수
            }
            else -> {
                // 권한이 없을 경우 GPS 권한 요청
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
        markerInfoText.text = markerInfo // Bottom Sheet 영역에 마커 정보 설정
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun openNaverMapAppForDirections(startLatitude: Double, startLongitude: Double) {
        // 길찾기 예시
        val APIKEY_ID = "tgoutvp62u"
        val APIKEY = "sVfCuiLh1aK2gLTNqEPPn24P5r7gybDHLVEyVibx"
        //레트로핏 객체 생성
        val retrofit = Retrofit.Builder()
            .baseUrl("https://naveropenapi.apigw.ntruss.com/map-direction/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(NaverAPI::class.java)

        // 출발지-목적지
        val callgetPath = api.getPath(APIKEY_ID, APIKEY, "${startLongitude} , ${startLatitude}", "127.12944193975801, 35.84678030608311")

        callgetPath.enqueue(object : Callback<ResultPath> { // 비동기 방식으로 API 요청
            override fun onResponse(call: Call<ResultPath>, response: Response<ResultPath>) { // Response 객체를 통해 응답 데이터를 접근가능
                val path_cords_list = response.body()?.route?.traoptimal // traoptimal = 실시간 최적경로 넣기

                val path = PathOverlay() // 경로선을 나타내는 위한 오버레이

                // 경로 넣어놓기 위한 공간, MutableList에 add 기능 쓰기 위해 더미 원소(0.1, 0.1) 하나 넣어둠
                val path_container: MutableList<LatLng> = mutableListOf(LatLng(0.1, 0.1))

                //경로 그리기 응답바디가 List<List<Double>> 이라서 2중 for문 썼음
                //구한 경로를 하나씩 path_container에 추가
                path_cords_list?.forEach { path_cords ->
                    path_cords.path.forEach { path_cords_xy ->
                        path_container.add(LatLng(path_cords_xy[1], path_cords_xy[0]))
                    }
                }

                //더미원소(0.1,0.1) 드랍후 path.coords에 path들을 넣어줌.
                path.coords = path_container.drop(1)
                path.color = Color.GREEN
                path.map = naverMap // 경로선 그리기

                //경로 시작점으로 화면 이동
                if (path.coords.isNotEmpty()) {
                    val cameraUpdate = CameraUpdate.scrollTo(path.coords[0])
                        .animate(CameraAnimation.Fly, 3000)
                    naverMap.moveCamera(cameraUpdate)

                    Toast.makeText(this@MainActivity, "경로 안내가 시작됩니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResultPath>, t: Throwable) {
                Toast.makeText(this@MainActivity, "경로 안내 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private lateinit var circle : CircleOverlay
    private fun drawCircle(center: LatLng) {
        circle = CircleOverlay().apply {
            this.center = center
            this.radius = 30.0
            this.color = Color.argb(33, 0, 0, 255)
            this.map = naverMap
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
        }
    }





    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap // naverMap 변수 초기화
        naverMap.isIndoorEnabled = true // 실내지도

        naverMap.locationSource = FusedLocationSource(this, 1000)
        naverMap.locationTrackingMode = LocationTrackingMode.Follow // 위치 추적 모드를 Follow로 설정
        naverMap.uiSettings.isLocationButtonEnabled = true // 현위치 버튼

        // 현재 위치 위도, 경도 저장
        fetchCurrentLocation()
        currentLatitude = currentLatLng?.latitude ?: 0.0
        currentLongitude = currentLatLng?.longitude ?: 0.0

        // 마커 띄우는 곳!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        val s_marker = Marker()

        //클라이언트 객체 생성
        val naverMapApiInterface = NaverMapRequest.getClient().create(NaverMapApiInterface::class.java)

        //응답 받을 콜백 구현
        val call: Call<NaverMapItem> = naverMapApiInterface.getMapData()
        Log.v("Debug중","디버그중입니다.")

        //클라이언트 객체가 제공하는 enqueue로 통신에 대한 요청, 응답 처리 방법 명시
        call.enqueue(object : Callback<NaverMapItem> {
            override fun onResponse(call: Call<NaverMapItem>, response: Response<NaverMapItem>) {
                if(response.isSuccessful){
                    Log.v("디버깅중", "성공!!!!!")
                    naverMapList = response.body()
                    naverMapInfo = naverMapList?.jjwifi

                    naverMapInfo?.let{
                        for(i in 0 until it.size){
                            val lat = it.get(i).y
                            val lnt = it.get(i).x
                            s_marker.position = LatLng(lat, lnt)

                            //클러스터링
                            clusterer.add(ItemKey(i, LatLng(it.get(i).y, it.get(i).x)), null)

                        }
                    }
                    clusterer.map = naverMap
                }

            }
            override fun onFailure(call: Call<NaverMapItem>, t: Throwable) {
                // 통신 실패 시 처리할 코드
                Log.v("디버깅중", "실패!!!!!")
            }
        })


        // 마커를 클릭했을 때의 동작 설정
        val marker = Marker()
        marker.position = LatLng(35.84678030608311, 127.12944193975801)
        marker.map = naverMap
        marker.icon = MarkerIcons.BLACK
        marker.iconTintColor = Color.RED
        marker.width = Marker.SIZE_AUTO
        marker.height = Marker.SIZE_AUTO
        lateinit var markerPosition : LatLng
        marker.setOnClickListener {
            markerPosition = marker.position
            targetPostion = markerPosition

            latitudeString = markerPosition.latitude.toString() // 위도를 문자열로 변환
            longitudeString = markerPosition.longitude.toString() // 경도를 문자열로 변환
            val positionString : String = "$latitudeString $longitudeString" // 위도와 경도를 합쳐서 위치를 나타내는 문자열 생성

            openDrawerWithMarkerInfo(positionString) // 마커에 대한 정보를 슬라이딩 드로어에 표시
            true
        }
        // 특정 줌 에서만 마크와 글자가 보임
        marker.captionMinZoom = 13.0
//            marker.captionMaxZoom = 16.0
        marker.minZoom = 13.0
//            marker.maxZoom = 16.0


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
            drawCircle(markerPosition)

        }
        non_scaleBtn.setOnClickListener{
            removeCircle()
        }
        search_loadBtn.setOnClickListener{
            fetchCurrentLocation()
            currentLatitude = currentLatLng?.latitude ?: 0.0
            currentLongitude = currentLatLng?.longitude ?: 0.0
            val positionString = "Latitude: $currentLatitude, Longitude: $currentLongitude"
            Toast.makeText(this, positionString, Toast.LENGTH_SHORT).show()

            //, currentLatitude, currentLongitude
            openNaverMapAppForDirections(35.840335812433025, 127.13602285714192)
                Toast.makeText(this, "길찾기", Toast.LENGTH_SHORT).show()
        }
    }
    //    companion object {
//        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
//    }
}