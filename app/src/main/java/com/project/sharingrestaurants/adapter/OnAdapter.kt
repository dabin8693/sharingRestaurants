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
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.storage.StorageReference
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.databinding.OnItemBinding
import com.project.sharingrestaurants.firebase.BoardEntity
import com.project.sharingrestaurants.room.ItemEntity
import com.project.sharingrestaurants.util.ConstValue.DELIMITER
import com.project.sharingrestaurants.util.DataTrans
import com.project.sharingrestaurants.viewmodel.OffLineViewModel
import com.project.sharingrestaurants.viewmodel.OnLineViewModel


class OnAdapter(val itemClick: (BoardEntity) -> Unit, var currentLatitude: Double, var currentLongitude: Double, val storageRef: StorageReference) :  RecyclerView.Adapter<OnAdapter.ViewHolder>(){
    private var items: List<BoardEntity> = listOf()
    private val viewHolderList: ArrayList<OnAdapter.ViewHolder> = ArrayList()
    private lateinit var context: Context
    //val glide: RequestManager


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
            binding.title.setText(item.tilte)
            binding.number.setText(item.recommends.toString())
            binding.place.setText(item.place)
            binding.ratingBar.rating = item.priority
            if(item.thumb != "") {//null체크는 필요없음
                Log.d("뷰홀더 썸네일ㅁㅁ", item.thumb.substring(1))
                Log.d("ㅁㅁ", "성공")
                Glide.with(itemView)
                    //캐시 쓸때 참조카운터로 캐시를 유지해서 캐시 허용옵션으로 적용하면 프래그먼트 재로딩시 이미지 호출 안됨(캐시가 비워져서)
                    //application context를 써도 캐싱 실패함
                //glide
                    .load(storageRef.child(item.thumb.substring(1)))//첫번째 사진만 보여준다
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(180, 180)
                    .into(binding.picture)
                    .onLoadFailed(
                        ResourcesCompat.getDrawable(
                            context.resources,
                            R.mipmap.ic_launcher,
                            null
                        )
                    )
            }

            itemView.setOnClickListener {
                itemClick(item)
            }



        }

        fun changeDistance(){
            binding.distance.text = DataTrans.calDist(item.latitude,item.longitude,
                currentLatitude, currentLongitude
            )
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }


}