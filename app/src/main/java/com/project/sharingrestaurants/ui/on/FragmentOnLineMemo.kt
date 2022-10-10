package com.project.sharingrestaurants.ui.on

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.adapter.OnAdapter
import com.project.sharingrestaurants.custom.LoginDialog
import com.project.sharingrestaurants.databinding.FragOnlineMemoBinding
import com.project.sharingrestaurants.ui.MainActivity
import com.project.sharingrestaurants.util.RunTimePermissionCheck
import com.project.sharingrestaurants.viewmodel.OnLineViewModel
import com.project.sharingrestaurants.viewmodel.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FragmentOnLineMemo : Fragment() {
    private val viewModel: OnLineViewModel by lazy {//main에서 프래그먼트 객체 = null되기전까지 유지됨 //ondestoy되어도 트랜젝션에서 replace되면 데이터 복구됨
        ViewModelProvider(this, ViewModelFactory(MyApplication.REPOSITORY)).get(
            OnLineViewModel::class.java
        )
    }
    private lateinit var binding: FragOnlineMemoBinding
    private lateinit var onAdapter: OnAdapter
    private lateinit var loginDialog: LoginDialog
    private lateinit var activity: MainActivity


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initStart(inflater, container, savedInstanceState)
        return binding.root
    }


    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {//뷰 초기화
        super.onViewCreated(view, savedInstanceState)
        onAdapter = OnAdapter(
            { documentId ->
                CoroutineScope(Dispatchers.Main).launch {
                    val item = viewModel.getBoard(documentId)
                    if (!item.documentId.equals("")) {
                        val intent =
                            Intent(activity, OnItemDetailShowActivity::class.java)//onClick
                        intent.putExtra("BoardEntity", item)
                        startActivity(intent)
                    }else{
                        //삭제된 글
                    }
                }
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
        viewModel.getList(viewLifecycleOwner).observe(viewLifecycleOwner) { list ->
            //최신순으로 초기화
            //실시간변경x 정렬 스피너 이벤트 여부 or 프레그먼트 최초 초기화 될때만 호출
            onAdapter.setItems(list)
        }

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    fun updateList() {
        viewModel.getList(viewLifecycleOwner).observe(viewLifecycleOwner) { list ->
            //최신순으로 초기화
            //실시간변경x 정렬 스피너 이벤트 여부 or 프레그먼트 최초 초기화 될때만 호출
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

        RunTimePermissionCheck.requestPermissions(activity)//위치 권한
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
            Glide.with(this)
                .load(viewModel.getAuth().profileImage)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.imageView)
                .onLoadFailed(ResourcesCompat.getDrawable(resources, R.mipmap.ic_launcher, null))
        }

    }

    fun loginShow() {
        if (viewModel.getIsLogin() == false) {
            loginDialog = LoginDialog(activity)
            loginDialog.signOnClick {
                val signInIntent: Intent =
                    viewModel.getGoogleSignInClient().signInIntent //구글로그인 페이지로 가는 인텐트 객체

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
                    viewModel.addFBAuth()//db회원 정보 저장 및 불러오기
                    loginDialog.dismiss()
                    Glide.with(this)
                        .load(viewModel.getAuth().profileImage)//첫번째 사진만 보여준다
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
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


}


