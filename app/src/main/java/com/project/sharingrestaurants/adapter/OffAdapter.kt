package com.project.sharingrestaurants.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.sharingrestaurants.databinding.OffItemBinding
import com.project.sharingrestaurants.room.ItemEntity


class OffAdapter(val itemClick: (ItemEntity) -> Unit, val itemLongClick: (ItemEntity) -> Unit) :  RecyclerView.Adapter<OffAdapter.ViewHolder>(){

    private var items: List<ItemEntity> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {//뷰홀더 생성
        val binding = OffItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {//뷰홀더에 데이터 바인딩
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {//리스트 총 크기 = items.size
        return items.size
    }
    fun setItems(items: List<ItemEntity>) {//아이템 체인지 이벤트 보내기
        this.items = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: OffItemBinding) : RecyclerView.ViewHolder(binding.root){
        //itemView = binding.root
        fun bind(item: ItemEntity) {//뷰홀더에 데이터 바인딩
            binding.itemName.text = item.title
            binding.itemPlace.text = item.place

            when (item.imageURL) {
                "null" -> {
                    binding.itemImageView.visibility = View.GONE
                }
                else -> {
                    binding.itemImageView.visibility = View.VISIBLE
                    Glide.with(itemView)
                         .load(item.imageURL)
                         .into(binding.itemImageView)
                }
            }

            // 아이템 상세 정보로 이동
            itemView.setOnClickListener {
                itemClick(item)//액티비티에 정의된 람다를 호출
            }

            // 아이템 삭제 다이얼로그 표시
            itemView.setOnLongClickListener {
                itemLongClick(item)
                true//return이 true면 추가적으로 click이벤트가 발생하지 않는다.
            }
        }
    }
}