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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.gun0912.tedpermission.rx3.TedPermission
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.adapter.OffAdapter
import com.project.sharingrestaurants.custom.ButtomSheetDialog
import com.project.sharingrestaurants.custom.CustomDialog
import com.project.sharingrestaurants.databinding.FragOfflineMemoBinding
import com.project.sharingrestaurants.room.ItemEntity
import com.project.sharingrestaurants.ui.MainActivity
import com.project.sharingrestaurants.util.CameraWork
import com.project.sharingrestaurants.util.DataTrans
import com.project.sharingrestaurants.viewmodel.OffLineViewModel
import kotlinx.coroutines.*


class FragmentOffLineMemo : Fragment() {

    //val viewmodel: OffLineViewModel by lazy { ViewModelProvider(this).get(OffLineViewModel::class.java) }
    lateinit var viewmodel: OffLineViewModel
    lateinit var binding: FragOfflineMemoBinding
    //private val binding2 get() = binding!! null처리 필요 할 경우
    lateinit var activity: MainActivity
    lateinit var inputMethodManager: InputMethodManager
    lateinit var offAdapter: OffAdapter
    lateinit var itemList: List<ItemEntity>
    lateinit var loginDialog: CustomDialog
    var job: Job = CoroutineScope(Dispatchers.IO).launch {  }

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

        inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager //키보드 매니저
        //inputMethodManager.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
        offAdapter = OffAdapter({item, position ->
            val intent = Intent(requireActivity(),OffItemDetailShowActivity::class.java)//onClick
            intent.putExtra("ItemEntity", item)
            intent.putExtra("position", position)
            startActivity(intent)
        },
            {
                deleteDialog(it)//onLongClick
            }, viewmodel.currentLatitude.value!!, viewmodel.currentLongitude.value!!
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
        viewmodel.searchText.observe(viewLifecycleOwner){
            if(job != null){
                job.cancel()
            }
            queryDeBouncing()
        }
        viewmodel.getItemList(resources.getString(R.string.spinner_item_title)).observe(viewLifecycleOwner){ list ->
            binding.noticeEmptyList.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            itemList = list
            offAdapter.setItems(itemList)
        }

        viewmodel.getList().observe(viewLifecycleOwner){ list ->
            Log.d("초기화 ㅇㄴㄹ","ㄴㅇㄹㄴ")
            Log.d("1",list.toString())
            offAdapter.setItems(list)//프래그먼트가 초기화 될때마다 리스트 초기화
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
        activity = requireActivity() as MainActivity
        //초기값 설정
        viewmodel.spinnerName.value = resources.getString(R.string.spinner_item_title)
        requestPermissions()//위치 권한
        viewmodel.currentLatitude.value = 0.0
        viewmodel.currentLongitude.value = 0.0
        viewmodel.getCurrentGPS(activity).observe(viewLifecycleOwner){
            viewmodel.currentLatitude.value = it.latitude
            viewmodel.currentLongitude.value = it.longitude
            if (offAdapter != null){
                offAdapter.distChanged(it.latitude, it.longitude)
            }
        }

        if (viewmodel.getIsLogin() == true){
            Log.d("url값은ㅇ",viewmodel.getAuth().photoUrl.value.toString())
            Glide.with(this)
                .load(viewmodel.getAuth().photoUrl.value)//첫번째 사진만 보여준다
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
                viewmodel.delete(item)

            }
        }
        builder.show()
    }
    private fun queryDeBouncing(){
        job = GlobalScope.launch {//workThreadPool동작
            delay(800)//딜레이 끝나면 내부적으로 cancel명령이 왔는지 확인한다.
            viewmodel.sarchTextDelay.postValue(viewmodel.searchText.value)
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
                    Toast.makeText(requireActivity(),getString(R.string.location_permission_denied_msg), Toast.LENGTH_SHORT).show()
                }
            }) { throwable -> Log.e("AAAAAA", throwable.message.toString()) }


    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//데이터 바인딩 onclick
    fun spinnerDialogShow() {
        var buttomSheetDialog = ButtomSheetDialog(viewmodel.spinnerName.value!!) {
            if (it == resources.getString(R.string.spinner_item_title)) {
                viewmodel.spinnerName.value = resources.getString(R.string.spinner_item_title)
            } else {
                viewmodel.spinnerName.value = resources.getString(R.string.spinner_item_titleplace)
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
        if (viewmodel.getIsLogin() == false) {
            loginDialog = CustomDialog(activity)
            loginDialog.signOnClick {
                val signInIntent: Intent =
                        viewmodel.getAuth().googleSignInClient!!.signInIntent //구글로그인 페이지로 가는 인텐트 객체

                    startActivityForResult(
                        signInIntent,
                        100
                    ) //Google Sign In flow 시작
            }
            loginDialog.finshOnclick { loginDialog.dismiss() }
            loginDialog.show()
        }else{//로그인 상태면 내정보창으로 이동
            activity.myShow()
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
                viewmodel.signIn(account, activity){
                    Log.d("url값은",viewmodel.getAuth().photoUrl.value.toString())
                        loginDialog.dismiss()
                        Glide.with(this)
                            .load(viewmodel.getAuth().photoUrl.value)//첫번째 사진만 보여준다
                            .into(binding.imageView)
                            .onLoadFailed(ResourcesCompat.getDrawable(resources, R.mipmap.ic_launcher, null))

                }
            } catch (e: ApiException) {

            }
        }
    }



}