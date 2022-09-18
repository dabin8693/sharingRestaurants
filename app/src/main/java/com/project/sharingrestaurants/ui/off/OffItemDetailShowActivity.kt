package com.project.sharingrestaurants.ui.off

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.adapter.OffAdapter
import com.project.sharingrestaurants.adapter.OffDetailAdapter
import com.project.sharingrestaurants.data.OffDetailItem
import com.project.sharingrestaurants.databinding.ActivityOffItemDetailShowBinding
import com.project.sharingrestaurants.room.ItemEntity
import com.project.sharingrestaurants.util.DataTrans
import com.project.sharingrestaurants.viewmodel.OffDetailViewModel
import com.project.sharingrestaurants.viewmodel.OffLineViewModel

class OffItemDetailShowActivity : AppCompatActivity() {
    val viewmodel: OffDetailViewModel by viewModels<OffDetailViewModel>()
    lateinit var binding: ActivityOffItemDetailShowBinding
    lateinit var adapter: OffDetailAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()!!.hide() //액션바 숨기기
        ininStart()
        viewmodel.item = intent.getSerializableExtra("ItemEntity") as ItemEntity
        viewmodel.position = intent.getIntExtra("position",10000)

        adapter = OffDetailAdapter()

        binding.recycle.adapter = adapter
        binding.recycle.layoutManager =
            LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)//기본값 VERTICAL
        binding.recycle.setHasFixedSize(true)
        viewmodel.newItem = DataTrans.itemTrans(viewmodel.item)
        adapter.setItem(viewmodel.newItem)

        binding.back.setOnClickListener{finish()}
        binding.write.setOnClickListener{
            intent = Intent(this, OffItemAddActivity::class.java)
            intent.putExtra("OffDetailItem", viewmodel.newItem)
            startActivity(intent)
        }
    }

    override fun onRestart() {
        super.onRestart()
        viewmodel.getList().observe(this){
            viewmodel.item = it.get(viewmodel.position)
            viewmodel.newItem = DataTrans.itemTrans(viewmodel.item)
            adapter.setItem(viewmodel.newItem)
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun ininStart() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_off_item_detail_show)
        binding.viewModel = viewmodel
        binding.lifecycleOwner = this
    }

}