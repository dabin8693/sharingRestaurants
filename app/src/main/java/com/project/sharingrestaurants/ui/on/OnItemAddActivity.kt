package com.project.sharingrestaurants.ui.on

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toDrawable
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableResource
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.adapter.OnAdapter
import com.project.sharingrestaurants.adapter.OnAddAdapter
import com.project.sharingrestaurants.data.BitmapImageItem
import com.project.sharingrestaurants.data.BoardHeadEntity
import com.project.sharingrestaurants.databinding.ActivityMainBinding
import com.project.sharingrestaurants.databinding.ActivityOffItemAddBinding
import com.project.sharingrestaurants.databinding.ActivityOnItemAddBinding
import com.project.sharingrestaurants.firebase.BoardEntity
import com.project.sharingrestaurants.ui.off.ShowMapActivity
import com.project.sharingrestaurants.util.CameraWork
import com.project.sharingrestaurants.viewmodel.MainViewModel
import com.project.sharingrestaurants.viewmodel.OnAddViewModel
import com.project.sharingrestaurants.viewmodel.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OnItemAddActivity : AppCompatActivity() {
    lateinit var binding: ActivityOnItemAddBinding
    lateinit var Adapter: OnAddAdapter
    val viewModel: OnAddViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory(MyApplication.REPOSITORY)).get(
            OnAddViewModel::class.java
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initStart()

        Adapter = OnAddAdapter({ position -> deleteDialog(position)}, {intent = Intent(this, ShowMapActivity::class.java)
                mapCallBack.launch(intent)},viewModel, this).apply {
            val list = ArrayList<Any>()
            list.add(BoardHeadEntity("", "", "", 0f))//head
            list.add("")//edit
            list.add("")//linear
            this.setItemList(list)
        }
        binding.recycle.apply {
            this.adapter = Adapter
            this.layoutManager =
                LinearLayoutManager(this@OnItemAddActivity, RecyclerView.VERTICAL, false)
            this.setHasFixedSize(true)//사이즈 측정이 필요없다 판단돼면 내부적으로 measure안한다
        }

        binding.camera.setOnClickListener {//pictureUri, pictureName은 임시 변수
            intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            CameraWork.saveToMediaStore(applicationContext) { pictureName, contentUri ->  viewModel.publicUri = contentUri; viewModel.publicName = pictureName}//공용저장소에 임시 파일 생성
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


    }

    private fun initStart(){

        binding = DataBindingUtil.setContentView(this, R.layout.activity_on_item_add)//binding.viewModel에 viewmodel 담기전에 먼저 초기화
        binding.viewModel = viewModel //xml에서 main뷰모델 데이터에 접근 가능하게
        binding.lifecycleOwner = this //이거 안쓰면 데이터바인딩 쓸때 xml이 데이터 관측 못 함

    }

    private fun addItem() {//등록
        val builder = AlertDialog.Builder(this)
        builder.apply {
            this.setMessage(resources.getText(R.string.completeDialog))
            this.setNegativeButton("NO") { _, _ -> }
            this.setPositiveButton("YES") { _, _ ->
                for (any in Adapter.getItem()){
                    if (Adapter.getItem().indexOf(any) == Adapter.getItem().lastIndex){//마지막 리니어레이아웃 제외
                        break
                    }
                    if (Adapter.getItem().indexOf(any)%2 == 0){
                        if (Adapter.getItem().indexOf(any) != 0){//image
                            viewModel.imageList.add(any as String)
                            Log.d("온이미지",(any as String))
                        }
                    }else{//text
                        viewModel.textList.add(any as String)
                        Log.d("온텍스트",(any as String))
                    }
                }
                /*
                for (text in viewModel.textList){
                    viewModel.setItemBody(text)
                }
                for (image in viewModel.imageList){
                    viewModel.setItemImage(image)
                }

                 */
                //progressStart(viewModel.uploadSuccess)
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

    private fun progressStart(uploadSuccess: LiveData<Boolean>){
        //다이얼로그 생성 -> 다이얼로그 안에서 프로그레스바 진행(io스레드에서)
        uploadSuccess.observe(this){//다이얼로그 중복 호출로 인한 옵저버 중복 생성 방지로 다이얼로그 외부에서 옵저버 생성
            //여기서 다이얼로그안의 프로그레스바 진행 상태 변경
        }
    }

    private val galleryCallBack: ActivityResultLauncher<Intent> = registerForActivityResult(//갤러리 앱
        ActivityResultContracts.StartActivityForResult()//콜백함수를 하나로 통일하면 누가 호출했는지 구분을 못 함
    ) {
        if (it.resultCode == RESULT_OK && it.data != null) {
            val intent: Intent = it.data!!
            val uri: Uri? = intent.data
            //viewModel.imageList.add(uri.toString())//갤러리 uri//최대치만 확장하는거
            //viewModel.textList.add("")//에디트텍스트 추가//최대치만 확장하는거
            Adapter.addImage("")//에디트텍스트 추가
            Adapter.addImage(uri.toString())//이미지뷰 추가
        }
    }

    private val cameraCallBack: ActivityResultLauncher<Intent> = registerForActivityResult(//카메라 앱
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            //viewModel.imageList.add(viewModel.publicUri.toString())//공용저장소 uri//최대치만 확장하는거
            //viewModel.textList.add("")//에디트텍스트 추가//최대치만 확장하는거
            Adapter.addImage("")//에디트텍스트 추가
            Adapter.addImage(viewModel.publicUri.toString())//이미지뷰 추가
        }else{//안찍고 나오거나 실패시
            //미디어스토어 임시파일 삭제
        }
    }

    private val mapCallBack: ActivityResultLauncher<Intent> = registerForActivityResult(//갤러리 앱
        ActivityResultContracts.StartActivityForResult()//콜백함수를 하나로 통일하면 누가 호출했는지 구분을 못 함
    ) {
        if (it.resultCode == RESULT_OK && it.data != null) {
            //binding.showmap.text = it.data!!.getStringExtra("address")
            //binding.showmap.background = null
            viewModel.itemLocate.value = it.data!!.getStringExtra("address")
            viewModel.mapDrawable.value = resources.getDrawable(R.drawable.empty,null)
            //Adapter.mapSelected(BoardHeadEntity(viewModel.itemTitle.value!!, viewModel.itemPlace.value!!, it.data!!.getStringExtra("address")!!, viewModel.itemPriority.value!!))
            viewModel.itemLatitude = it.data!!.getDoubleExtra("latitude",0.0)
            viewModel.itemLongitude = it.data!!.getDoubleExtra("longitude",0.0)
            Log.d("위도2ㅇㅇㅎ",viewModel.itemLatitude.toString())
            Log.d("경도2ㅇㅇㅎ",viewModel.itemLongitude.toString())
        }
    }

    private fun deleteDialog(position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            this.setMessage("삭제하시겠습니까?")
            this.setNegativeButton("NO") { _, _ -> }
            this.setPositiveButton("YES") { _, _ ->
                Adapter.deleteItem(position)
            }
        }
        builder.show()
    }

}