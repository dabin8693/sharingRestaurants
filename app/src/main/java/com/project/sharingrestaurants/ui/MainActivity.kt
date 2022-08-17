package com.project.sharingrestaurants.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.databinding.ActivityMainBinding
import com.project.sharingrestaurants.ui.off.FragmentOffLineMemo
import com.project.sharingrestaurants.ui.off.OffItemAddActivity
import com.project.sharingrestaurants.ui.on.FragmentOnLineMemo

import com.project.sharingrestaurants.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    val mainViewModel: MainViewModel by viewModels<MainViewModel>()
    lateinit var transaction: FragmentTransaction
    lateinit var fragOff: FragmentOffLineMemo
    lateinit var fragOn: FragmentOnLineMemo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()!!.hide() //액션바 숨기기

        initStart()

        transaction = supportFragmentManager.beginTransaction()
        fragOff = FragmentOffLineMemo()
        fragOn = FragmentOnLineMemo()
        transaction.replace(R.id.Fragcontainer, fragOff, "Off")
        //transaction.addToBackStack(null)
        transaction.commit()//비동기 적용

    }

    override fun onBackPressed() {
        //super.onBackPressed()//빽키 앱 종료 안되게
        if(getSupportFragmentManager().getFragments().get(0) == fragOff){//백스택 사용안해서 getFragments는 한개 밖에 없다.
            finish()
        }
        transaction = supportFragmentManager.beginTransaction() //commit할때마다 다시 호출해야됨
        transaction.replace(R.id.Fragcontainer, fragOff, "Off")
        //transaction.addToBackStack(null)
        transaction.commit()

    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun initStart(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)//binding.viewModel에 viewmodel 담기전에 먼저 초기화
        binding.viewModel = mainViewModel //xml에서 main뷰모델 데이터에 접근 가능하게
        binding.mainActivity = this //xml에서 main액티비티 데이터에 접근 가능하게
        binding.lifecycleOwner = this //이거 안쓰면 데이터바인딩 쓸때 xml이 데이터 관측 못 함
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    fun offShow(){
        transaction = supportFragmentManager.beginTransaction() //commit할때마다 다시 호출해야됨
        transaction.replace(R.id.Fragcontainer, fragOff, "Off")
        //transaction.addToBackStack(null)
        transaction.commit()
    }
    fun onShow(){
        transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.Fragcontainer, fragOn, "On")
        //transaction.addToBackStack(null)
        transaction.commit()
    }
    fun chatShow(){

    }
    fun myShow(){

    }
    fun onAdd(){
        if(getSupportFragmentManager().getFragments().get(0) == fragOff) {
            val intent = Intent(this, OffItemAddActivity::class.java)
            startActivity(intent)
        }else if(getSupportFragmentManager().getFragments().get(0) == fragOn){

        }else{

        }
    }
}