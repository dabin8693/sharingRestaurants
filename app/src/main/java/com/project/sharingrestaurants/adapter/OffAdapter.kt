package com.project.sharingrestaurants.adapter

import android.app.Activity
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.sharingrestaurants.databinding.OffItemBinding
import com.project.sharingrestaurants.room.ItemEntity
import com.project.sharingrestaurants.ui.off.OffItemAddActivity.Companion.DELIMITER
import com.project.sharingrestaurants.util.DataTrans


class OffAdapter(val itemClick: (ItemEntity, Int) -> Unit, val itemLongClick: (ItemEntity) -> Unit, var currentLatitude: Double, var currentLongitude: Double) :  RecyclerView.Adapter<OffAdapter.ViewHolder>(){
    private var items: List<ItemEntity> = listOf()
    private val viewHolderList: ArrayList<ViewHolder> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {//뷰홀더 생성
        Log.d("뷰홀더 생성","ㅇㅇ")
        val binding = OffItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewholer = ViewHolder(binding)
        viewHolderList.add(viewholer)
        return viewholer
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {//뷰홀더에 데이터 바인딩
        Log.d("뷰홀더 바인드 생성","ㅇㅇ")
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int {//리스트 총 크기 = items.size
        Log.d("아이템 사이즈","ㅇㅇ")
        return items.size
    }
    fun setItems(items: List<ItemEntity>) {//아이템 체인지 이벤트 보내기
        Log.d("셋아이템","ㅇㅇ")
        this.items = items
        notifyDataSetChanged()
    }
    fun distChanged(currentLatitude: Double, currentLongitude: Double){//위치 가져오기 비동기 처리 끝난후 호출됨
        this.currentLatitude = currentLatitude
        this.currentLongitude = currentLongitude
        for (i in viewHolderList){
            viewHolderList.get(viewHolderList.indexOf(i)).changeDistance()
        }
    }

    inner class ViewHolder(private val binding: OffItemBinding) : RecyclerView.ViewHolder(binding.root){
        //itemView = binding.root
        lateinit var item: ItemEntity
        fun bind(item: ItemEntity, position: Int) {//뷰홀더에 데이터 바인딩
            Log.d("뷰홀더 바인드","ㅇㅇ")
            this.item = item
            binding.itemName.text = item.title
            binding.itemPlace.text = item.place
            binding.itemRatingBar.rating = item.priority
            binding.itemDistance.text = DataTrans().calDist(item.latitude,item.longitude,
                currentLatitude, currentLongitude
            ).toString()+"km"

            when (item.imageURL) {
                "null" -> {
                    binding.itemImageView.visibility = View.GONE
                }
                "" -> {
                    binding.itemImageView.visibility = View.GONE
                }
                else -> {
                    val urlList: List<String> = item.imageURL.split(DELIMITER)
                    binding.itemImageView.visibility = View.VISIBLE
                    Glide.with(itemView)
                         .load(urlList[0])//첫번째 사진만 보여준다
                         .into(binding.itemImageView)
                }
            }

            // 아이템 상세 정보로 이동
            itemView.setOnClickListener {
                itemClick(item, position)//액티비티에 정의된 람다를 호출
            }

            // 아이템 삭제 다이얼로그 표시
            itemView.setOnLongClickListener {
                itemLongClick(item)
                true//return이 true면 추가적으로 click이벤트가 발생하지 않는다.
            }
        }
        fun changeDistance(){
            binding.itemDistance.text = DataTrans().calDist(item.latitude,item.longitude,
                currentLatitude, currentLongitude
            ).toString()+"km"
        }
    }
}