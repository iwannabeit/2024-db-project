package com.example.db_wifi.addMarkerControll

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
import com.naver.maps.map.util.FusedLocationSource
import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.location.Location
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.app.ActivityCompat
import com.example.db_wifi.R
import com.naver.maps.map.overlay.Marker
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException


class PlusActivity : FragmentActivity(), OnMapReadyCallback {

    // FusedLocationProviderClient는 manifest에서 위치권한 얻은 후 사용할 수 있습니다!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationSource: FusedLocationSource // 위치관련 클래스(타입)

    //위치 권한 요청을 위한 코드
    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 1000
    private lateinit var naverMap: NaverMap
    private var currentLatLng : LatLng? = null

    //버튼들
    private lateinit var addWifi_Btn : ImageButton
    private lateinit var close_Btn : ImageButton
    private lateinit var myWifi_name : EditText

    // txt파일
    private val FILENAME: String = "wifi_data.txt"

    // intent를 통해 가져온 배열
    private lateinit var wifiDataList: ArrayList<WifiLocation>

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
        setContentView(R.layout.plus_layout)

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        wifiDataList = intent.getSerializableExtra("wifiDataList") as ArrayList<WifiLocation>
        Log.d("WifiData2", "Size: ${wifiDataList.size}")

        // wifiDataList를 잘 가져왔는지 log를 통해 확인, 필요시 주석 처리 해도 됩니다.
        for (wifiData in wifiDataList) {
            Log.d("WifiData2", "${wifiData.name} Latitude: ${wifiData.latitude}, Longitude: ${wifiData.longitude}")
        }

        // 위치 권한 확인
        checkLocationPermission()

        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient("f5wddcflyd")

        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map2) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map2, it).commit()
            }
        mapFragment.getMapAsync(this)


        addWifi_Btn = findViewById(R.id.addWifi_Btn)
        close_Btn = findViewById(R.id.close_Btn)
        myWifi_name = findViewById(R.id.myWifi_name)

        enableEdgeToEdge()
    }

    // Wifi 데이터 파싱 함수
//    private fun parseWifiData(wifiData: String): Triple<String, Double, Double> {
//        val parts = wifiData.split("-")
//        val name = parts[0].toString()
//        val coordinates = parts[1].split(", ")
//        val latitude = coordinates[0].toDouble()
//        val longitude = coordinates[1].toDouble()
//        return Triple(name, latitude, longitude)
//    }

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


    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap // naverMap 변수 초기화

        naverMap.locationSource = FusedLocationSource(this, 1000)
        naverMap.locationTrackingMode = LocationTrackingMode.Follow // 위치 추적 모드를 Follow로 설정
        naverMap.uiSettings.isLocationButtonEnabled = true // 현위치 버튼

        // 현재 위치 위도, 경도 저장
        fetchCurrentLocation()

        for (wifiData in wifiDataList) {
            addMarkerToMap(wifiData.latitude, wifiData.longitude, wifiData.name)
        }

        lateinit var center : LatLng
        naverMap.addOnCameraChangeListener { _, _ ->
            // 중심 좌표 가져오기
            center = naverMap.cameraPosition.target
        }

        addWifi_Btn.setOnClickListener {
            showSaveConfirmationDialog(center)
            // 현재위치도 같이 확인
//            fetchCurrentLocation()
//            val currentLatitude = currentLatLng?.latitude ?: 0.0
//            val currentLongitude = currentLatLng?.longitude ?: 0.0
//            Log.d("FileContents", "Latitude: $currentLatitude, Longitude: $currentLongitude")
        }
        close_Btn.setOnClickListener {
            val intent = Intent(this@PlusActivity, SecondActivity::class.java)
            startActivity(intent)
        }
    }

    private fun addMarkerToMap(latitude: Double, longitude: Double, title: String) {
        val marker = Marker()
        marker.position = LatLng(latitude, longitude)
        marker.map = naverMap
        marker.captionText = title
    }

    // txt파일에 wifi정보 저장
    private fun saveToFile(wifiName: String, latitude: Double, longitude: Double) {
        val fileContent = "$wifiName - $latitude, $longitude\n"
        try {
            FileOutputStream(File(filesDir, FILENAME), true).use {
                it.write(fileContent.toByteArray())
                Toast.makeText(this, "WIFI저장을 완료했습니다.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Toast.makeText(this, "파일 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
            Log.e("FileIO", "Error writing to file $FILENAME: ${e.message}")
        }
    }

    // 내부 저장소 안에 wifi_data.txt의 내용을 확인하는 함수 (log를 통해)
    private fun printFileContents() {
        try {
            val file = File(filesDir, FILENAME)
            val reader = BufferedReader(FileReader(file))
            val stringBuilder = StringBuilder()

            var line: String? = reader.readLine()
            while (line != null) {
                stringBuilder.append(line).append("\n")
                line = reader.readLine()
            }
            reader.close()

            // 로그를 통해 txt의 내용을 알 수 있습니다
            Log.d("FileContents", stringBuilder.toString())
        } catch (e: IOException) {
            Log.e("FileIO", "Error reading file $FILENAME: ${e.message}")
        }
    }

    private fun showSaveConfirmationDialog(center : LatLng) {
        val wifiName = myWifi_name.text.toString()
        AlertDialog.Builder(this)
            .setTitle(wifiName)
            .setMessage("Wi-Fi 정보를 저장하시겠습니까?")
            .setPositiveButton("저장") { dialogInterface: DialogInterface, i: Int ->
                center?.let {
                    saveToFile(wifiName, it.latitude, it.longitude)
                }
                // 중심 위치를 잘 가져왔나 확인 용
                //            Toast.makeText(
//                this,
//                "지도 중심 위치: 위도 ${center.latitude}, 경도 ${center.longitude}",
//                Toast.LENGTH_SHORT
//            ).show()
                printFileContents() //log를 통해 내부 저장소의 txt내용을 알기 위한 함수이므로 필요 없으면 주석 처리 해도 됩니다.
                val intent = Intent(this@PlusActivity, SecondActivity::class.java)
                startActivity(intent)

            }
            .setNegativeButton("취소") { dialogInterface: DialogInterface, i: Int ->
                // 취소 버튼 클릭 시 아무 동작 없음
            }
            .show()
    }
}