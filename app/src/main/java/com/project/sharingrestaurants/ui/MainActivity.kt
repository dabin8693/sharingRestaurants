package com.project.sharingrestaurants.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.custom.CustomDialog
import com.project.sharingrestaurants.databinding.ActivityMainBinding
import com.project.sharingrestaurants.firebase.FBAuth
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
    lateinit var fragUser: FragmentUser

    override fun onStart() {
        super.onStart()
        Log.d("초기화 온스타트","ㅇㅇ")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()!!.hide() //액션바 숨기기

        initStart()

        transaction = supportFragmentManager.beginTransaction()
        fragOff = FragmentOffLineMemo()
        fragOn = FragmentOnLineMemo()
        fragUser = FragmentUser()
        transaction.replace(R.id.Fragcontainer, fragOff, "Off")
        //transaction.addToBackStack(null)
        transaction.commit()//비동기 적용

    }

    override fun onDestroy() {
        super.onDestroy()
        FBAuth.referenceClear()
    }

    override fun onBackPressed() {
        //super.onBackPressed()//빽키 앱 종료 안되게

        if(supportFragmentManager.findFragmentById(R.id.Fragcontainer) == fragOff){//백스택 사용안해서 getFragments는 한개 밖에 없다.
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
        FBAuth.initialization(this)
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
    fun upShow(){
        //추천글 보는 프래그먼트
        //FBAuth.signOut()
    }
    fun myShow(){
        Log.d("로그인 여부",FBAuth.isLogin.value.toString())
        if (FBAuth.isLogin.value == true) {
            transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.Fragcontainer, fragUser, "User")
            //transaction.addToBackStack(null)
            transaction.commit()
        }else{
            if (supportFragmentManager.findFragmentById(R.id.Fragcontainer) == fragOff){
                fragOff.loginShow()
            }else if (supportFragmentManager.findFragmentById(R.id.Fragcontainer) == fragOn){
                fragOn.loginShow()
            }

        }
    }

    fun onAdd(){
        if(supportFragmentManager.findFragmentById(R.id.Fragcontainer) == fragOff) {
            val intent = Intent(this, OffItemAddActivity::class.java)
            startActivity(intent)
        }else if(supportFragmentManager.findFragmentById(R.id.Fragcontainer) == fragOn){

        }else{

        }
    }

}