package com.project.sharingrestaurants.adapter

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.sharingrestaurants.databinding.OffItemImageBinding
import com.project.sharingrestaurants.databinding.OffItemTextBinding
import com.project.sharingrestaurants.databinding.OnAddEditBinding

class OnAddAdapter(val itemInput: (String, Int) -> Unit): RecyclerView.Adapter<OnAddAdapter.ViewHolder>() {
    private var items: ArrayList<String> = ArrayList<String>().apply { add("") }//edit = "" or string(글 수정일 경우), image = url.toString //처음 생성할때 에디트 텍스트 한개 추가
    private lateinit var binding: ViewDataBinding

    override fun getItemViewType(position: Int): Int {
        if (position % 2 == 0) {//0:edit 1:image
            return 0
        } else {
            return 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnAddAdapter.ViewHolder {
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
                )//text
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

    fun setItemList(array: ArrayList<String>) {
        this.items = array//edit = "", image = url.toString
        notifyDataSetChanged()
    }

    fun updateItem(string: String){
        this.items.add(string)
        notifyDataSetChanged()
    }

    inner class ViewHolder(binding: ViewDataBinding, viewType: Int) : RecyclerView.ViewHolder(binding.root) {
        val viewType = viewType
        val binding = binding

        fun bind(item: String, position: Int) {

            when (viewType) {
                0 -> {
                    val temp = binding as OnAddEditBinding

                    temp.edit.setText(item)


                    temp.edit.addTextChangedListener(object : TextWatcher{//주의점!! position 지역변수 끌어다 쓰면 안됨 익명클래스라서 값이 복사됨 뷰홀더는 재활용되어서 bind함수로 넘어오는 position이랑 안 맞음
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
                            itemInput(temp.edit.text.toString(), adapterPosition/2)
                            items.set(adapterPosition, temp.edit.text.toString())

                        }//position/2 = 중간중간 사진때문에

                    })
                }
                1 -> {
                    Glide.with(itemView)
                        .load(item.toUri()).override(360,640)
                        .into((binding as OffItemImageBinding).image)
                }
            }
        }

    }

}