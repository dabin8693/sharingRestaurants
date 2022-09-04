package com.project.sharingrestaurants.ui.on

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.adapter.OffAdapter
import com.project.sharingrestaurants.adapter.OnAdapter
import com.project.sharingrestaurants.custom.CustomDialog
import com.project.sharingrestaurants.databinding.FragOfflineMemoBinding
import com.project.sharingrestaurants.databinding.FragOnlineMemoBinding
import com.project.sharingrestaurants.firebase.FBAuth
import com.project.sharingrestaurants.ui.MainActivity
import com.project.sharingrestaurants.ui.off.OffItemDetailShowActivity
import com.project.sharingrestaurants.viewmodel.OffLineViewModel
import com.project.sharingrestaurants.viewmodel.OnLineViewModel

class FragmentOnLineMemo : Fragment() {//뷰페이저2 구현
    val viewmodel: OnLineViewModel by lazy { ViewModelProvider(requireActivity()).get(OnLineViewModel::class.java) }
    lateinit var binding: FragOnlineMemoBinding
    lateinit var onAdapter: OnAdapter


    override fun onCreateView(//레이아웃 인플레이트 하는곳 //액티비티의 onstart랑 비슷하다
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initStart(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {//뷰 초기화, livedata옵저버, recyclerview, viewpager2, adapter초기화
        super.onViewCreated(view, savedInstanceState)
        Log.d("frag2","onViewCreated")
        binding.viewModel = viewmodel
        binding.lifecycleOwner = viewLifecycleOwner

        onAdapter = OnAdapter(viewmodel, viewLifecycleOwner)
        binding.recyclerViewOn.apply {
            this.adapter = onAdapter
            this.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL,false)
            this.setHasFixedSize(true)//사이즈 측정이 필요없다 판단돼면 내부적으로 measure안한다
        }

    }

    private fun initStart(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) {
        binding = FragOnlineMemoBinding.inflate(inflater, container, false)
        binding.viewModel = viewmodel
        binding.fragmentOn = this
        binding.lifecycleOwner = viewLifecycleOwner
        if (FBAuth.isLogin.value == true){
            binding.imageView.setImageResource(R.mipmap.ic_launcher)
        }

    }

    fun loginShow(){
        if (FBAuth.isLogin.value == false) {
            CustomDialog(requireActivity()).apply {
                signOnClick {
                    val signInIntent: Intent =
                        FBAuth.getgoogleSignInClient()!!.signInIntent //구글로그인 페이지로 가는 인텐트 객체

                    startActivityForResult(signInIntent, 100) //Google Sign In flow 시작

                }
                finshOnclick { dismiss() }

            }.show()
        }else{//로그인 상태면 내정보창으로 이동
            (requireActivity() as MainActivity).myShow()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 구글로그인 버튼 응답
        if (requestCode == 100) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // 구글 로그인 성공
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                FBAuth.firebaseAuthWithGoogle(account){//로그인 성공 콜백
                    binding.imageView.setImageResource(R.mipmap.ic_launcher)
                }
            } catch (e: ApiException) {

            }
        }
    }

}