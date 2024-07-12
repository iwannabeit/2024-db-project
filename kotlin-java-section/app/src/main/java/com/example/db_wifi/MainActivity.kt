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
import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import com.example.db_wifi.addMarkerControll.SecondActivity
import com.example.db_wifi.addMarkerControll.WifiLocation
import com.example.db_wifi.addMarkerData.RetrofitClient
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.clustering.ClusterMarkerInfo
import com.naver.maps.map.clustering.DefaultClusterMarkerUpdater
import com.naver.maps.map.clustering.DefaultLeafMarkerUpdater
import com.naver.maps.map.clustering.LeafMarkerInfo
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.overlay.PolygonOverlay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.Serializable


//import com.naver.maps.map.CameraUpdate
open class MainActivity : FragmentActivity(), OnMapReadyCallback {

    private var naverMapInfo: List<NaverMapData>? = null
    private var naverMapList: NaverMapItem? = null

    // FusedLocationProviderClient는 manifest에서 위치권한 얻은 후 사용할 수 있습니다!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationSource: FusedLocationSource // 위치관련 클래스(타입) // 위치관련 클래스(타입)

    //위치 권한 요청을 위한 코드
    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 1000

    private lateinit var naverMap: NaverMap


    private lateinit var markerInfoText: TextView

    private var latitudeString : String? = null
    private var longitudeString : String? = null
    private lateinit var targetPostion : LatLng

    private var currentLatLng : LatLng? = null
    private var currentLatitude :Double = 0.0
    private var currentLongitude :Double = 0.0

    private var currentLocation: LatLng? = null
    private var selectedMarker: Marker? = null
    private var path = PathOverlay() // 경로선을 나타내는 위한 오버레이
    private lateinit var search_loadBtn : Button
    private lateinit var scaleBtn : Button
    private lateinit var non_scaleBtn : Button
    private lateinit var mywifi_Btn : Button // 내 와이파이 버튼

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>


    //실내 실외 버튼 코드
    private lateinit var indoorBtn : Button
    private lateinit var outdoorBtn : Button
    private lateinit var myBtn : Button


    //검색 기능
    private lateinit var add_srchBtn : ImageButton
    private lateinit var autoComplete : AutoCompleteTextView
    private lateinit var searchList : MutableList<String>
    private lateinit var LatLngList : MutableList<LatLng>
    private var index : Int? = 0

    //길찾기 종료
    private lateinit var finish_loadBtn : Button

    // 마커 만들기
    val start_marker = Marker() // 길찾기 시작 마커
    val end_marker = Marker() // 길찾기 도착지 마커
    var isIndoor = false
    var isOutdoor = false
    var isMyMarker = false

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


//                // -------------------------------
//        // 예시로 데이터 생성
//        val latitude = 127.442
//        val longitude = 53.223
//        val description = "Example marker"
//
//        // 서버에 전송할 데이터 객체 생성
////        val markerData = MarkerData(latitude, longitude, description)
//        val markerData = MarkerData(latitude, longitude, description)
//
//
//        // Retrofit 서비스 객체를 사용하여 서버에 데이터 전송
//        RetrofitClient.markerApiService.addMarker(markerData)
//        .enqueue(object : Callback<Void> {
//            override fun onResponse(call: Call<Void>, response: Response<Void>) {
//                if (response.isSuccessful) {
//                    Log.d("Marker", "${markerData}")
//                } else {
//                    Log.e("Marker", "Failed to add marker")
//                }
//            }
//
//            override fun onFailure(call: Call<Void>, t: Throwable) {
//                Log.e("Marker", "Error adding marker: ${t.message}")
//            }
//        })
//        // -------------------------------
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
        mywifi_Btn = findViewById(R.id.mywifi_button)

        // 내부,외부 마커
        indoorBtn = findViewById(R.id.indoor_button)
        outdoorBtn = findViewById(R.id.outdoor_button)
        myBtn = findViewById(R.id.myw_button)

        // 검색 기능
        add_srchBtn = findViewById(R.id.addressSrchBtn)
        autoComplete = findViewById(R.id.autoComplete)

        searchList = arrayListOf<String>()
        LatLngList = arrayListOf<LatLng>()

        // 길찾기 종료 버튼
        finish_loadBtn = findViewById(R.id.finish_loadBtn)
        finish_loadBtn.visibility = View.INVISIBLE
        finish_loadBtn.isClickable = false


        autoComplete.setAdapter(ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, searchList))

        add_srchBtn.setOnClickListener{
            hideKeyboard()
            val add_text : String = autoComplete.text.toString()
            if(correctList(add_text)) {
                Log.d("검색버튼 디버깅", "결과 값: $add_text")
                val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLngList[index!!], 18.0)
                    .animate(CameraAnimation.Fly, 3000)
                naverMap.moveCamera(cameraUpdate)
//                Toast.makeText(this, "검색 성공!", Toast.LENGTH_SHORT).show()
                drawCircle(LatLngList[index!!])
                index = 0
            } else {
                Toast.makeText(this, "해당 주소를 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
            }
            true
        }

        enableEdgeToEdge()

    }

    //검색 관련
    private fun correctList(add : String): Boolean {
        for(listAdd in searchList) {
            if(add == listAdd)
                return true
            index = index?.plus(1)
        }
        index = 0
        return false
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(autoComplete.windowToken, 0)
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
//                Toast.makeText(
//                    this,
//                    "현재 위치: 위도 $latitude, 경도 $longitude",
//                    Toast.LENGTH_LONG
//                ).show()
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

    private fun openNaverMapAppForDirections(startLatitude: Double, startLongitude: Double, endLatitude: Double, endLongitude: Double) {
        // 길찾기 예시
        val APIKEY_ID = "tgoutvp62u"
        val APIKEY = "5G2F2i3pLlKwqmJDFH18gEBFGNM7M44GvTwm3s3M"
        //레트로핏 객체 생성
        val retrofit = Retrofit.Builder()
            .baseUrl("https://naveropenapi.apigw.ntruss.com/map-direction/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(NaverAPI::class.java)

        // 출발지-목적지
        val callgetPath = api.getPath(APIKEY_ID, APIKEY, "${startLongitude} , ${startLatitude}", "${endLongitude}, ${endLatitude}")

        callgetPath.enqueue(object : Callback<ResultPath> { // 비동기 방식으로 API 요청
            override fun onResponse(call: Call<ResultPath>, response: Response<ResultPath>) { // Response 객체를 통해 응답 데이터를 접근가능
                val path_cords_list = response.body()?.route?.traoptimal // traoptimal = 실시간 최적경로 넣기


                // 경로 넣어놓기 위한 공간, MutableList에 add 기능 쓰기 위해 더미 원소(0.1, 0.1) 하나 넣어둠
                val path_container: MutableList<LatLng> = mutableListOf(LatLng(0.1, 0.1))

                //경로 그리기 응답바디가 List<List<Double>> 이라서 2중 for문 썼음
                //구한 경로를 하나씩 path_container에 추가
                path_cords_list?.forEach { path_cords ->
                    path_cords.path.forEach { path_cords_xy ->
                        path_container.add(LatLng(path_cords_xy[1], path_cords_xy[0]))
                    }
                }


// 새로운 PathOverlay 객체 생성
                path.map = null

                val newPath = PathOverlay()

                if (path.map != null) {
                    // 기존에 있던 길찾기 제거
                    path.map = null
                    Toast.makeText(this@MainActivity, "경ss.", Toast.LENGTH_SHORT).show()

                }

// 새로운 경로 그리기
                //더미원소(0.1,0.1) 드랍후 path.coords에 path들을 넣어줌.
                newPath.coords = path_container.drop(1)
                newPath.color = Color.GREEN
                newPath.map = naverMap // 경로선 그리기

// 이후 path를 새로운 객체로 갱신

                path = newPath

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


    // ====================================================================================
    // 내가 추가한 마커 리스트 불러오는 함수
    private val wifiDataList = mutableListOf<WifiLocation>()
    private fun loadFile(){
        try {
            val inputStream = openFileInput("wifi_data.txt")
            val inputStreamReader = inputStream.reader()
            val bufferedReader = BufferedReader(inputStreamReader)
            var line: String? = bufferedReader.readLine()
            while (line != null) {

                val wifiLatLng = extractWifiLocationFromLine(line)
                wifiDataList.add(wifiLatLng)

                line = bufferedReader.readLine()
            }
//            wifiDataList.forEach {
//                Toast.makeText(this, "${it.name}", Toast.LENGTH_SHORT).show()
//            }
            bufferedReader.close()
        } catch (e: IOException) {
            Toast.makeText(this, "실패", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
//        return wifiDataList
    }

    private fun extractWifiLocationFromLine(line: String): WifiLocation {
        val parts = line.split(" - ")
        // 첫 번째 요소가 이름
        val name = parts[0]

        val coordinates = parts[1].split(", ")
        val latitude = coordinates[0].toDouble()

        val coordinates2 = coordinates[1].split("/")
        val longitude = coordinates2[0].toDouble()
        val password = coordinates2[1]

        return WifiLocation(name, latitude, longitude, password)
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap // naverMap 변수 초기화
        naverMap.isIndoorEnabled = true // 실내지도

        naverMap.locationSource = FusedLocationSource(this, 1000)
        naverMap.locationTrackingMode = LocationTrackingMode.Follow // 위치 추적 모드를 Follow로 설정
        naverMap.uiSettings.isLocationButtonEnabled = true // 현위치 버튼

        // 현재 위치 위도, 경도 저장
        fetchCurrentLocation()
//        currentLatitude = currentLatLng?.latitude ?: 0.0
//        currentLongitude = currentLatLng?.longitude ?: 0.0

        val secondActivity = SecondActivity()


        // 마커 띄우는 곳!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        val s_marker = Marker()

        val coordinates = mutableListOf<LatLng>()
        var markerPosition : LatLng? = null



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

                    val indoorMarkers = mutableListOf<Marker>()
                    val outdoorMarkers = mutableListOf<Marker>()
                    val myMarkers = mutableListOf<Marker>()

                    val builder: Clusterer.Builder<ItemKey> = Clusterer.Builder<ItemKey>()


                    builder.clusterMarkerUpdater(object : DefaultClusterMarkerUpdater() {
                        override fun updateClusterMarker(info: ClusterMarkerInfo, marker: Marker) {
                            super.updateClusterMarker(info, marker)

                            val bitmap = BitmapFactory.decodeResource(this@MainActivity.resources, R.drawable.wf_clusterer)
                            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, false)
                            marker.icon = OverlayImage.fromBitmap(resizedBitmap)
                            marker.captionTextSize = 25f
                            marker.captionColor = Color.rgb(100, 12, 12)
                            marker.captionText = info.size.toString()

                        }
                    }).leafMarkerUpdater(object : DefaultLeafMarkerUpdater() {
                        override fun updateLeafMarker(info: LeafMarkerInfo, marker: Marker) {
                            super.updateLeafMarker(info, marker)
                            val key = info.key as ItemKey

                            val bitmap = BitmapFactory.decodeResource(this@MainActivity.resources, R.drawable.wf_marker)
                            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 110, 150, false)
                            marker.icon = OverlayImage.fromBitmap(resizedBitmap)
                        }
                    })

                    val in_clusterer: Clusterer<ItemKey> = builder.screenDistance(100.0).build()
                    val out_clusterer: Clusterer<ItemKey> = builder.screenDistance(100.0).build()
                    val my_clusterer: Clusterer<ItemKey> = builder.screenDistance(100.0).build()


                    // 길찾기 클러스터
                    val start_clusterer: Clusterer<ItemKey> = builder.screenDistance(100.0).build()
                    val end_clusterer: Clusterer<ItemKey> = builder.screenDistance(100.0).build()
                    naverMapInfo?.let{
                        for(i in 0 until it.size){

                            val lat = it.get(i).y
                            val lnt = it.get(i).x
                            val marker = Marker()
                            // 장소이름 저장

                            var s_place = it.get(i).place.toString()

                            //검색 리스트 저장
                            searchList.add(it.get(i).place)
                            LatLngList.add(LatLng(lat,lnt))


                            marker.setOnClickListener {
                                markerPosition = marker.position
                                openDrawerWithMarkerInfo(s_place) // 마커에 대한 정보를 슬라이딩 드로어에 표시
                                true
                            }

                            marker.alpha = 0.0f
                            marker.position = LatLng(it[i].y, it[i].x)
//
                            if(it[i].side == "inside"){
                                indoorMarkers.add(marker)
                            }
                            else{
                                outdoorMarkers.add(marker)
                            }


                        }
                    }
                    indoorBtn.setOnClickListener {
                        isIndoor = true
                        isOutdoor = false
                        isMyMarker = false
                        // 모든 야외 마커 숨기기
                        outdoorMarkers.forEach { it.map = null }
                        // 내 마커 숨기기
                        myMarkers.forEach{ it.map = null }
                        // 모든 실내 마커 표시하기
                        indoorMarkers.forEach {
                            it.map = naverMap
                            in_clusterer.add(ItemKey(it.hashCode(), it.position), null)
                        }
                        out_clusterer.map = null
                        in_clusterer.map = naverMap
                        my_clusterer.map = null

                    }

                    outdoorBtn.setOnClickListener {
                        isOutdoor = true
                        isIndoor = false
                        // 모든 실내 마커 숨기기
                        indoorMarkers.forEach { it.map = null }
                        // 내 마커 숨기기
                        myMarkers.forEach{ it.map = null }
                        // 모든 야외 마커 표시하기
                        outdoorMarkers.forEach {
                            it.map = naverMap
                            out_clusterer.add(ItemKey(it.hashCode(), it.position), null)
                        }

                        out_clusterer.map = naverMap
                        in_clusterer.map = null
                        my_clusterer.map = null

                    }

                    myBtn.setOnClickListener {
                        isIndoor = false
                        isOutdoor = false
                        out_clusterer.map = null
                        in_clusterer.map = null
                        my_clusterer.map = null

                        myMarkers.forEach { it.map = null }
                        myMarkers.clear()

                        loadFile()

                        wifiDataList.forEach { data ->
                            val myMarker = Marker() // 내가 설정한 마커
                            myMarker.position = LatLng(data.latitude, data.longitude)

                            myMarker.alpha = 0.0f
                            myMarker.setOnClickListener {
                                markerPosition = myMarker.position
                                openDrawerWithMarkerInfo(data.name) // 마커에 대한 정보를 슬라이딩 드로어에 표시
                                true
                            }
                            my_clusterer.add(ItemKey(data.hashCode(), myMarker.position), null)

                            myMarkers.add(myMarker)
                        }

                        // 마커 표시
                        myMarkers.forEach { it.map = naverMap }
                        my_clusterer.map = naverMap
                    }

                    // 경로찾기 버튼 클릭
                    search_loadBtn.setOnClickListener{
                        start_clusterer.clear()
                        end_clusterer.clear()

                        // 현재위치
                        currentLatitude = currentLatLng?.latitude ?: 0.0
                        currentLongitude = currentLatLng?.longitude ?: 0.0

                        // 마커 포지션
                        val latitude1 = markerPosition?.latitude ?: 0.0
                        val longitude1 = markerPosition?.longitude ?: 0.0

                        val positionString = "Latitude: $currentLatitude, Longitude: $currentLongitude"

                        openNaverMapAppForDirections(currentLatitude, currentLongitude, latitude1, longitude1)

                        finish_loadBtn.visibility = View.VISIBLE
                        finish_loadBtn.isClickable = true

                        // 출발지, 도착지 마커


                        start_marker.position = LatLng(currentLatitude, currentLongitude)
                        start_clusterer.add(ItemKey(start_marker.hashCode(), start_marker.position), null)
                        start_clusterer.map = naverMap

                        end_marker.position = LatLng(latitude1,longitude1)
                        end_clusterer.add(ItemKey(end_marker.hashCode(), end_marker.position), null)
                        end_clusterer.map = naverMap


                        indoorMarkers.forEach { it.map = null }
                        outdoorMarkers.forEach{ it.map = null}
                        myMarkers.forEach{it.map = null}
                        out_clusterer.map = null
                        in_clusterer.map = null
                        my_clusterer.map = null

                    }

                    // 경로찾기 종료
                    finish_loadBtn.setOnClickListener{
                        start_marker.map = null
                        end_marker.map = null
                        path.map = null
                        if(isIndoor){
                            indoorMarkers.forEach { it.map = naverMap }
                            in_clusterer.map = naverMap
                        }
                        if(isOutdoor){
                            outdoorMarkers.forEach{ it.map = naverMap}
                            out_clusterer.map = naverMap
                        }
                        start_clusterer.clear()
                        end_clusterer.clear()


//            Toast.makeText(this,  "경로 안내가 종료 되었습니다.", Toast.LENGTH_SHORT).show()
                        finish_loadBtn.visibility = View.INVISIBLE
                        finish_loadBtn.isClickable = false
                    }

                }
            }
            override fun onFailure(call: Call<NaverMapItem>, t: Throwable) {
                // 통신 실패 시 처리할 코드
                Log.v("디버깅중", "실패!!!!!")
            }
        })



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

        mywifi_Btn.setOnClickListener{ // 내 와이파이 리스트를 보는 버튼
            val intent = Intent(this@MainActivity, SecondActivity::class.java)
            startActivity(intent)
        }
    }


    //    companion object {
//        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
//    }
}