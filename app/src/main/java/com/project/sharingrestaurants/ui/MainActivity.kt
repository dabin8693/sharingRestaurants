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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.databinding.ActivityMainBinding
import com.project.sharingrestaurants.ui.off.FragmentOffLineMemo
import com.project.sharingrestaurants.ui.off.OffItemAddActivity
import com.project.sharingrestaurants.ui.on.FragmentOnLineMemo
import com.project.sharingrestaurants.ui.on.OnItemAddActivity
import com.project.sharingrestaurants.viewmodel.MainViewModel
import com.project.sharingrestaurants.viewmodel.ViewModelFactory


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory(MyApplication.REPOSITORY)).get(
            MainViewModel::class.java
        )
    }
    private lateinit var transaction: FragmentTransaction
    private lateinit var fragOff: FragmentOffLineMemo
    private lateinit var fragOn: FragmentOnLineMemo
    private lateinit var fragUser: FragmentUser
    private lateinit var addCallBack: ActivityResultLauncher<Intent>

    override fun onStart() {
        super.onStart()
        Log.d("초기화 온스타트", "ㅇㅇ")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initStart()

        transaction = supportFragmentManager.beginTransaction()
        fragOff = FragmentOffLineMemo()
        fragOn = FragmentOnLineMemo()
        fragUser = FragmentUser()
        transaction.replace(R.id.Fragcontainer, fragOff, "Off")
        //transaction.addToBackStack(null)
        transaction.commit()//비동기 적용
        addCallBack = registerForActivityResult(//onresume이전에 콜백이 등록되어야 된다.
            ActivityResultContracts.StartActivityForResult()//콜백함수를 하나로 통일하면 누가 호출했는지 구분을 못 함
        ) {
            Log.d("콜백1","ㄴㅇㄹ")
            fragOn.updateList()
        }
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("리스타트 ㅁㅁ", "ㄴㅇㄹ")

    }

    override fun onDestroy() {
        super.onDestroy()

    }


    override fun onBackPressed() {
        //super.onBackPressed()//빽키 앱 종료 안되게

        if (supportFragmentManager.findFragmentById(R.id.Fragcontainer) == fragOff) {//백스택 사용안해서 getFragments는 한개 밖에 없다.
            finish()
        }
        transaction = supportFragmentManager.beginTransaction() //commit할때마다 다시 호출해야됨
        transaction.replace(R.id.Fragcontainer, fragOff, "Off")
        //transaction.addToBackStack(null)
        transaction.commit()
        viewModel.setOffDrawable(this)
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun initStart() {

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )//binding.viewModel에 viewmodel 담기전에 먼저 초기화
        binding.viewModel = viewModel //xml에서 main뷰모델 데이터에 접근 가능하게
        binding.mainActivity = this //xml에서 main액티비티 데이터에 접근 가능하게
        binding.lifecycleOwner = this //이거 안쓰면 데이터바인딩 쓸때 xml이 데이터 관측 못 함

        viewModel.setOffDrawable(this)

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    fun offShow() {
        transaction = supportFragmentManager.beginTransaction() //commit할때마다 다시 호출해야됨
        transaction.replace(R.id.Fragcontainer, fragOff, "Off")
        //transaction.addToBackStack(null)
        transaction.commit()
        viewModel.setOffDrawable(this)
    }

    fun onShow() {
        if(supportFragmentManager.findFragmentById(R.id.Fragcontainer) != fragOn) {
            transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.Fragcontainer, fragOn, "On")
            //transaction.addToBackStack(null)
            transaction.commit()
        }else{
            fragOn.updateList()
        }
        viewModel.setOnDrawable(this)
    }

    fun upShow() {
        //추천글 보는 프래그먼트
        //FBAuth.signOut()
        //val auth: AuthEntity = AuthEntity("구글이메일","닉네임",DataTrans.getTime())
        //val board: BoardEntity = BoardEntity("구글이메일1",DataTrans.getTime(),"제목","치킨집","경기도 수원시", 3.5F, "본문1","이미지주소",3,5,10.5,20.7)
        //val comment: CommentEntity = CommentEntity("구글이메일2","202201","202202",DataTrans.getTime(),"댓글내용1")
        //FBDatabase.setAuth(auth)
        //FBDatabase.setBoard(board)
        //FBDatabase.setComment(comment)
        viewModel.setUpDrawable(this)
    }

    fun myShow() {
        if (viewModel.getIsLogin()) {//로그인 상태
            transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.Fragcontainer, fragUser, "User")
            //transaction.addToBackStack(null)
            transaction.commit()
            viewModel.setMyDrawable(this)
        } else {
            if (supportFragmentManager.findFragmentById(R.id.Fragcontainer) == fragOff) {
                fragOff.loginShow()
            } else if (supportFragmentManager.findFragmentById(R.id.Fragcontainer) == fragOn) {
                fragOn.loginShow()
            }

        }
    }

    fun onAdd() {
        if (supportFragmentManager.findFragmentById(R.id.Fragcontainer) == fragOff) {
            val intent = Intent(this, OffItemAddActivity::class.java)
            startActivity(intent)
        } else if (supportFragmentManager.findFragmentById(R.id.Fragcontainer) == fragOn) {
            val intent = Intent(this, OnItemAddActivity::class.java)
            addCallBack.launch(intent)
        } else {

        }
    }


}