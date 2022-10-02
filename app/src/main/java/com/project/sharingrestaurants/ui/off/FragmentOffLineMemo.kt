package com.project.sharingrestaurants.ui.off

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import com.gun0912.tedpermission.rx3.TedPermission
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.adapter.OffAdapter
import com.project.sharingrestaurants.custom.ButtomSheetDialog
import com.project.sharingrestaurants.custom.CustomDialog
import com.project.sharingrestaurants.databinding.FragOfflineMemoBinding
import com.project.sharingrestaurants.room.ItemEntity
import com.project.sharingrestaurants.ui.MainActivity
import com.project.sharingrestaurants.util.CameraWork
import com.project.sharingrestaurants.util.DataTrans
import com.project.sharingrestaurants.viewmodel.OffAddViewModel
import com.project.sharingrestaurants.viewmodel.OffLineViewModel
import com.project.sharingrestaurants.viewmodel.ViewModelFactory
import kotlinx.coroutines.*
import java.security.Signature


class FragmentOffLineMemo : Fragment() {

    private val viewModel: OffLineViewModel by lazy {//프래그먼트 객체가 사라질때까지 유지
        ViewModelProvider(this, ViewModelFactory(MyApplication.REPOSITORY)).get(
            OffLineViewModel::class.java
        )
    }
    private lateinit var binding: FragOfflineMemoBinding
    private lateinit var activity: MainActivity
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var offAdapter: OffAdapter
    private lateinit var itemList: List<ItemEntity>
    private lateinit var loginDialog: CustomDialog
    private var job: Job = CoroutineScope(Dispatchers.IO).launch { }

    //ViewLifecycleOwner는 onCreateView 이전에 호출되어서 onDestroyView때 null이 된다.
    override fun onCreateView(//레이아웃 인플레이트 하는곳 //액티비티의 onstart랑 비슷하다
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        initStart(inflater, container, savedInstanceState)//뷰모델, 바인딩 초기화
        return binding.root//데이터바인딩의 생명주기가 프래그먼트랑 같아서
        //백스택을 사용하면 ondetach까지 가지 않고 oncreateview ondestoryview만 반복될수있어서 메모리 누수가 발생한다.(뷰가 종료되면 gc에 수거되야하는데 데이터바인딩을 참조해서 수거가 안됨)
    }//ondestoryview에서 binding = null

    //액티비티에 화면이 가려져도 fragment생명주기 콜백이 호출이 안된다. //프래그먼트 교체때만 생명주기 호출된다.
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {//뷰 초기화, livedata옵저버, recyclerview, viewpager2, adapter초기화
        super.onViewCreated(
            view,
            savedInstanceState
        )//옵저버 lifecycle 무조건 viewLifecycleOwner사용!!!(중복 구독 방지)
        Log.d("프래그 ㅁㅁ", "ㄴㅇㄹ")
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
            Log.d("초기화 ㅇㄴㄹ", "ㄴㅇㄹㄴ")
            Log.d("1", list.toString())
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
        requestPermissions()//위치 권한
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
            Log.d("url값은ㅇ", viewModel.getAuth().photoUrl.value.toString())
            Glide.with(this)
                .load(viewModel.getAuth().photoUrl.value)//첫번째 사진만 보여준다
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
        job = GlobalScope.launch {//workThreadPool동작
            delay(800)//딜레이 끝나면 내부적으로 cancel명령이 왔는지 확인한다.
            viewModel.sarchTextDelay.postValue(viewModel.searchText.value)
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

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//데이터 바인딩 onclick
    fun spinnerDialogShow() {
        var buttomSheetDialog = ButtomSheetDialog(viewModel.spinnerName.value!!) {
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
                viewModel.signIn(account, java.lang.ref.WeakReference(activity).get()) {//로그인 성공
                    viewModel.addFBAuth(viewLifecycleOwner)//db회원 정보 저장 및 불러오기
                    Log.d("url값은", viewModel.getAuth().photoUrl.value.toString())
                    loginDialog.dismiss()
                    Glide.with(this)
                        .load(viewModel.getAuth().photoUrl.value)//첫번째 사진만 보여준다
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .signature(ObjectKey("sign"))
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