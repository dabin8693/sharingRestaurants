package com.project.sharingrestaurants.adapter

import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.net.toUri
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.data.BoardHeadEntity
import com.project.sharingrestaurants.databinding.*
import com.project.sharingrestaurants.firebase.BoardEntity
import com.project.sharingrestaurants.ui.off.ShowMapActivity
import com.project.sharingrestaurants.viewmodel.OnAddViewModel

class OnAddAdapter(val deleteImage: (Int) -> Unit, val showMap: () -> Unit, val viewModel: OnAddViewModel, val lifecycleOwner: LifecycleOwner): RecyclerView.Adapter<OnAddAdapter.ViewHolder>() {
    private var items: ArrayList<Any> = ArrayList()//.apply { add("") }//edit = "" or string(글 수정일 경우), image = url.toString //처음 생성할때 에디트 텍스트 한개 추가
    private lateinit var binding: ViewDataBinding
    private lateinit var context: Context
    private lateinit var lastEdit: EditText
    private var focusEditPosition: Int// 수정에서 넘어올때 한번 더 초기화
    private var focusImagePosition: Int// 사진 롱클릭시 업데이트

    init {
        focusEditPosition = 1
        focusImagePosition = 0
    }

    override fun getItemViewType(position: Int): Int {
        if (position == this.items.lastIndex){
            return 3//linear
        }
        if (position % 2 == 0) {//0:edit 1:image
            if (position == 0){
                return 2//head
            }
            return 1//image
        } else {
            return 0//text
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnAddAdapter.ViewHolder {
        context = parent.context
        when (viewType) {
            0 -> {
                binding = OnAddEditBinding.inflate(//텍스트
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )//text
                return ViewHolder(binding, viewType)
            }
            1 -> {
                binding = OffItemImageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )//image
                return ViewHolder(binding, viewType)
            }
            2 -> {
                binding = OnAddHeadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                (binding as OnAddHeadBinding).viewModel = viewModel
                binding.lifecycleOwner = lifecycleOwner
                return ViewHolder(binding, viewType)
            }
            3 -> {
                binding = OnItemLinearBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolder(binding, viewType)
            }
        }
        binding = OnAddEditBinding.inflate(LayoutInflater.from(parent.context), parent, false)//image
        return ViewHolder(binding, viewType)
    }

    override fun onBindViewHolder(holder: OnAddAdapter.ViewHolder, position: Int) {
        holder.bind(items.get(position), position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setItemList(array: ArrayList<Any>) {//최초 초기화 //수정창에서 넘어오면 focusEditPosition값 변경
        this.items = array//edit = "", image = url.toString
        notifyDataSetChanged()
    }

    fun addImage(string: String){//이미지 추가할때 호출
        //Log.d("어뎁터포지션2/",focusEditPosition.value.toString())
        this.items.add(focusEditPosition +1, string)//마지막 인덱스는 계속 linear가 있어야 됨 //밀려서 들어감
        notifyDataSetChanged()
    }

    //fun getPosition(): LiveData<Int> = focusEditPosition

    fun getItem(): ArrayList<Any> = items

    fun deleteItem(position: Int){
        this.items.set(position-1, this.items.get(position-1).toString() + this.items.get(position+1).toString())//에디트 텍스트 글 합치기
        this.items.removeAt(position)//이미지 삭제
        this.items.removeAt(position)//에디트텍스트 삭제
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ViewDataBinding, val viewType: Int) : RecyclerView.ViewHolder(binding.root) {//edit텍스트는 클릭리스너 말고 포커스체인지 리스너로 해야 한번터치로 응답함

        init {//초기값
            when(viewType){//head
                2 -> {
                    binding as OnAddHeadBinding
                    val item = items.get(0) as BoardHeadEntity
                    if (item.locate.equals("")) {//지도 선택x
                        viewModel.mapDrawable.value = context.resources.getDrawable(R.drawable.rectangleshape, null)
                    }else{
                        viewModel.mapDrawable.value = context.resources.getDrawable(R.drawable.empty,null)
                    }
                    viewModel.itemTitle.value = item.tilte
                    viewModel.itemPlace.value = item.place
                    viewModel.itemPriority.value = item.priority
                }
            }
        }
        fun bind(item: Any, position: Int) {//item타입 - BoardHeadEntity(data class), text(edit, image, linear)

            when (viewType) {
                0 -> {//text
                    binding as OnAddEditBinding
                    if (items.lastIndex - 1 == position) {//3 = head, edit, linear //3이상 = head, edit, (image, edit)반복, linear
                        lastEdit = binding.edit
                    }
                    binding.edit.setText(item as String)
                    binding.edit.addTextChangedListener(object : TextWatcher{//주의점!! position 지역변수 끌어다 쓰면 안됨 익명클래스라서 값이 복사됨 뷰홀더는 재활용되어서 bind함수로 넘어오는 position이랑 안 맞음
                        override fun beforeTextChanged(// 입력하기 전에 조치
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {

                        }

                        override fun onTextChanged(// 입력난에 변화가 있을 시 조치
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {

                        }

                        override fun afterTextChanged(s: Editable?) {// 입력이 끝났을 때 조치
                            //itemInput(binding.edit.text.toString(), adapterPosition/2)//edit만 따로 저장하는 list라서 포지션이 다르다
                            items.set(adapterPosition, binding.edit.text.toString())

                        }//position/2 = 중간중간 사진때문에

                    })
                    binding.edit.setOnFocusChangeListener{ _, _ ->
                        if (adapterPosition > 0) {//간혹 사진추가화 -1이 나올때 있음
                            focusEditPosition = adapterPosition
                        }
                    }
                }
                1 -> {//image
                    binding as OffItemImageBinding
                    Glide.with(itemView)
                        .load((item as String).toUri()).override(360,640)
                        .into(binding.image)
                    binding.image.setOnLongClickListener{
                        focusImagePosition = adapterPosition
                        deleteImage(adapterPosition)
                        true
                    }
                }

                2 -> {//head //livedata데이터바인딩이라 따로 데이터 담을 필요x//최초 생성할때만 초기값 설정
                    binding as OnAddHeadBinding

                    binding.showmap.setOnClickListener{
                        showMap()
                    }
                    binding.edittitle.setOnFocusChangeListener{ _, _ ->
                        focusEditPosition = 1
                        Log.d("어뎁터포지션1/",focusEditPosition.toString())
                    }
                    binding.editplace.setOnFocusChangeListener{ _, _ ->
                        focusEditPosition = 1
                        Log.d("어뎁터포지션11/",focusEditPosition.toString())
                    }
                }
                3 -> {//linear
                    binding as OnItemLinearBinding
                    binding.focuselinear.setOnClickListener{//마지막 리니어 클릭시 마지막 에디트에 포커싱 주기 //에디트 텍스트 터지 범위 확장하기 위해
                        lastEdit.requestFocus()
                        focusEditPosition = items.lastIndex - 1//마지막 에디트
                        Log.d("어뎁터포지션4/",focusEditPosition.toString())
                    }
                }
            }
        }

    }

}