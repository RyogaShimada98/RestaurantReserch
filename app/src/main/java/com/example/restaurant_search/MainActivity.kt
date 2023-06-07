package com.example.restaurant_search

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*


class MainActivity : AppCompatActivity() {

    /**
     * 確認すべきこと。①エミュレータのgoogle_play関係のバージョンが古いかも。
     * 実機のバージョンで古いどうか確かめてみて、問題なければそっちを開発環境にする
     * ②今回のみ位置情報をokを選ぶとエラー起こる？？？？？
     *pixel 5XのAPI30使ったら、位置情報を取得できない。log見たら、エラーがおこってる。
     */


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}


