package com.project.sharingrestaurants.adapter


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.databinding.OnItemBinding
import com.project.sharingrestaurants.firebase.BoardEntity
import com.project.sharingrestaurants.room.ItemEntity
import com.project.sharingrestaurants.util.DataTrans
import com.project.sharingrestaurants.viewmodel.OffLineViewModel
import com.project.sharingrestaurants.viewmodel.OnLineViewModel


class OnAdapter(val itemClick: (BoardEntity) -> Unit, var currentLatitude: Double, var currentLongitude: Double) :  RecyclerView.Adapter<OnAdapter.ViewHolder>(){
    private var items: List<BoardEntity> = listOf()
    private val viewHolderList: ArrayList<OnAdapter.ViewHolder> = ArrayList()
    private lateinit var context: Context


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {//뷰홀더 생성
        val binding = OnItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewholer = ViewHolder(binding)
        viewHolderList.add(viewholer)
        context = parent.context
        return viewholer
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {//뷰홀더에 데이터 바인딩
        holder.bind(items[position])
        Log.d("바인드 뷰홀더","ㅇㅇ")
    }

    fun distChanged(currentLatitude: Double, currentLongitude: Double){//위치 가져오기 비동기 처리 끝난후 호출됨
        this.currentLatitude = currentLatitude
        this.currentLongitude = currentLongitude
        for (i in viewHolderList){
            viewHolderList.get(viewHolderList.indexOf(i)).changeDistance()
        }
    }


    fun setItems(items: List<BoardEntity>) {//아이템 체인지 이벤트 보내기
        Log.d("셋아이템","ㅇㅇ")
        this.items = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: OnItemBinding) : RecyclerView.ViewHolder(binding.root){
        lateinit var item: BoardEntity
        fun bind(item: BoardEntity) {//뷰홀더에 데이터 바인딩
            // 아이템 상세 정보로 이동
            Log.d("바인드 뷰홀더22","ㅇㅇ")
            //binding.distance.setText("6.2km")
            binding.title.setText(item.tilte)
            binding.number.setText(item.recommends.toString())
            binding.place.setText(item.place)
            binding.ratingBar.rating = item.priority
            //binding.title.setText("수원역 죠스떡볶이 맛집추천")
            //binding.place.setText("6번출구 앞 죠스떡볶이")
            //binding.number.setText("2")
            //binding.ratingBar.rating = 5F
            Glide.with(itemView)
                .load(item.image)//첫번째 사진만 보여준다
                .override(180,180)
                .into(binding.picture)
                .onLoadFailed(
                    ResourcesCompat.getDrawable(
                    context.resources,
                    R.mipmap.ic_launcher,
                    null)
                )
            itemView.setOnClickListener {
                itemClick(item)
            }



        }

        fun changeDistance(){
            binding.distance.text = DataTrans.calDist(item.latitude,item.longitude,
                currentLatitude, currentLongitude
            ).toString()+"km"
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }


}