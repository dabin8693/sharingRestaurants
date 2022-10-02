package com.project.sharingrestaurants.ui.on

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.gun0912.tedpermission.rx3.TedPermission
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.adapter.OnAdapter
import com.project.sharingrestaurants.custom.CustomDialog
import com.project.sharingrestaurants.databinding.FragOnlineMemoBinding
import com.project.sharingrestaurants.firebase.BoardEntity
import com.project.sharingrestaurants.firebase.FBLogin
import com.project.sharingrestaurants.ui.MainActivity
import com.project.sharingrestaurants.viewmodel.OnLineViewModel
import com.project.sharingrestaurants.viewmodel.ViewModelFactory

class FragmentOnLineMemo : Fragment() {
    //뷰페이저2 구현
    private val viewModel: OnLineViewModel by lazy {//프래그먼트 객체가 사라질때까지 유지
        ViewModelProvider(this, ViewModelFactory(MyApplication.REPOSITORY)).get(
            OnLineViewModel::class.java
        )
    }
    private lateinit var binding: FragOnlineMemoBinding
    private lateinit var onAdapter: OnAdapter
    private lateinit var item: BoardEntity
    private lateinit var itemList: List<BoardEntity>
    private lateinit var loginDialog: CustomDialog
    private lateinit var activity: MainActivity


    override fun onCreateView(//레이아웃 인플레이트 하는곳 //액티비티의 onstart랑 비슷하다
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("생명주기", "onCreateView")
        initStart(inflater, container, savedInstanceState)
        return binding.root
    }


    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {//뷰 초기화, livedata옵저버, recyclerview, viewpager2, adapter초기화
        super.onViewCreated(view, savedInstanceState)
        Log.d("생명주기", "onViewCreated")
        onAdapter = OnAdapter(
            {
                val intent =
                    Intent(activity, OnItemDetailShowActivity::class.java)//onClick
                intent.putExtra("BoardEntity", item)
                startActivity(intent)
            },
            viewModel.currentLatitude.value!!,
            viewModel.currentLongitude.value!!,
            viewModel.getStorageRef()
        )
        binding.recyclerViewOn.apply {
            this.adapter = onAdapter
            this.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            this.setHasFixedSize(true)//사이즈 측정이 필요없다 판단돼면 내부적으로 measure안한다
        }
        viewModel.getList().observe(viewLifecycleOwner) { list ->
            //최신순으로 초기화
            //실시간변경x 정렬 스피너 이벤트 여부 or 프레그먼트 최초 초기화 될때만 호출
            Log.d("리스트초기화", list.toString())
            onAdapter.setItems(list)
        }

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    fun updateList() {//livedata말고 코루틴으로 처리
        viewModel.getList().observe(viewLifecycleOwner) { list ->
            //최신순으로 초기화
            //실시간변경x 정렬 스피너 이벤트 여부 or 프레그먼트 최초 초기화 될때만 호출
            Log.d("리스트초기화", list.toString())
            onAdapter.setItems(list)
        }
    }

    private fun initStart(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) {
        binding = FragOnlineMemoBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.fragmentOn = this
        binding.lifecycleOwner = viewLifecycleOwner
        activity = requireActivity() as MainActivity

        requestPermissions()//위치 권한
        viewModel.currentLatitude.value = 0.0
        viewModel.currentLongitude.value = 0.0
        viewModel.getCurrentGPS(activity).observe(viewLifecycleOwner) {
            //위치정보도착하고 필터 거리순으로 클릭시 viewmodel에서 데이터 정렬후 onAdapter.setItems(list)
            viewModel.currentLatitude.value = it.latitude
            viewModel.currentLongitude.value = it.longitude
            if (onAdapter != null) {
                onAdapter.distChanged(it.latitude, it.longitude)
            }
        }

        if (viewModel.getIsLogin() == true) {
            Log.d("url값은ㅇ", viewModel.getAuth().photoUrl.value.toString())
            Glide.with(this)
                .load(viewModel.getAuth().photoUrl.value)//첫번째 사진만 보여준다
                .into(binding.imageView)
                .onLoadFailed(ResourcesCompat.getDrawable(resources, R.mipmap.ic_launcher, null))
        }

    }

    fun loginShow() {
        if (viewModel.getIsLogin() == false) {
            loginDialog = CustomDialog(activity)
            loginDialog.signOnClick {
                val signInIntent: Intent =
                    viewModel.getAuth().googleSignInClient!!.signInIntent //구글로그인 페이지로 가는 인텐트 객체

                startActivityForResult(signInIntent, 100) //Google Sign In flow 시작
            }
            loginDialog.finshOnclick { loginDialog.dismiss() }
            loginDialog.show()
        } else {//로그인 상태면 내정보창으로 이동
            activity.myShow()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 구글로그인 버튼 응답
        if (requestCode == 100) {
            // 구글로그인 버튼 응답
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // 구글 로그인 성공
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                viewModel.signIn(account, java.lang.ref.WeakReference(activity).get()) {
                    viewModel.addFBAuth(viewLifecycleOwner)//db회원 정보 저장 및 불러오기
                    Log.d("url값은", viewModel.getAuth().photoUrl.value.toString())
                    loginDialog.dismiss()
                    Glide.with(this)
                        .load(viewModel.getAuth().photoUrl.value)//첫번째 사진만 보여준다
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(binding.imageView)
                        .onLoadFailed(
                            ResourcesCompat.getDrawable(
                                resources,
                                R.mipmap.ic_launcher,
                                null
                            )
                        )

                }
            } catch (e: ApiException) {

            }
        }
    }


    // 위치권한 관련 요청
    private fun requestPermissions() {
        // 내장 위치 추적 기능 사용
        //locationSource =
        //FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        TedPermission.create()
            .setRationaleTitle("위치권한 요청")
            .setRationaleMessage("현재 위치로 이동하기 위해 위치권한이 필요합니다.") // "we need permission for read contact and find your location"
            .setPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            //rxandroid
            .request()
            .subscribe({ tedPermissionResult ->
                if (!tedPermissionResult.isGranted) {
                    Toast.makeText(
                        requireActivity(),
                        getString(R.string.location_permission_denied_msg),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }) { throwable -> Log.e("AAAAAA", throwable.message.toString()) }


    }
}


