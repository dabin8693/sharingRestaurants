package com.project.sharingrestaurants.ui.on

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.project.sharingrestaurants.R

class OnItemAddActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()!!.hide() //액션바 숨기기
        setContentView(R.layout.activity_on_item_add)
    }
}