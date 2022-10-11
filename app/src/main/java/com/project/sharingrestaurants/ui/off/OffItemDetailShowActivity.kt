package com.project.sharingrestaurants.ui.off

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.sharingrestaurants.LifecycleTest
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.adapter.OffDetailAdapter
import com.project.sharingrestaurants.databinding.ActivityOffItemDetailShowBinding
import com.project.sharingrestaurants.room.ItemEntity
import com.project.sharingrestaurants.util.DataTrans
import com.project.sharingrestaurants.viewmodel.*

class OffItemDetailShowActivity : AppCompatActivity() {
    val viewModel: OffDetailViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory(MyApplication.REPOSITORY)).get(
            OffDetailViewModel::class.java
        )
    }
    lateinit var binding: ActivityOffItemDetailShowBinding
    lateinit var adapter: OffDetailAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getLifecycle().addObserver(LifecycleTest("activityOffDetail"))
        ininStart()
        viewModel.item = intent.getSerializableExtra("ItemEntity") as ItemEntity
        viewModel.position = intent.getIntExtra("position",10000)

        adapter = OffDetailAdapter()

        binding.recycle.adapter = adapter
        binding.recycle.layoutManager =
            LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)//기본값 VERTICAL
        binding.recycle.setHasFixedSize(true)
        viewModel.newItem = DataTrans.itemTrans(viewModel.item)
        adapter.setItem(viewModel.newItem)

        binding.back.setOnClickListener{finish()}
        binding.write.setOnClickListener{
            intent = Intent(this, OffItemAddActivity::class.java)
            intent.putExtra("OffDetailItem", viewModel.newItem)
            startActivity(intent)
        }
    }

    override fun onRestart() {
        super.onRestart()
        viewModel.getList().observe(this){
            viewModel.item = it.get(viewModel.position)
            viewModel.newItem = DataTrans.itemTrans(viewModel.item)
            adapter.setItem(viewModel.newItem)
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun ininStart() {

        binding = DataBindingUtil.setContentView(this, R.layout.activity_off_item_detail_show)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

}