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
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import com.example.db_wifi.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.overlay.Marker
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import kotlin.properties.Delegates


class EditActivity : FragmentActivity(), OnMapReadyCallback {

    // FusedLocationProviderClient는 manifest에서 위치권한 얻은 후 사용할 수 있습니다!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationSource: FusedLocationSource // 위치관련 클래스(타입)

    //위치 권한 요청을 위한 코드
    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 1000
    private lateinit var naverMap: NaverMap
    private var currentLatLng : LatLng? = null

    //버튼들
    private lateinit var editWifi_Btn : ImageButton
    private lateinit var close_Btn : ImageButton
    private lateinit var myWifi_name : EditText
    private lateinit var pw_Btn : ImageButton
    private lateinit var ps_text : EditText

    private lateinit var ps_bottom_sheet : BottomSheetBehavior<LinearLayout>

    // txt파일
    private val FILENAME: String = "wifi_data.txt"

    // intent를 통해 가져온 WifiLoelqjcation
    private lateinit var EditWifi : WifiLocation
    private var LineIndex by Delegates.notNull<Int>()

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

        EditWifi = intent.getSerializableExtra("EditWifi") as WifiLocation
        LineIndex = intent.getIntExtra("LineIndex", -1)
        // 잘 가져왔는지 확인
        Log.d("EditWifi", "${EditWifi.name}, ${EditWifi.latitude}, ${EditWifi.longitude}, ${EditWifi.password}, $LineIndex")


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


        editWifi_Btn = findViewById(R.id.addWifi_Btn)
        close_Btn = findViewById(R.id.close_Btn)
        myWifi_name = findViewById(R.id.myWifi_name)
        pw_Btn = findViewById(R.id.pw_Btn)
        ps_text = findViewById(R.id.ps_text)

        val bottomSheet = findViewById<LinearLayout>(R.id.ps_bottom_sheet)
        ps_bottom_sheet = BottomSheetBehavior.from(bottomSheet)
        ps_bottom_sheet.state = BottomSheetBehavior.STATE_HIDDEN

        ps_text.setText(EditWifi.password)
        myWifi_name.setText(EditWifi.name)

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

        addMarkerToMap(EditWifi.latitude, EditWifi.longitude, EditWifi.name)
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(EditWifi.latitude, EditWifi.longitude))
            .animate(CameraAnimation.None, 3000)
        naverMap.moveCamera(cameraUpdate)

        lateinit var center : LatLng
        naverMap.addOnCameraChangeListener { _, _ ->
            // 중심 좌표 가져오기
            center = naverMap.cameraPosition.target
        }

        editWifi_Btn.setOnClickListener {
            if (myWifi_name.text.isNullOrEmpty() || myWifi_name.text.isBlank()){
                Toast.makeText(this, "WIFI 이름을 입력하세요", Toast.LENGTH_SHORT).show()
            }
            else {
                showSaveConfirmationDialog(center)
            }
            // 현재위치도 같이 확인
//            fetchCurrentLocation()
//            val currentLatitude = currentLatLng?.latitude ?: 0.0
//            val currentLongitude = currentLatLng?.longitude ?: 0.0
//            Log.d("FileContents", "Latitude: $currentLatitude, Longitude: $currentLongitude")
        }
        close_Btn.setOnClickListener {
            val intent = Intent(this@EditActivity, SecondActivity::class.java)
            startActivity(intent)
        }
        pw_Btn.setOnClickListener {
            ps_bottom_sheet.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun addMarkerToMap(latitude: Double, longitude: Double, title: String) {
        val marker = Marker()
        marker.position = LatLng(latitude, longitude)
        marker.map = naverMap
        marker.captionText = title
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
        var password : String? = ""
        if(ps_text.text.isNullOrEmpty() || ps_text.text.isBlank()) {
            Log.d("psSetting", "1")
            password = "없음"
        } else {
            password = ps_text.text.toString()
            Log.d("psSetting", "222${password}")

        }
        AlertDialog.Builder(this)
            .setTitle("Wi-Fi 정보를 변경하시겠습니까?")
            .setMessage(wifiName)
            .setPositiveButton("변경") { dialogInterface: DialogInterface, i: Int ->
                center?.let {
                    // 기존 EidtWifi의 lat, lng를 center의 값으로 변경
                    val newWifiLocation = WifiLocation(wifiName, it.latitude, it.longitude, password)
                    EditWifi = newWifiLocation

                    // txt 파일 업데이트
                    updateFile()
                }
                printFileContents() //log를 통해 내부 저장소의 txt내용을 알기 위한 함수이므로 필요 없으면 주석 처리 해도 됩니다.
                val intent = Intent(this@EditActivity, SecondActivity::class.java)
                startActivity(intent)

            }
            .setNegativeButton("취소") { dialogInterface: DialogInterface, i: Int ->
                // 취소 버튼 클릭 시 아무 동작 없음
            }
            .show()
    }

    private fun updateFile() {
        try {
            val file = File(filesDir, FILENAME)
            val tempFile = File(filesDir, "temp_$FILENAME")

            val bufferedReader = BufferedReader(FileReader(file))
            val bufferedWriter = BufferedWriter(FileWriter(tempFile))

            var line: String? = bufferedReader.readLine()
            var lineNumber = 0

            while (line != null) {
                if (lineNumber == LineIndex) {
                    // 해당 라인 수정
                    val updatedLine = "${EditWifi.name} - ${EditWifi.latitude}, ${EditWifi.longitude} / ${EditWifi.password}"
                    bufferedWriter.write(updatedLine)
                } else {
                    // 기존 라인 그대로 쓰기
                    bufferedWriter.write(line)
                }
                bufferedWriter.newLine()

                lineNumber++
                line = bufferedReader.readLine()
            }

            bufferedWriter.close()
            bufferedReader.close()

            // 기존 파일 삭제 후 임시 파일을 기존 파일 이름으로 변경
            if (!file.delete()) {
                Log.d("Update File", "Could not delete file")
                return
            }

            if (!tempFile.renameTo(file)) {
                Log.d("Update File", "Could not rename file")
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}