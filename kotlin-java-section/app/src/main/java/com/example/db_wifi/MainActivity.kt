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
import com.naver.maps.map.clustering.Clusterer
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.naver.maps.map.util.MarkerIcons
import android.Manifest
import android.graphics.PointF
import androidx.core.app.ActivityCompat
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.PathOverlay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//import com.naver.maps.map.CameraUpdate
class MainActivity : FragmentActivity(), OnMapReadyCallback {

    private var naverMapInfo: List<NaverMapData>? = null
    private var naverMapList: NaverMapItem? = null

    private var clusterer: Clusterer<ItemKey> = Clusterer.Builder<ItemKey>().screenDistance(20.0).build()

    // FusedLocationProviderClient는 manifest에서 위치권한 얻은 후 사용할 수 있습니다!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationSource: FusedLocationSource // 위치관련 클래스(타입)

    //위치 권한 요청을 위한 코드
    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 1000

    private lateinit var naverMap: NaverMap
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var markerInfoText: TextView

    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    // 위치 권한 요청
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // 권한이 허용되었을 때 위치 정보 요청
                requestLocationUpdates() // 현재위치 불러오는 함수
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
        checkLocationPermission()// 권한 확인 함수

        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient("f5wddcflyd")

        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            } // 지도 나타내기
        mapFragment.getMapAsync(this) // 지도가 준비되었을 때 호출될 메서드 -> onMapReady 대기

        // activity_main.xml의 해당 영역을 조작하거나 업데이트 하기위해 불러온 것
        drawerLayout = findViewById(R.id.main) // 배경 영역 선택
        markerInfoText = findViewById(R.id.marker_info_text) // 슬라이딩 드로어 영역 선택

        // 위치 권한이 허용되었는지 확인하고, 그렇지 않은 경우 사용자에게 권한을 요청하는 데 사용되는 객체를 생성
        // 사용자가 권한을 부여하면 해당 위치 정보를 사용
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)


        enableEdgeToEdge()
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
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
                currentLatitude = location?.latitude ?: 0.0
                currentLongitude = location?.longitude ?: 0.0
                Toast.makeText(
                    this,
                    "현재 위치: 위도 $currentLatitude, 경도 $currentLongitude",
                    Toast.LENGTH_LONG
                ).show()
            }.addOnFailureListener { e ->
                // 위치 가져오기 실패시 처리
                Toast.makeText(this, "위치 정보를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("Location", "Failed to get location: ${e.message}")
            }
    }    // 위치 정보 요청


    private fun openDrawerWithMarkerInfo(markerInfo: String) {
        markerInfoText.text = markerInfo // 슬라이딩 드로어에 마커 정보 설정
        drawerLayout.openDrawer(GravityCompat.START) // 슬라이딩 드로어 열기
    }


    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap // naverMap 변수 초기화
        naverMap.isIndoorEnabled = true // 실내지도

        naverMap.locationSource = locationSource // 기존에 설정된 위치 소스 초기화
        naverMap.locationTrackingMode = LocationTrackingMode.Follow // 위치 추적 모드를 Follow로 설정
        naverMap.uiSettings.isLocationButtonEnabled = true // 현위치 버튼


        val APIKEY_ID = "tgoutvp62u"
        val APIKEY = "sVfCuiLh1aK2gLTNqEPPn24P5r7gybDHLVEyVibx"
        //레트로핏 객체 생성
        val retrofit = Retrofit.Builder()
            .baseUrl("https://naveropenapi.apigw.ntruss.com/map-direction/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(NaverAPI::class.java)

        //근처에서 길찾기
        val callgetPath = api.getPath(APIKEY_ID, APIKEY, "127.13602285714192 , 35.840335812433025", "127.12944193975801, 35.84678030608311")

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
//        val markerList = mutableListOf<Marker>() // 마커 리스트 생성
//            // 마커 생성
//            val marker1 = Marker()
//            val currentLocation = LatLng(currentLatitude!!,currentLongitude!!)
//            marker1.position = currentLocation
//            markerList.add(marker1) // 마커 리스트에 추가
//
//            for(marker in markerList){
//                marker.map = naverMap
//                marker.icon = MarkerIcons.BLACK
//                marker.iconTintColor = Color.RED // 마커 겉의 색깔
//                marker.captionText = "현재 위치 : ${marker.position.latitude},${marker.position.longitude}" // 마크에 글자 설정
//                marker.setCaptionAligns(Align.Top) // 글자를 위로
//                marker.captionTextSize = 15.04f // 텍스트 사이즈
//                marker.captionOffset = 30 // 마크과 글자 사이 간격
//
//                // 특정 줌 에서만 마크와 글자가 보임
////                marker.captionMinZoom = 12.0
////                marker.captionMaxZoom = 16.0
////                marker.minZoom = 12.0
////                marker.maxZoom = 16.0
//
//                // marker에 대한 정보 저장
//                val marker1Info = "위도: ${marker.position.latitude}, 경도: ${marker.position.longitude}"
//                marker.tag = marker1Info // 마커 태그에 정보 입력
//
//                // 마커를 클릭했을 때의 동작 설정
//                marker.setOnClickListener {
//                    val markerInfo = it.tag as? String // 마커태그에 저장된 정보 가져오기
//                    markerInfo?.let { info ->
//                        openDrawerWithMarkerInfo(info) // 마커에 대한 정보를 슬라이딩 드로어에 표시(info를 매개변수로한 위에 정의한 함수 가져오기)
//                    }
//                    true
//                }
//            }





        //실내지도 활성화
//        naverMap.isIndoorEnabled = true
//        naverMap.uiSettings.isIndoorLevelPickerEnabled = true // 실내지도 층 버튼
//
//            // 마커를 클릭했을 때의 동작 설정
//            val marker = Marker()
//            marker.position = LatLng(35.83430784590786, 127.13271435616436)
//            marker.map = naverMap
//            marker.icon = MarkerIcons.BLACK
//            marker.iconTintColor = Color.RED
//            marker.width = Marker.SIZE_AUTO
//            marker.height = Marker.SIZE_AUTO
//            marker.setOnClickListener {
//                val cameraPosition = naverMap.cameraPosition
//
//                val latitudeString = cameraPosition.target.latitude.toString() // 위도를 문자열로 변환
//                val longitudeString = cameraPosition.target.longitude.toString() // 경도를 문자열로 변환
//                val positionString : String = "$latitudeString $longitudeString" // 위도와 경도를 합쳐서 위치를 나타내는 문자열 생성
//
//                openDrawerWithMarkerInfo(positionString) // 마커에 대한 정보를 슬라이딩 드로어에 표시
//                true
//            }


    }
}