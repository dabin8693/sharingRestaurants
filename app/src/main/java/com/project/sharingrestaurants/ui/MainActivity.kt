package com.project.sharingrestaurants.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.custom.CustomDialog
import com.project.sharingrestaurants.data.BitmapImageItem
import com.project.sharingrestaurants.databinding.ActivityMainBinding
import com.project.sharingrestaurants.firebase.*
import com.project.sharingrestaurants.ui.off.FragmentOffLineMemo
import com.project.sharingrestaurants.ui.off.OffItemAddActivity
import com.project.sharingrestaurants.ui.on.FragmentOnLineMemo
import com.project.sharingrestaurants.ui.on.OnItemAddActivity
import com.project.sharingrestaurants.util.CameraWork
import com.project.sharingrestaurants.util.DataTrans

import com.project.sharingrestaurants.viewmodel.MainViewModel
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    val mainViewModel: MainViewModel by viewModels<MainViewModel>()
    lateinit var transaction: FragmentTransaction
    lateinit var fragOff: FragmentOffLineMemo
    lateinit var fragOn: FragmentOnLineMemo
    lateinit var fragUser: FragmentUser
    //lateinit var shared: SharedPreferences

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

        //shared = getSharedPreferences("temp",Context.MODE_PRIVATE)
        if (mainViewModel.getAuth().currentUser != null) {
            for (profile in mainViewModel.getAuth().currentUser!!.providerData) {
                Log.d("auth00Number","1")
                mainViewModel.getAuth().photoUrl.value = profile.photoUrl
            }

            //shared.getString("userPicture", "")!!.toUri()
            //Log.d("저장된uri:", shared.getString("userPicture", "")!!)
        }
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
        //val auth: AuthEntity = AuthEntity("구글이메일","닉네임",DataTrans.getTime())
        //val board: BoardEntity = BoardEntity("구글이메일1",DataTrans.getTime(),"제목","치킨집","경기도 수원시", 3.5F, "본문1","이미지주소",3,5,10.5,20.7)
        //val comment: CommentEntity = CommentEntity("구글이메일2","202201","202202",DataTrans.getTime(),"댓글내용1")
        //FBDatabase.setAuth(auth)
        //FBDatabase.setBoard(board)
        //FBDatabase.setComment(comment)
    }
    fun myShow(){
        if (mainViewModel.getAuth().currentUser != null) {
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
            val intent = Intent(this, OnItemAddActivity::class.java)
            startActivity(intent)
        /*
            mainViewModel.addFBBoard(hashMapOf("documentId" to "", "timestamp" to FieldValue.serverTimestamp(), "userID" to "daf", "tilte" to "sdf", "place" to "xxx",
                "locate" to "s", "priority" to 3.5f, "body" to "qwer", "image" to "xcv", "recommends" to 5, "latitude" to 3.2, "longitude" to 1.1))

         */
        }else{

        }
    }



}