package com.project.sharingrestaurants.ui.on

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.adapter.OnAdapter
import com.project.sharingrestaurants.adapter.OnAddAdapter
import com.project.sharingrestaurants.data.BitmapImageItem
import com.project.sharingrestaurants.databinding.ActivityMainBinding
import com.project.sharingrestaurants.databinding.ActivityOffItemAddBinding
import com.project.sharingrestaurants.databinding.ActivityOnItemAddBinding
import com.project.sharingrestaurants.ui.off.ShowMapActivity
import com.project.sharingrestaurants.util.CameraWork
import com.project.sharingrestaurants.viewmodel.MainViewModel
import com.project.sharingrestaurants.viewmodel.OnAddViewModel
import com.project.sharingrestaurants.viewmodel.ViewModelFactory

class OnItemAddActivity : AppCompatActivity() {
    lateinit var binding: ActivityOnItemAddBinding
    lateinit var Adapter: OnAddAdapter
    val viewModel: OnAddViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory(MyApplication.REPOSITORY)).get(
            OnAddViewModel::class.java
        )
    }
    lateinit var cameraWork: CameraWork

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initStart()

        Adapter = OnAddAdapter{text, position ->//position은 중간중간 사진을 뺐다
            viewModel.textList.set(position,text)//textlist사이즈는 사진추가될때마다 추가
        }
        binding.recycle.apply {
            this.adapter = Adapter
            this.layoutManager =
                LinearLayoutManager(this@OnItemAddActivity, RecyclerView.VERTICAL, false)
            this.setHasFixedSize(true)//사이즈 측정이 필요없다 판단돼면 내부적으로 measure안한다
        }
        binding.camera.setOnClickListener {//pictureUri, pictureName은 임시 변수
            intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraWork.saveToMediaStore { pictureName, contentUri ->  viewModel.publicUri = contentUri; viewModel.publicName = pictureName}//공용저장소에 임시 파일 생성
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, viewModel.publicUri)//카메라엡이 publicUri를 통해 해당 위치에 사진 저장
            cameraCallBack.launch(intent)
        }

        binding.gallery.setOnClickListener {
            intent = Intent(Intent.ACTION_PICK)//갤러리
            intent.setType("image/*")//mine타입 지정
            galleryCallBack.launch(intent)
        }

        binding.back.setOnClickListener {
            finish()
        }

        binding.completeButton.setOnClickListener {
            addItem()
        }

        binding.showmap.setOnClickListener{
            intent = Intent(this, ShowMapActivity::class.java)
            mapCallBack.launch(intent)
        }
    }

    private fun initStart(){

        binding = DataBindingUtil.setContentView(this, R.layout.activity_on_item_add)//binding.viewModel에 viewmodel 담기전에 먼저 초기화
        binding.viewModel = viewModel //xml에서 main뷰모델 데이터에 접근 가능하게
        binding.lifecycleOwner = this //이거 안쓰면 데이터바인딩 쓸때 xml이 데이터 관측 못 함
        cameraWork = CameraWork(applicationContext)

    }

    private fun addItem() {//등록
        val builder = AlertDialog.Builder(this)
        builder.apply {
            this.setMessage(resources.getText(R.string.completeDialog))
            this.setNegativeButton("NO") { _, _ -> }
            this.setPositiveButton("YES") { _, _ ->
                for (text in viewModel.textList){
                    viewModel.setItemBody(text)
                }
                for (image in viewModel.imageList){
                    viewModel.setItemImage(image)
                }

                viewModel.addItem(this@OnItemAddActivity, contentResolver).observe(this@OnItemAddActivity){
                    if (true){
                        finish()
                    }else{
                        Log.d("작성실패","")
                    }
                }

            }
        }
        builder.show()
    }

    private val galleryCallBack: ActivityResultLauncher<Intent> = registerForActivityResult(//갤러리 앱
        ActivityResultContracts.StartActivityForResult()//콜백함수를 하나로 통일하면 누가 호출했는지 구분을 못 함
    ) {
        if (it.resultCode == RESULT_OK && it.data != null) {
            var intent: Intent = it.data!!
            var uri: Uri? = intent.data
            viewModel.imageList.add(uri.toString())//갤러리 uri
            viewModel.textList.add("")//에디트텍스트 추가
            Adapter.updateItem(uri.toString())//이미지뷰 추가
            Adapter.updateItem("")//에디트텍스트 추가
        }
    }

    private val cameraCallBack: ActivityResultLauncher<Intent> = registerForActivityResult(//카메라 앱
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            viewModel.imageList.add(viewModel.publicUri.toString())//공용저장소 uri
            viewModel.textList.add("")//에디트텍스트 추가
            Adapter.updateItem(viewModel.publicUri.toString())//이미지뷰 추가
            Adapter.updateItem("")//에디트텍스트 추가
        }else{//안찍고 나오거나 실패시
            //미디어스토어 임시파일 삭제
        }
    }

    private val mapCallBack: ActivityResultLauncher<Intent> = registerForActivityResult(//갤러리 앱
        ActivityResultContracts.StartActivityForResult()//콜백함수를 하나로 통일하면 누가 호출했는지 구분을 못 함
    ) {
        if (it.resultCode == RESULT_OK && it.data != null) {
            binding.showmap.text = it.data!!.getStringExtra("address")
            binding.showmap.background = null
            viewModel.itemLatitude = it.data!!.getDoubleExtra("latitude",0.0)
            viewModel.itemLongitude = it.data!!.getDoubleExtra("longitude",0.0)
            Log.d("위도2ㅇㅇㅎ",viewModel.itemLatitude.toString())
            Log.d("경도2ㅇㅇㅎ",viewModel.itemLongitude.toString())
        }
    }

}