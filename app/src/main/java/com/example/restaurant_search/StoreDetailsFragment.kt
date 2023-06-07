package com.example.restaurant_search

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.navArgs
import coil.load

/**
 * 店舗の詳細画面
 */
class StoreDetailsFragment : Fragment(R.layout.fragment_store_details) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: StoreDetailsFragmentArgs by navArgs()
        Log.d("args", args.toString())
        val name = args.name
        val address = args.address
        val open = args.open
        val photo = args.photo
        val logo_image = args.logoimage
        val genre = args.genre
        val subGenre = args.subGenre

//        画面に埋め込む
        val nameTextView = view.findViewById<TextView>(R.id.shop_name)
        val addressTextView = view.findViewById<TextView>(R.id.address)
        val openTextView = view.findViewById<TextView>(R.id.open)
        val genreTextview = view.findViewById<TextView>(R.id.genre)
        val subGenreTextview = view.findViewById<TextView>(R.id.sub_genre)
        val photoView = view.findViewById<ImageView>(R.id.food_image)
        val logo_View = view.findViewById<ImageView>(R.id.logo_image)


        nameTextView.text = name
        addressTextView.text = address
        openTextView.text = open
        genreTextview.text = genre
        subGenreTextview.text = subGenre
        photoView.load(photo)
        logo_View.load(logo_image)

    }
}