package com.project.sharingrestaurants.ui.off

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.databinding.ActivityOffItemDetailShowBinding
import com.project.sharingrestaurants.viewmodel.OffLineViewModel

class OffItemDetailShowActivity : AppCompatActivity() {
    val viewmodel: OffLineViewModel by viewModels<OffLineViewModel>()
    lateinit var binding: ActivityOffItemDetailShowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ininStart()

    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun ininStart() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_off_item_detail_show)
        binding.viewModel = viewmodel
        binding.lifecycleOwner = this
    }
}