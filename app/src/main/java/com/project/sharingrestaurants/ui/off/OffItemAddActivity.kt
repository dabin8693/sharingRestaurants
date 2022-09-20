package com.project.sharingrestaurants.ui.off

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.data.BitmapImageItem
import com.project.sharingrestaurants.data.OffDetailItem
import com.project.sharingrestaurants.databinding.ActivityOffItemAddBinding
import com.project.sharingrestaurants.util.CameraWork
import com.project.sharingrestaurants.viewmodel.OffAddViewModel
import com.project.sharingrestaurants.viewmodel.OffDetailViewModel
import com.project.sharingrestaurants.viewmodel.OffLineViewModel
import com.project.sharingrestaurants.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

//화면 고정설정
class OffItemAddActivity : AppCompatActivity() {
    lateinit var binding: ActivityOffItemAddBinding
    val viewModel: OffAddViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory(MyApplication.REPOSITORY)).get(
            OffAddViewModel::class.java
        )
    }
    lateinit var cameraWork: CameraWork


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initStart()

        if (intent != null && intent.hasExtra("OffDetailItem")) {
            //off detail에서 넘어온 경우
            applyExistingInfo(intent.getSerializableExtra("OffDetailItem") as OffDetailItem)
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

        binding.focuselinear.setOnClickListener{//가장 마지막 뷰에 포커싱 주기
            if (viewModel.viewList!!.size == 0) {//동적 뷰 생성한게 없을때
                binding.editbody.requestFocus()//맨 처음 에디트
            }else{
                viewModel.viewList!!.get(viewModel.viewList!!.size-1).requestFocus()//마지막 에디트 텍스트
            }
        }

        binding.editbody.setOnFocusChangeListener{_,_->
            viewModel.nowEditPosition = 0 //기존 에디트 텍스트 포커싱(body에디트 최상단꺼)
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

    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun initStart() {

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_off_item_add
        )//binding.viewModel에 viewmodel 담기전에 먼저 초기화
        binding.viewModel = viewModel //xml에서 main뷰모델 데이터에 접근 가능하게
        binding.lifecycleOwner = this //이거 안쓰면 데이터바인딩 쓸때 xml이 데이터 관측 못 함
        viewModel.nowEditText = binding.editbody
        viewModel.childCount = binding.addLinear.childCount - 1//마지막 리니어레이아웃1개 뺀다
        viewModel.nowEditPosition = 0
        cameraWork = CameraWork(applicationContext)
    }

    private fun addItem() {//등록
        val builder = AlertDialog.Builder(this)
        builder.apply {
            this.setMessage(resources.getText(R.string.completeDialog))
            this.setNegativeButton("NO") { _, _ -> }
            this.setPositiveButton("YES") { _, _ ->
                if (viewModel.getImageList().size != 0) {
                    var tempUri: String = ""
                    for (i in 0..(viewModel.getImageList().size - 1)) {//비트맵,string타입의 uri
                        if (viewModel.getImageList().get(i) is BitmapImageItem) {//비트맵일 경우 로컬에 저장후 uri가져오기
                            tempUri = cameraWork.saveToprivate(//로컬에 비트맵저장하고 uri가져오기
                                (viewModel.getImageList().get(i) as BitmapImageItem).bitmap,
                                (viewModel.getImageList().get(i) as BitmapImageItem).name
                            )

                        }else{//기존에 저장한적 있는 이미지 uri
                            tempUri = viewModel.getImageList().get(i) as String
                        }
                        viewModel.setItemImage(tempUri) //사진 없으면 itemImages에 ""만 들어가 있다
                    }
                }
                //binding.editbody.text 글어 없으면 ""들어가 있다
                viewModel.setItemBody(binding.editbody.text.toString())//최초 에디트텍스트
                for (i in viewModel.viewList!!){//그 이후 생성된 에디트텍스트
                    if (i is EditText){
                        viewModel.setItemBody(i.text.toString())
                    }
                }
                viewModel.addItem()

                finish()
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
            callBackProcessing(uri!!, "gallery")//갤러리 uri
        }
    }

    private val cameraCallBack: ActivityResultLauncher<Intent> = registerForActivityResult(//카메라 앱
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            callBackProcessing(viewModel.publicUri, "camera")//공용저장소 uri
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

    private fun callBackProcessing(uri: Uri, code: String){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val source = ImageDecoder.createSource(contentResolver, uri!!)
            ImageDecoder.decodeBitmap(source)?.let {
                var bitmap = cameraWork.resizeBitmap(it)//0-0 1-2 2-4 3-6
                //viewModel.imageBitmapOrStringList.add(getViewPosition()/2,bitmap)
                if (code == "gallery") {//imageBitmapOrStringList변수는 이미지만 저장해서 getViewPosition보다 position이 2배 작다.
                    viewModel.addImageBitmap(getViewPosition()/2, BitmapImageItem(bitmap, cameraWork.getTime() + ".jpg"))
                    //viewModel.imageFileNameList.add(cameraWork.getTime() + ".jpg")
                }else if (code == "camera"){
                    viewModel.addImageBitmap(getViewPosition()/2, BitmapImageItem(bitmap, viewModel.publicName))
                    //viewModel.imageFileNameList.add(viewModel.pictureName)
                }
                addViewAuto(getViewPosition(), bitmap)
            }
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, uri!!)?.let {
                var bitmap = cameraWork.resizeBitmap(it)
                //viewModel.imageBitmapOrStringList.add(getViewPosition()/2,bitmap)
                if (code == "gallery") {
                    viewModel.addImageBitmap(getViewPosition()/2, BitmapImageItem(bitmap, cameraWork.getTime() + ".jpg"))
                }else if (code == "camera"){
                    viewModel.addImageBitmap(getViewPosition()/2, BitmapImageItem(bitmap, viewModel.publicName))
                }
                addViewAuto(getViewPosition(), bitmap)
            }
        }
    }




    private fun makeEditText(position: Int) {//포지션: 포커스 현재위치
        val editText = EditText(applicationContext)
        val layoutParam = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE//비트 연산으로 해야됨
        editText.background = null
        editText.layoutParams = layoutParam
        editText.setOnFocusChangeListener(focusEvent)
        //editText.background = null
        editText.id = 100 + position

        viewModel.viewList!!.add(position , editText)//현재 포지션보다 뒤에 추가

    }

    private fun makeImageView(position: Int, bitmap: Bitmap) {//포지션: 포커스 현재위치
        val imageView = ImageView(applicationContext)
        val layoutParam = LinearLayout.LayoutParams(
            360,
            640
        )
        layoutParam.gravity = Gravity.CENTER
        imageView.layoutParams = layoutParam
        imageView.setImageBitmap(bitmap)
        imageView.setOnLongClickListener(clickEvent)
        imageView.id = 1000 + position

        viewModel.viewList!!.add(position , imageView)//현재 포지션보다 뒤에 추가
    }

    private fun makeImageView2(position: Int, uri: String) {//포지션: 포커스 현재위치
        val imageView = ImageView(applicationContext)
        val layoutParam = LinearLayout.LayoutParams(
            360,
            640
        )
        layoutParam.gravity = Gravity.CENTER
        imageView.layoutParams = layoutParam
        imageView.setImageURI(Uri.parse(uri))
        imageView.setOnLongClickListener(clickEvent)
        imageView.id = 1000 + position

        viewModel.viewList!!.add(position , imageView)//현재 포지션보다 뒤에 추가
    }

    private fun deleteEditText(editText: EditText, position: Int) {//에디트 텍스트 합치기(마지막껄 삭제)
        val text = editText.text
        binding.addLinear.removeView(editText)
        viewModel.viewList!!.remove(editText)
        (binding.addLinear.get(position) as EditText).text.append(text)
    }

    private fun deleteImageView(imageView: ImageView) {
        val position = binding.addLinear.indexOfChild(imageView)//삭제 할 뷰 위치 찾기
        //position-4 = 첫번째 이미지 값은 1이다.
        //position이 title = 0 locate = 1 place = 2 rating = 3 첫 에디트 = 4이다.
        //1/2 = 0, 3/2 = 1이다. (소수점 내림) position-5로 해도 됨
        viewModel.deleteImageBitmap((position-4)/2)//이미지리스트에서 현재이미지 삭제

        binding.addLinear.removeView(imageView)//화면에서 뷰 제거
        viewModel.viewList!!.remove(imageView)//뷰 객체리스트에서 뷰 제거
        deleteEditText(binding.addLinear.get(position) as EditText, position-1)//에디트 텍스트 삭제하고 합치기
    }


    private fun addView(position: Int) {//포지션: 포커스 현재위치
        binding.addLinear.addView(
            viewModel.viewList!!.get(position),//최초 생성이면 list에 한개가 들어가있지만 position은 -1이 넘어와서 1더해줘야 한다.
            position + viewModel.childCount
        )
    }

    private fun getViewPosition(): Int {//body첫번째 에디트가 position = 0 이다.
        return viewModel.nowEditPosition
    }

    private fun addViewAuto(position: Int, bitmap: Bitmap) {//난중에 여유있으면 리사이클러뷰로 구현
        makeImageView(position, bitmap)//본문 구조는(최초 에디트 -> (이미지 -> 에디트 반복 구조))
        addView(position)
        makeEditText(position + 1)//이미지 뷰 추가되어서 포지션+1
        addView(position + 1)
    }

    private fun addViewAuto2(position: Int, uri: String) {//세부사항 액티비티에서 넘어왔을때
        makeImageView2(position, uri)
        addView(position)
        makeEditText(position + 1)//이미지 뷰 추가되어서 포지션+1
        addView(position + 1)
    }

    private val focusEvent = object : View.OnFocusChangeListener {
        override fun onFocusChange(v: View?, hasFocus: Boolean) {//현재 포커싱된 에디트텍스트, 포지션 저장
            viewModel.nowEditText = v as EditText
            viewModel.nowEditPosition = viewModel.viewList!!.indexOf(viewModel.nowEditText!!) + 1
        }//신규 생성된 뷰들은 position이 1이상 이다.
    }

    private val clickEvent = object : View.OnLongClickListener {
        override fun onLongClick(v: View?): Boolean {
            deleteDialog(v as ImageView)
            return true
        }
    }

    private fun deleteDialog(imageView: ImageView) {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            this.setMessage("삭제하시겠습니까?")
            this.setNegativeButton("NO") { _, _ -> }
            this.setPositiveButton("YES") { _, _ ->
                deleteImageView(imageView)//???
            }
        }
        builder.show()
    }

    //기존 정보 적용
    private fun applyExistingInfo(item: OffDetailItem){
        viewModel.setItem(item)

        binding.showmap.background = null
        binding.editbody.setText(item.body.get(0))
        if (item.imageURL.size != 0) {
            if (item.imageURL.get(0) != "") {
                for (i in item.imageURL) {//이미지 한개당 (에디트+이미지)뷰 추가
                    addViewAuto2(item.imageURL.indexOf(i) * 2, i)//뷰가 2개씩 추가되어서 인덱스*2
                }
            }
            var index = 1
            for (i in viewModel.viewList!!){
                if(i is EditText){//새로 생성한 에디트에 본문내용 부착
                    i.setText(item.body.get(index))
                    index++
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.referenceClear()//참조 해제
    }
}