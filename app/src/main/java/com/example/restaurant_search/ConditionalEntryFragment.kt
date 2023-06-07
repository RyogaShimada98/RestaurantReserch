package com.example.restaurant_search

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar

/**検索入力画面*/

class ConditionalEntryFragment : Fragment(R.layout.fragment_conditional_entry) {

    //検索条件での選択肢(Spinnerに登録する。)
    private val rangeSpinnerItem = arrayOf(
        "300m以内" to 1,
        "500m以内" to 2,
        "1000m以内" to 3,
        "2000m以内" to 4,
        "3000m以内" to 5
    )
    private val genreSpinnerItem = arrayOf(
        "未選択" to "",
        "居酒屋" to "G001",
        "ダイニングバー・バル" to "G002",
        "創作料理" to "G003",
        "和食" to "G004",
        "洋食" to "G005",
        "イタリアン・フレンチ" to "G006",
        "中華" to "G007",
        "焼肉・ホルモン" to "G008",
        "アジア・エスニック料理" to "G009",
        "各国料理" to "G010",
        "カラオケ・パーティ" to "G011",
        "バー・カクテル" to "G012",
        "ラーメン" to "G013",
        "カフェ・スイーツ" to "G014",
        "その他グルメ" to "G015",
        "お好み焼き・もんじゃ" to "G016",
        "韓国料理" to "G017"
    )


    //ホットペッパーAPIのURLに使用。
    // 緯度・経度
    private var latitude = "0.0"
    private var longitude = "0.0"
    //検索範囲
    private var selectedRange: Int = 0
    //ジャンル
    private var selectedPrimegenre: String = ""
    private var selectedSubgenre: String = ""


    //位置情報取得の際に使用
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var requestingLocationUpdates = false


    // 位置情報へのアクセス許可を要求する。許可されたなら位置情報更新を可能にする。
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            requestingLocationUpdates = true
        } else {
            view?.let { Snackbar.make(it, "設定から位置情報を許可してください", Snackbar.LENGTH_LONG).show() }
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    /** ----------------------位置情報の取得------------------------*/
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            requestingLocationUpdates = true
        } else {
            requestPermissionLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                location?.let {
                    // 緯度の表示
                    latitude = it.latitude.toString()

                    // 経度の表示
                    longitude = it.longitude.toString()
                }
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        /** ----------------------------------------------------------*/

//        Spinnerを選択した際の処理
        rangeSpinner(rangeSpinnerItem, R.id.radiusSpinner)
        genreSpinner(genreSpinnerItem, R.id.primegenreSpinner,true)
       genreSpinner(genreSpinnerItem, R.id.subgenreSpinner,false)


//      ボタンをタップした際の処理
        val button: Button = view.findViewById(R.id.SearchButton)
        button.setOnClickListener {
            Log.d("selectrange", selectedRange.toString())
            Log.d("selectedPrimegenre",selectedPrimegenre)
            Log.d("selectedSubgenre",selectedSubgenre)

            // 画面遷移の処理
            val action =
                ConditionalEntryFragmentDirections.actionConditionalEntryFragmentToSearchResultsFragment(
                    range = selectedRange,
                    latitude = latitude,
                    longitude = longitude,
                    primaryGenre = selectedPrimegenre,
                    subGenre = selectedSubgenre
                )
            findNavController().navigate(action)
        }
    }


//    位置情報が許可されているか確認。許可されているなら、位置情報取得開始。
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback, Looper.getMainLooper())
        }else{
            return
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        Log.d("onResume","onResume")
        if (requestingLocationUpdates) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }


    // spinnerをタップしたときの処理
    private fun rangeSpinner(spinnerItems: Array<Pair<String, Int>>, spinnerID: Int){

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            spinnerItems.map { it.first }
        )
        val spinner: Spinner = requireView().findViewById(spinnerID)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedRange = spinnerItems[position].second
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 何も選択されなかった場合の処理
            }
        }
    }



      // 上記と重複していますが、引数の型が異なるため、記載。
    private fun genreSpinner(spinnerItems: Array<Pair<String, String>>, spinnerID: Int, isPrimary: Boolean) {


          val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
              requireContext(),
              android.R.layout.simple_spinner_item,
              spinnerItems.map { it.first }
          )

          val spinner: Spinner = requireView().findViewById(spinnerID)

          adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

          spinner.adapter = adapter

          spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
              override fun onItemSelected(
                  parent: AdapterView<*>,
                  view: View?,
                  position: Int,
                  id: Long
              ) {
                  if (isPrimary) {
                      selectedPrimegenre = spinnerItems[position].second
                  } else {
                      selectedSubgenre = spinnerItems[position].second
                  }
              }
          override fun onNothingSelected(parent: AdapterView<*>) {
              // 何も選択されなかった場合の処理
            }
        }
    }
}
