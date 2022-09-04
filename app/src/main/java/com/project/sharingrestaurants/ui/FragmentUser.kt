package com.project.sharingrestaurants.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.databinding.FragOfflineMemoBinding
import com.project.sharingrestaurants.databinding.FragUserBinding
import com.project.sharingrestaurants.firebase.FBAuth
import com.project.sharingrestaurants.util.DataTrans
import com.project.sharingrestaurants.viewmodel.OffLineViewModel
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
            FBAuth.signOut()
            (requireActivity() as MainActivity).offShow()
        }
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