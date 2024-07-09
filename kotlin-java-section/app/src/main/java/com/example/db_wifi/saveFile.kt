//// -------------------------------
//// 예시로 데이터 생성
//val latitude = 127.442
//val longitude = 53.223
//val description = "Example marker"
//
//// 서버에 전송할 데이터 객체 생성
//val markerData = MarkerData(latitude, longitude, description)
//
//// Retrofit 서비스 객체를 사용하여 서버에 데이터 전송
//RetrofitClient.markerApiService.addMarker(markerData)
//.enqueue(object : Callback<Void> {
//    override fun onResponse(call: Call<Void>, response: Response<Void>) {
//        if (response.isSuccessful) {
//            Log.d("Marker", "Marker added successfully")
//        } else {
//            Log.e("Marker", "Failed to add marker")
//        }
//    }
//
//    override fun onFailure(call: Call<Void>, t: Throwable) {
//        Log.e("Marker", "Error adding marker: ${t.message}")
//    }
//})
//// -------------------------------