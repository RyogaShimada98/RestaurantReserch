package com.example.restaurant_search

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import coil.load
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import androidx.navigation.fragment.findNavController
import kotlinx.serialization.Contextual


/**検索結果一覧画面*/

@Serializable
data class HotPepperResponse(
    val results: Results
) {
    @Serializable
    data class Results(
        val shop: List<Shop>,
        val results_available:Int
    ) {
        @Serializable
        data class Shop(
            val id: String,
            val name: String,
            val address: String,
            val logo_image: String,
            val mobile_access: String,
            val open: String,
            val close: String,
            val genre:Genre,
            val sub_genre: SubGenre? = null,
            val photo: Photo
        ) {
            @Serializable
            data class Genre(
                val name: String
            )
            @Serializable
            data class SubGenre(
                val name: String
            )

            @Serializable
            data class Photo(
                val mobile: Mobile
            ) {
                @Serializable
                data class Mobile(
                    val l: String
                )
            }
        }
    }
}


interface HotPepperApiService {
    @GET("hotpepper/gourmet/v1/")
    suspend fun getShops(
        @Query("key") key: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("range") range: Int,
        @Query("genre") primaryGenre:String,
        @Query("genre") subGenre:String,
        @Query("order") order: Int,
        @Query("count") count: Int,
        @Query("format") format: String
    ): HotPepperResponse
}



class SearchResultsFragment : Fragment(R.layout.fragment_search_results) {


    companion object{
        private val key = "8d1c4417905a0e4d"
        private val order = 4
        private val count = 30
        private val format = "json"
    }

    private lateinit var client: OkHttpClient
    private lateinit var retrofit: Retrofit
    private lateinit var service: HotPepperApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        client = OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl("https://webservice.recruit.co.jp/")
            .addConverterFactory(Json {
                ignoreUnknownKeys = true
            }.asConverterFactory("application/json".toMediaType()))
            .client(client) // OkHttp clientを追加
            .build()
        service = retrofit.create(HotPepperApiService::class.java)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // データが格納されているargsを取得
        val args:SearchResultsFragmentArgs by navArgs()

        val range = args.range
        val latitude = args.latitude.toDouble()
        val longitude = args.longitude.toDouble()
        val primaryGenre = args.primaryGenre
        val subGenre = args.subGenre


        Log.d("_latitude", latitude.toString())
        Log.d("range", range.toString())
        Log.d("longitude", longitude.toString())

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // レスポンスから店舗情報を取得
                val response =
                    service.getShops(key, latitude, longitude, range ?: 0, primaryGenre,subGenre,order,count, format)

//                取得した情報を画面に表示。
                withContext(Dispatchers.Main) {
                    if (response.results.results_available != 0) {
                        val shops = response.results.shop
                        Log.d("shop", response.results.shop.toString())

                        val listView = view.findViewById<ListView>(R.id.list)

                        val adapter = ShopListAdapter(requireContext(), shops)
                        listView.adapter = adapter

//                        選択した店舗の詳細画面に遷移
                        listView.onItemClickListener =
                            object : AdapterView.OnItemClickListener {
                                override fun onItemClick(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {

                                    val shop = shops[position]

                                    val action =
                                        SearchResultsFragmentDirections.actionSearchResultsFragmentToStoreDetailsFragment(
                                            name = shop.name,
                                            address = shop.address,
                                            open = shop.open,
                                            photo = shop.photo.mobile.l,
                                            logoimage = shop.logo_image,
                                            genre = shop.genre.name,
                                            subGenre = shop.sub_genre?.name?: ""
                                        )
                                    findNavController().navigate(action)
                                }
                            }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "検索結果はありません", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }
            } catch (e: Exception) {
                // handle the exception
                Log.e("error", e.toString())
            }
        }
    }
    //テキストと写真をリストビューに表示するためのアダプター。
    private inner class ShopListAdapter(
        private val context: Context,
        private val shops: List<HotPepperResponse.Results.Shop>
    ) :
        ArrayAdapter<HotPepperResponse.Results.Shop>(context, 0, shops) {

        private val inflater = LayoutInflater.from(context)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: inflater.inflate(R.layout.list_item, parent, false)

            val shop = shops[position]

            val nameTextView = view.findViewById<TextView>(R.id.name)
            val mobile_accessTextView = view.findViewById<TextView>(R.id.mobile_access)
            val imageView = view.findViewById<ImageView>(R.id.image)

            nameTextView.text = shop.name
            mobile_accessTextView.text = shop.mobile_access
            imageView.load(shop.logo_image)

            return view
        }
    }
}



