package com.project.sharingrestaurants.ui.off

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.adapter.OffAdapter
import com.project.sharingrestaurants.custom.SortButtomSheetDialog
import com.project.sharingrestaurants.custom.LoginDialog
import com.project.sharingrestaurants.databinding.FragOfflineMemoBinding
import com.project.sharingrestaurants.room.ItemEntity
import com.project.sharingrestaurants.ui.MainActivity
import com.project.sharingrestaurants.util.RunTimePermissionCheck
import com.project.sharingrestaurants.viewmodel.OffLineViewModel
import com.project.sharingrestaurants.viewmodel.ViewModelFactory
import kotlinx.coroutines.*


class FragmentOffLineMemo : Fragment() {

    private val viewModel: OffLineViewModel by lazy {//main에서 프래그먼트 객체 = null되기전까지 유지됨 //ondestoy되어도 트랜젝션에서 replace되면 데이터 복구됨
        ViewModelProvider(this, ViewModelFactory(MyApplication.REPOSITORY)).get(
            OffLineViewModel::class.java
        )
    }
    private lateinit var binding: FragOfflineMemoBinding
    private lateinit var activity: MainActivity
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var offAdapter: OffAdapter
    private lateinit var itemList: List<ItemEntity>
    private lateinit var loginDialog: LoginDialog
    private var job: Job = CoroutineScope(Dispatchers.IO).launch { }

    //ViewLifecycleOwner는 onCreateView 이전에 호출되어서 onDestroyView때 null이 된다.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        initStart(inflater, container, savedInstanceState)//뷰모델, 바인딩 초기화
        return binding.root//데이터바인딩의 생명주기가 프래그먼트랑 같아서
        //백스택을 사용할 경우 ondestoryview에서 binding = null
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {//뷰 초기화
        super.onViewCreated(
            view,
            savedInstanceState
        )//옵저버 lifecycle 무조건 viewLifecycleOwner사용!!!(중복 구독 방지)
        inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager //키보드 매니저
        //inputMethodManager.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
        offAdapter = OffAdapter(
            { item, position ->
                val intent =
                    Intent(requireActivity(), OffItemDetailShowActivity::class.java)//onClick
                intent.putExtra("ItemEntity", item)
                intent.putExtra("position", position)
                startActivity(intent)
            },
            {
                deleteDialog(it)//onLongClick
            }, viewModel.currentLatitude.value!!, viewModel.currentLongitude.value!!
        )
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
        viewModel.searchText.observe(viewLifecycleOwner) {
            if (job != null) {
                job.cancel()
            }
            queryDeBouncing()
        }
        viewModel.getItemList(resources.getString(R.string.spinner_item_title))
            .observe(viewLifecycleOwner) { list ->
                binding.noticeEmptyList.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                itemList = list
                offAdapter.setItems(itemList)
            }

        viewModel.getList().observe(viewLifecycleOwner) { list ->
            offAdapter.setItems(list)//프래그먼트가 초기화 될때마다 리스트 초기화
        }


    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun initStart(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) {

        binding = FragOfflineMemoBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.fragmentOff = this
        binding.lifecycleOwner = viewLifecycleOwner
        activity = requireActivity() as MainActivity
        //초기값 설정
        viewModel.spinnerName.value = resources.getString(R.string.spinner_item_title)
        RunTimePermissionCheck.requestPermissions(activity)//위치 권한
        viewModel.currentLatitude.value = 0.0
        viewModel.currentLongitude.value = 0.0
        viewModel.getCurrentGPS(activity).observe(viewLifecycleOwner) {
            viewModel.currentLatitude.value = it.latitude
            viewModel.currentLongitude.value = it.longitude
            if (offAdapter != null) {
                offAdapter.distChanged(it.latitude, it.longitude)
            }
        }

        if (viewModel.getIsLogin() == true) {
            Glide.with(this)
                .load(viewModel.getAuth().profileImage)//첫번째 사진만 보여준다
                .into(binding.imageView)
                .onLoadFailed(ResourcesCompat.getDrawable(resources, R.mipmap.ic_launcher, null))
        }
    }

    private fun deleteDialog(item: ItemEntity) {
        val builder = AlertDialog.Builder(requireActivity())
        builder.apply {
            this.setMessage("삭제하시겠습니까?")
            this.setNegativeButton("NO") { _, _ -> }
            this.setPositiveButton("YES") { _, _ ->
                viewModel.delete(item)

            }
        }
        builder.show()
    }

    private fun queryDeBouncing() {
        job = CoroutineScope(Dispatchers.IO).launch {//workThreadPool동작
            delay(800)//딜레이 끝나면 내부적으로 cancel명령이 왔는지 확인한다.
            viewModel.sarchTextDelay.postValue(viewModel.searchText.value)
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//데이터 바인딩 onclick
    fun spinnerDialogShow() {
        var buttomSheetDialog = SortButtomSheetDialog(viewModel.spinnerName.value!!) {
            if (it == resources.getString(R.string.spinner_item_title)) {
                viewModel.spinnerName.value = resources.getString(R.string.spinner_item_title)
            } else {
                viewModel.spinnerName.value = resources.getString(R.string.spinner_item_titleplace)
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
                viewModel.signIn(account, java.lang.ref.WeakReference(activity).get()) {//로그인 성공
                    viewModel.addFBAuth(viewLifecycleOwner)//db회원 정보 저장 및 불러오기
                    loginDialog.dismiss()
                    Glide.with(this)
                        .load(viewModel.getAuth().profileImage)//첫번째 사진만 보여준다
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


}