package com.project.sharingrestaurants.ui.on

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.adapter.OnAdapter
import com.project.sharingrestaurants.adapter.OnDetailAdapter
import com.project.sharingrestaurants.databinding.ActivityOnItemDetailShowBinding
import com.project.sharingrestaurants.firebase.BoardEntity
import com.project.sharingrestaurants.firebase.FBLogin
import com.project.sharingrestaurants.viewmodel.OnDetailViewModel
import com.project.sharingrestaurants.viewmodel.OnLineViewModel
import com.project.sharingrestaurants.viewmodel.ViewModelFactory

class OnItemDetailShowActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnItemDetailShowBinding
    private lateinit var adapter: OnDetailAdapter
    val viewModel: OnDetailViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory(MyApplication.REPOSITORY)).get(
            OnDetailViewModel::class.java
        )
    }
    lateinit var item: BoardEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initStart()
        item = intent.getSerializableExtra("BoardEntity") as BoardEntity

        adapter = OnDetailAdapter(item)
        binding.recycle.apply {
            this.adapter = adapter
            this.layoutManager =
                LinearLayoutManager(this@OnItemDetailShowActivity, RecyclerView.VERTICAL, false)
            this.setHasFixedSize(true)//사이즈 측정이 필요없다 판단돼면 내부적으로 measure안한다
        }

    }

    private fun initStart(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_on_item_detail_show)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

    }
}