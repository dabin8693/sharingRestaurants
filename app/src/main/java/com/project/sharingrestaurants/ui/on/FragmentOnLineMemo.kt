package com.project.sharingrestaurants.ui.on

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.project.sharingrestaurants.databinding.FragOfflineMemoBinding
import com.project.sharingrestaurants.databinding.FragOnlineMemoBinding
import com.project.sharingrestaurants.viewmodel.OffLineViewModel
import com.project.sharingrestaurants.viewmodel.OnLineViewModel

class FragmentOnLineMemo : Fragment() {//뷰페이저2 구현
val viewmodel: OnLineViewModel by lazy { ViewModelProvider(this).get(OnLineViewModel::class.java) }
    lateinit var binding: FragOnlineMemoBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("frag2","onAttach")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("frag2","onCreate")
    }

    override fun onCreateView(//레이아웃 인플레이트 하는곳 //액티비티의 onstart랑 비슷하다
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("frag2","onCreateView")
        binding = FragOnlineMemoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {//뷰 초기화, livedata옵저버, recyclerview, viewpager2, adapter초기화
        super.onViewCreated(view, savedInstanceState)
        Log.d("frag2","onViewCreated")

    }

    override fun onDestroyView() {//backstack에 들어가면 액티비티의 onstop이랑 비슷하다
        super.onDestroyView()
        Log.d("frag2","onDestroyView")
        //이 프레그먼트가 안보일때 viewmodel메모리 회수하고 싶으면 여기서 null한다.(프래그먼트 생명주기 참조하는viewmodel만 해당)
        // -나는 앱 종료전까지는 viewmodel을 제거 할 생각이 없다. 화면 리프레시를 안 좋아함.
        //액티비티 생명주기를 참조하는 viewmodel은 다른곳에서도 참조하고 있어서 메모리 회수가 안됨
        //Fragment View!!!에 대한 모든 참조가 제거되어야 함. -꼼꼼히 확인
    }

    override fun onDestroy() {//Fragment 가 제거되거나 FragmentManager 가 destroy 됐을 경우
        super.onDestroy()//수동으로 제거 할 생각이 없으므로 error 발생이나 액티비가 종료될때 호출 됨
        Log.d("frag2","onDestroy")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("frag2","onDetach")
    }
}