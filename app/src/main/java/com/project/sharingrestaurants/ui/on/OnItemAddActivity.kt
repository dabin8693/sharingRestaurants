package com.project.sharingrestaurants.ui.on

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.adapter.OnAdapter
import com.project.sharingrestaurants.adapter.OnAddAdapter
import com.project.sharingrestaurants.databinding.ActivityMainBinding
import com.project.sharingrestaurants.databinding.ActivityOffItemAddBinding
import com.project.sharingrestaurants.databinding.ActivityOnItemAddBinding
import com.project.sharingrestaurants.viewmodel.MainViewModel
import com.project.sharingrestaurants.viewmodel.OnAddViewModel
import com.project.sharingrestaurants.viewmodel.ViewModelFactory

class OnItemAddActivity : AppCompatActivity() {
    lateinit var binding: ActivityOnItemAddBinding
    lateinit var Adapter: OnAddAdapter
    val viewModel: OnAddViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory(MyApplication.REPOSITORY)).get(
            OnAddViewModel::class.java
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initStart()

        Adapter = OnAddAdapter{text, position ->//position은 중간중간 사진을 뺐다
            viewModel.textList.set(position,text)//textlist사이즈는 사진추가될때마다 추가
        }
    }

    private fun initStart(){

        binding = DataBindingUtil.setContentView(this, R.layout.activity_off_item_add)//binding.viewModel에 viewmodel 담기전에 먼저 초기화
        binding.viewModel = viewModel //xml에서 main뷰모델 데이터에 접근 가능하게
        binding.lifecycleOwner = this //이거 안쓰면 데이터바인딩 쓸때 xml이 데이터 관측 못 함


    }
}