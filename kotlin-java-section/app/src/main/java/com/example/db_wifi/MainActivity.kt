package com.example.db_wifi

import android.health.connect.datatypes.units.Length
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.clustering.Clusterer
import com.naver.maps.map.overlay.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var naverMapInfo: List<NaverMapData>? = null
    private var naverMapList: NaverMapItem? = null
    private var clusterer: Clusterer<ItemKey> = Clusterer.Builder<ItemKey>().screenDistance(20.0).build()

//    val distance: Clusterer<ItemKey> = Clusterer.Builder<ItemKey>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient("f5wddcflyd")

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

            ///////////////////선빈 작업 구간////////////////////////////////////////////////////////
            //전주 시청으로 카메라 시점 시작
            val cameraUpdate = CameraUpdate.scrollTo(LatLng(35.8247083, 127.147528))
            naverMap.moveCamera(cameraUpdate)


            // 마커 띄우는 곳!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            val marker = Marker()

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


//                        Toast.makeText(this@MainActivity, naverMapInfo?.get(1)?.address, Toast.LENGTH_LONG).show()

                        naverMapInfo?.let{
                            for(i in 0 until it.size){
//                                val markers = arrayOfNulls<Marker>(it.size)

//                                val marker = markers[i]
                                val lat = it.get(i).y
                                val lnt = it.get(i).x

                                marker.position = LatLng(lat, lnt)
                                //클러스터링
                                clusterer.add(ItemKey(i, LatLng(it.get(i).y, it.get(i).x)), null)

                            }
                        }
//                        marker.map = naverMap
                        clusterer.map = naverMap

                    }

                }
                override fun onFailure(call: Call<NaverMapItem>, t: Throwable) {
                    // 통신 실패 시 처리할 코드
                    Log.v("디버깅중", "실패!!!!!")
                }
            })
            ///////////////////////선빈 부분 ///////////////////////////////////


        }
}
