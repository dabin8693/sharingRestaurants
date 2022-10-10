package com.project.sharingrestaurants.ui.on

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.adapter.OnAddAdapter
import com.project.sharingrestaurants.databinding.ActivityOnItemAddBinding
import com.project.sharingrestaurants.firebase.BoardEntity
import com.project.sharingrestaurants.ui.ShowMapActivity
import com.project.sharingrestaurants.util.CameraWork
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initStart()

        if (intent != null && intent.hasExtra("BoardEntity")) {
            //on detail에서 넘어온 경우
            val item: BoardEntity = intent.getSerializableExtra("BoardEntity") as BoardEntity
            applyExistingInfo(item)
        }else{
            Adapter = OnAddAdapter({ position -> deleteDialog(position)}, {intent = Intent(this, ShowMapActivity::class.java)
                mapCallBack.launch(intent)},viewModel, this).apply {
                val list = ArrayList<Any>()
                list.add(BoardEntity())//head
                list.add("")//edit
                list.add("")//linear
                this.setItemList(list)
            }
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
                for (index in 0 until Adapter.getItem().size){
                    if (index == Adapter.getItem().lastIndex){//마지막 리니어레이아웃 제외
                        break
                    }
                    if (index%2 == 0){
                        if (index != 0){//image
                            viewModel.imageList.add(Adapter.getItem().get(index).toString())
                        }
                    }else{//text
                        viewModel.textList.add(Adapter.getItem().get(index).toString())
                    }
                }

                //progressStart(viewModel.uploadSuccess)
                viewModel.upLoad(this@OnItemAddActivity, contentResolver).observe(this@OnItemAddActivity){
                    if (true){
                        Log.d("ㅇㅇ","저장성공")
                        setResult(RESULT_OK)
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

    private val galleryCallBack: ActivityResultLauncher<Intent> = registerForActivityResult(//갤러리 앱 //필드임
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK && it.data != null) {
            val intent: Intent = it.data!!
            val uri: Uri? = intent.data
            Adapter.addImage("")//에디트텍스트 추가
            Adapter.addImage(uri.toString())//이미지뷰 추가
        }
    }

    private val cameraCallBack: ActivityResultLauncher<Intent> = registerForActivityResult(//카메라 앱 //필드임
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            Adapter.addImage("")//에디트텍스트 추가
            Adapter.addImage(viewModel.publicUri.toString())//이미지뷰 추가
        }else{//안찍고 나오거나 실패시
            //미디어스토어 임시파일 삭제
        }
    }

    private val mapCallBack: ActivityResultLauncher<Intent> = registerForActivityResult(//네이버맵 //필드임
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK && it.data != null) {
            viewModel.itemLocate.value = it.data!!.getStringExtra("address")
            viewModel.mapDrawable.value = resources.getDrawable(R.drawable.empty,null)
            viewModel.itemLatitude = it.data!!.getDoubleExtra("latitude",0.0)
            viewModel.itemLongitude = it.data!!.getDoubleExtra("longitude",0.0)
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

    //기존 정보 적용
    private fun applyExistingInfo(item: BoardEntity){
        viewModel.setItem(item)
        if (!item.locate.equals("")){//지도 선택o
            viewModel.mapDrawable.value = resources.getDrawable(R.drawable.empty,null)
        }
//item.tilte, item.place, item.locate, item.priority
        Adapter = OnAddAdapter({ position -> deleteDialog(position)}, {intent = Intent(this, ShowMapActivity::class.java)
            mapCallBack.launch(intent)},viewModel, this).apply {
            val list = ArrayList<Any>()
            val boardEntity: BoardEntity = BoardEntity().also {
                it.tilte = item.tilte
                it.place = item.place
                it.locate = item.locate
                it.priority = item.priority
            }
            list.add(boardEntity)//head
            for (index in 0 until (item.body.size-1)){//총 인덱스-1 개
                list.add(item.body.get(index))
                list.add(item.image.get(index))
            }
            list.add(item.body.last())
            list.add("")//linear
            this.setItemList(list)
        }
        viewModel.isInserted = true
    }

}