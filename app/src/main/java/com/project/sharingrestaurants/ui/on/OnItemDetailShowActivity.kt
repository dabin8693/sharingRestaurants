package com.project.sharingrestaurants.ui.on

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.databinding.ActivityOnItemDetailShowBinding
import com.project.sharingrestaurants.firebase.BoardEntity
import com.project.sharingrestaurants.firebase.FBLogin
import com.project.sharingrestaurants.viewmodel.OnDetailViewModel
import com.project.sharingrestaurants.viewmodel.OnLineViewModel
import com.project.sharingrestaurants.viewmodel.ViewModelFactory

class OnItemDetailShowActivity : AppCompatActivity() {
    lateinit var binding: ActivityOnItemDetailShowBinding
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
        binding.title.setText("수원역 죠스떡볶이 맛집추천")
        binding.locate.setText("대한민국 경기도 수원시 팔달구 덕영대로 923-10")
        binding.place.setText("6번출구 앞 죠스떡볶이")
        binding.rating.rating = 5F
        Glide.with(this)
            .load(viewModel.getAuth().photoUrl.value)//첫번째 사진만 보여준다
            .override(100,100)
            .into(binding.profileimage)
        //binding.image.setImageURI(item.image.toUri())
        binding.profilenickname.setText("미식가")
        binding.body.setText("굳굳")
        binding.recommend.setText("2")
        binding.recomments.setText("0")
    }

    private fun initStart(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_on_item_detail_show)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

    }
}