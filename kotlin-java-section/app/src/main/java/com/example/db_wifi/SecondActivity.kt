package com.example.db_wifi

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginRight
import com.google.android.material.internal.ViewUtils.dpToPx
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.Serializable



class SecondActivity : AppCompatActivity() {
    private lateinit var backMain_Btn : Button
    private lateinit var plus_Btn : Button
    private lateinit var delete_Btn : Button
    private lateinit var wifi_info : LinearLayout

    // 내부 저장소에 저장되어있는 와이파이의 위도와 경도
    private val wifiDataList = mutableListOf<WifiLocation>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.my_layout)

        backMain_Btn = findViewById(R.id.backMain_Btn)
        plus_Btn = findViewById(R.id.plusBtn)
        delete_Btn = findViewById(R.id.delete_Btn)
        wifi_info = findViewById(R.id.wifi_info)

        delete_Btn.visibility = View.INVISIBLE
        delete_Btn.isClickable = false

        // 내부 저장소 wifi_data.txt에 있는 정보를 scrollView에 추가
        readFromFile()

        backMain_Btn.setOnClickListener {
            val intent = Intent(this@SecondActivity, MainActivity::class.java)
            startActivity(intent)
        }
        plus_Btn.setOnClickListener {
            val intent = Intent(this@SecondActivity, PlusActivity::class.java)
            intent.putExtra("wifiDataList", ArrayList(wifiDataList))
            startActivity(intent)

            // Plus_Btn클릭 시 wifiDataList에 있는 각 데이터들을 log를 통해 확인 할 수 있습니다
            Log.d("WifiData", "wifiDataList contents:")
            wifiDataList.forEach { Log.d("WifiData", "$it") }
        }
        delete_Btn.setOnClickListener {
            deleteCheckedLinearLayouts()
        }
    }

    private fun readFromFile() {
        try {
            val inputStream = openFileInput("wifi_data.txt")
            val inputStreamReader = inputStream.reader()
            val bufferedReader = BufferedReader(inputStreamReader)
            var line: String? = bufferedReader.readLine()
            while (line != null) {

                val wifiLatLng = extractWifiLocationFromLine(line)
                wifiDataList.add(wifiLatLng)

                addLinearLayout(line)
                line = bufferedReader.readLine()
            }
            bufferedReader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // txt의 한 라인을 이름과 위도, 경도로 분리한 뒤 WifiLocation이라는 data로 바꾸는 함수
    private fun extractWifiLocationFromLine(line: String): WifiLocation {
        // '-'를 기준으로 이름과 좌표를 분리
        val parts = line.split(" - ")
        // 첫 번째 요소가 이름
        val name = parts[0]
        val coordinates = parts[1].split(", ")
        val latitude = coordinates[0].toDouble()
        val longitude = coordinates[1].toDouble()

        return WifiLocation(name, latitude, longitude)
    }

    @SuppressLint("RestrictedApi")
    private fun addLinearLayout(text: String) {
        // 각각 할당되는 LinearLayout 생성 (여기에 wifi 정보랑 버튼들이 있습니다)
        // newLinearLayout에 TextView와 button_layout이 있고,
        // TextView에는 wifi의 정보, button_layout에는 버튼들과 체크박스가 있습니다
        val newLinearLayout = LinearLayout(this)
        newLinearLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        newLinearLayout.orientation = LinearLayout.VERTICAL

        // TextView 생성
        val textView = TextView(this)
        textView.text = text
        textView.setTextColor(Color.BLACK)
        textView.textSize = 18f // TypedValue 사용 없이 직접 설정
        textView.setPadding(20, 20, 20, 20)

        // LinearLayout에 TextView 추가
        newLinearLayout.addView(textView)

        // 배경 drawable 설정
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            newLinearLayout.background = resources.getDrawable(R.drawable.linear_layout_border, null)
        } else {
            newLinearLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.linear_layout_border))
        }

        // 버튼들이 있을 layout 생성
        val button_layout = LinearLayout(this)
        button_layout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        button_layout.orientation = LinearLayout.HORIZONTAL
        button_layout.gravity = Gravity.END // 오른쪽 정렬

        // 편집 버튼 생성
        val editButton = Button(this)
        editButton.text = "편집" // 후에 그림으로 바꾸면 좋을 것 같슴다
        editButton.setOnClickListener {
            EditWifiLocation(newLinearLayout)
        }

        // 지도 버튼 생성
        val mapButton = Button(this)
        mapButton.text = "지도"
        mapButton.setOnClickListener {
            Toast.makeText(this, "지도 버튼 클릭됨", Toast.LENGTH_SHORT).show()
        }

        val checkBox = CheckBox(this)
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 체크됐을 때 밝은 회색 배경으로 변경
                newLinearLayout.setBackgroundColor(Color.LTGRAY)

                delete_Btn.visibility = View.VISIBLE
                delete_Btn.isClickable = true
            } else {
                // 체크 해제됐을 때 원래 배경색으로 변경
                newLinearLayout.setBackgroundResource(R.drawable.linear_layout_border)

                checkDeleteButtonVisibility()
            }
        }
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 470, 0) // 세 번째 값을 늘릴 수록 체크박스와 버튼의 거리가 멀어집니다

        checkBox.layoutParams = params

        button_layout.addView(checkBox)
        button_layout.addView(mapButton)
        button_layout.addView(editButton)

        newLinearLayout.addView(button_layout)

        // 기존의 wifi_info LinearLayout에 새로운 LinearLayout 추가
        wifi_info.addView(newLinearLayout)
    }

    private fun checkDeleteButtonVisibility() {
        // 모든 체크박스의 체크 상태를 확인하여 delete_Btn을 감추거나 보이게 설정
        var anyChecked = false
        for (i in 0 until wifi_info.childCount) {
            val linearLayout = wifi_info.getChildAt(i) as LinearLayout
            val buttonLayout = linearLayout.getChildAt(1) as LinearLayout // 첫 번째(0)는 TextView, 두 번째(1)는 ButtonLayout 입니다
            val checkBox = buttonLayout.getChildAt(0) as CheckBox

            if (checkBox.isChecked) {
                anyChecked = true
                break
            }
        }

        if (anyChecked) {
            delete_Btn.visibility = View.VISIBLE
            delete_Btn.isClickable = true
        } else {
            delete_Btn.visibility = View.INVISIBLE
            delete_Btn.isClickable = false
        }
    }

    private fun deleteCheckedLinearLayouts() {
        val checkedIndexes = mutableListOf<Int>()

        // 체크된 LinearLayout의 인덱스를 찾음
        for (i in 0 until wifi_info.childCount) {
            val linearLayout = wifi_info.getChildAt(i) as LinearLayout
            val buttonLayout = linearLayout.getChildAt(1) as LinearLayout
            val checkBox = buttonLayout.getChildAt(0) as CheckBox

            if (checkBox.isChecked) {
                checkedIndexes.add(i)
            }
        }

        // 인덱스를 역순으로 삭제 (앞에서부터 삭제하면 인덱스가 밀리므로)
        for (index in checkedIndexes.reversed()) {
            wifi_info.removeViewAt(index)
            // 데이터 목록에서도 삭제
            wifiDataList.removeAt(index)

            // txt파일 에서도 삭제
            deleteLineFromFile(index)
        }

        // 삭제 후 삭제 버튼 숨기기
        delete_Btn.visibility = View.INVISIBLE
        delete_Btn.isClickable = false
    }

    private fun deleteLineFromFile(index: Int) {
        try {
            val file = File(filesDir, "wifi_data.txt")
            val tempFile = File(filesDir, "temp_wifi_data.txt")

            val bufferedReader = BufferedReader(FileReader(file))
            val bufferedWriter = BufferedWriter(FileWriter(tempFile))

            var line: String? = bufferedReader.readLine()
            var lineNumber = 0

            while (line != null) {
                // 인덱스에 해당하는 라인을 건너뛰고 나머지를 temp 파일에 쓴다
                if (lineNumber != index) {
                    bufferedWriter.write(line)
                    bufferedWriter.newLine()
                }

                lineNumber++
                line = bufferedReader.readLine()
            }

            bufferedWriter.close()
            bufferedReader.close()

            // 기존 파일을 삭제하고, 임시 파일을 기존 파일 이름으로 변경
            if (!file.delete()) {
                Log.d("Delete Line", "Could not delete file")
                return
            }

            if (!tempFile.renameTo(file)) {
                Log.d("Delete Line", "Could not rename file")
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun EditWifiLocation(linearLayout: LinearLayout) {
        val index = wifi_info.indexOfChild(linearLayout)
        if (index != -1) {
            val view = linearLayout.getChildAt(0) as TextView
            val text = view.text.toString()

            val EditWifi = extractWifiLocationFromLine(text)
            val intent = Intent(this@SecondActivity, EditActivity::class.java)
            intent.putExtra("EditWifi", EditWifi)
            intent.putExtra("LineIndex", index) // wifi_info의 인덱스를 넣습니다
            Log.d("EditWifi1", "${EditWifi.name}, ${EditWifi.latitude}, ${EditWifi.longitude} $index")
            startActivity(intent)
        } else {
            Log.e("EditWifiLocation", "LinearLayout not found in wifi_info")
        }
    }

}