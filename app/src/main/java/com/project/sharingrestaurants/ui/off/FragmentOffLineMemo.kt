package com.project.sharingrestaurants.ui.off

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.project.sharingrestaurants.adapter.OffAdapter
import com.project.sharingrestaurants.custom.ButtomSheetDialog
import com.project.sharingrestaurants.databinding.FragOfflineMemoBinding
import com.project.sharingrestaurants.room.ItemEntity
import com.project.sharingrestaurants.viewmodel.OffLineViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentOffLineMemo : Fragment() {

    //val viewmodel: OffLineViewModel by lazy { ViewModelProvider(this).get(OffLineViewModel::class.java) }
    lateinit var viewmodel: OffLineViewModel
    lateinit var binding: FragOfflineMemoBinding
    lateinit var inputMethodManager: InputMethodManager
    lateinit var offAdapter: OffAdapter
    lateinit var itemList: List<ItemEntity>
    lateinit var job: Job

    //ViewLifecycleOwner는 onCreateView 이전에 호출되어서 onDestroyView때 null이 된다.
    override fun onCreateView(//레이아웃 인플레이트 하는곳 //액티비티의 onstart랑 비슷하다
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        initStart(inflater, container, savedInstanceState)//뷰모델, 바인딩 초기화
        return binding.root
    }

    //액티비티에 화면이 가려져도 fragment생명주기 콜백이 호출이 안된다. //프래그먼트 교체때만 생명주기 호출된다.
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {//뷰 초기화, livedata옵저버, recyclerview, viewpager2, adapter초기화
        super.onViewCreated(
            view,
            savedInstanceState
        )//옵저버 lifecycle 무조건 viewLifecycleOwner사용!!!(중복 구독 방지)
        spinnerInitialize()//최초 스피너값 초기화
        inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager //키보드 매니저
        //inputMethodManager.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
        offAdapter = OffAdapter({
            val intent = Intent(requireActivity(),OffItemDetailShowActivity::class.java)//onClick
            intent.putExtra("ItemEntity", it)
            startActivity(intent)
        },
            {
                deleteDialog(it)//onLongClick
            })
        binding.recyclerView.apply {
            this.adapter = offAdapter
            this.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)//그리드 모양으로
            this.setHasFixedSize(true)//사이즈 측정이 필요없다 판단돼면 내부적으로 measure안한다
        }
        binding.searchView.setOnFocusChangeListener { view, hasFocus ->
            //클릭 리스너는 한번 터치로 호출이 안돼서 이걸로 씀
            //초기값은 hint false, visibility false로 설정 함
            if (hasFocus) {
                binding.searchView.hint = "검색어를 입력해주세요"
                binding.textClearButton.visibility = View.VISIBLE
                inputMethodManager.showSoftInput(binding.searchView, 0)
            }
        }
        viewmodel.searchText.observe(viewLifecycleOwner){
            if(job != null){
                job.cancel()
            }
            queryDeBouncing()
        }
        viewmodel.getItemList().observe(viewLifecycleOwner){ list ->
            binding.noticeEmptyList.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            itemList = list
            offAdapter.setItems(itemList)
        }


    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun initStart(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) {
        viewmodel =
            ViewModelProvider(requireActivity()).get(OffLineViewModel::class.java)//프래그먼트 lifecycle은 화면회전시 초기화 됨
        binding = FragOfflineMemoBinding.inflate(inflater, container, false)
        binding.viewModel = viewmodel
        binding.fragmentOff = this
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun spinnerInitialize() {
        if (viewmodel.spinnerName.value == null) {// 앱 처음 켰을때 초기값 설정
            viewmodel.spinnerName.value = "제목"
        }
    }
    private fun deleteDialog(item: ItemEntity) {
        val builder = AlertDialog.Builder(requireActivity())
        builder.apply {
            this.setMessage("삭제하시겠습니까?")
            this.setNegativeButton("NO") { a, b -> }
            this.setPositiveButton("YES") { a, b ->
                viewmodel.delete(item)

            }
        }
        builder.show()
    }
    private fun queryDeBouncing(){
        job = GlobalScope.launch {//workThreadPool동작
            delay(800)//딜레이 끝나면 내부적으로 cancel명령이 왔는지 확인한다.
            viewmodel.sarchTextDelay.value = viewmodel.spinnerName.value
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//데이터 바인딩 onclick
    fun spinnerDialogShow() {
        var buttomSheetDialog = ButtomSheetDialog(viewmodel.spinnerName.value!!) {
            if (it == "제목") {
                viewmodel.spinnerName.value = "제목"
            } else {
                viewmodel.spinnerName.value = "제목+내용"
            }
        }
        buttomSheetDialog.show(childFragmentManager, "")
    }

    fun textClear() {
        binding.searchView.text = null
    }

    fun searchShow() {
        if (binding.searchParent.visibility != View.VISIBLE) {
            binding.searchParent.visibility = View.VISIBLE
        } else {
            binding.searchParent.visibility = View.GONE
        }
    }

    fun loginShow() {

    }
}