package com.project.sharingrestaurants.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.project.sharingrestaurants.databinding.FragUserBinding
import com.project.sharingrestaurants.firebase.FBLogin
import com.project.sharingrestaurants.viewmodel.UserViewModel

class FragmentUser: Fragment() {
    lateinit var viewmodel: UserViewModel
    lateinit var binding: FragUserBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initStart(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logoutbutton.setOnClickListener{
            viewmodel.signOut()
            (requireActivity() as MainActivity).offShow()
        }
        binding.idemail.setText("dabin75783239@gmail.com")
        binding.nickname.setText("식객")
        Glide.with(this)
            .load(viewmodel.getAuth().photoUrl.value)//첫번째 사진만 보여준다
            .override(210,210)
            .into(binding.profileimage)
    }

    private fun initStart(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) {
        viewmodel =
            ViewModelProvider(requireActivity()).get(UserViewModel::class.java)//프래그먼트 lifecycle은 화면회전시 초기화 됨
        binding = FragUserBinding.inflate(inflater, container, false)
        binding.viewModel = viewmodel
        binding.lifecycleOwner = viewLifecycleOwner

        //초기값 설정

    }
}