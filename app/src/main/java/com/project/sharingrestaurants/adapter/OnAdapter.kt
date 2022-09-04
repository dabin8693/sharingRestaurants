package com.project.sharingrestaurants.adapter


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.sharingrestaurants.databinding.OnItemBinding
import com.project.sharingrestaurants.room.ItemEntity
import com.project.sharingrestaurants.viewmodel.OffLineViewModel
import com.project.sharingrestaurants.viewmodel.OnLineViewModel


class OnAdapter(val viewModel: OnLineViewModel, val lifecycleOwner: LifecycleOwner) :  RecyclerView.Adapter<OnAdapter.ViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {//뷰홀더 생성
        val binding = OnItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = lifecycleOwner
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {//뷰홀더에 데이터 바인딩
        holder.bind(position)
    }



    inner class ViewHolder(private val binding: OnItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(position: Int) {//뷰홀더에 데이터 바인딩
            // 아이템 상세 정보로 이동
            itemView.setOnClickListener {

            }


            Log.d("포지션",position.toString())
        }
    }

    override fun getItemCount(): Int {
        return 0
    }
}