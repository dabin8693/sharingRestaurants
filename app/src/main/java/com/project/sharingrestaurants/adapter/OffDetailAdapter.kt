package com.project.sharingrestaurants.adapter

import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.data.OffDetailItem
import com.project.sharingrestaurants.databinding.*
import com.project.sharingrestaurants.room.ItemEntity

class OffDetailAdapter() : RecyclerView.Adapter<OffDetailAdapter.ViewHolder>() {
    private var items: List<String> = listOf()//
    private lateinit var binding: ViewDataBinding

    override fun getItemViewType(position: Int): Int {
        Log.d("타입생성호출","")
        when (position) {
            0 -> {
                return 0//text(title)
            }
            1 -> {
                return 1//text(locate)
            }
            2 -> {
                return 2//text(place)
            }
            3 -> {
                return 3//rating(rating)
            }
            4 -> {
                return 4//text(body)
            }
        }
        if (position % 2 == 0) {//4:text(body) 5:image
            return 4
        } else {
            return 5
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("뷰홀더생성호출","")

        when (viewType) {
            0 -> {
                binding = OffItemTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)//text
                return ViewHolder(binding, viewType)
            }
            1 -> {
                binding = OffItemTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)//text
                return ViewHolder(binding, viewType)
            }
            2 -> {
                binding = OffItemTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)//text
                return ViewHolder(binding, viewType)
            }
            3 -> {
                binding = OffItemRatingBinding.inflate(LayoutInflater.from(parent.context), parent, false)//rating
                return ViewHolder(binding, viewType)
            }
            4 -> {
                binding = OffItemTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)//text
                return ViewHolder(binding, viewType)
            }
        }
        Log.d("뷰홀더 예외","")
        binding = OffItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)//image
        return ViewHolder(binding, viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("뷰홀더호출",position.toString())
        holder.bind(items.get(position))
    }

    override fun getItemCount(): Int {
        Log.d("카운트호출",items.size.toString())
        return items.size
    }

    fun setItemList(array: List<String>) {
        this.items = array
        notifyDataSetChanged()
    }
//list -> arraylist x //arraylist -> list o //list -> mutablelist o
    fun setItem(items: OffDetailItem) {
        val array = ArrayList<String>()
        array.add(items.title)
        array.add(items.locate)
        array.add(items.place)
        array.add(items.priority.toString())
        for (i in 0..items.body.size - 1) {
            array.add(items.body.get(i))
            if (i < items.body.size - 1) {
                array.add(items.imageURL.get(i))
            }
        }

        setItemList(array)
    }

    inner class ViewHolder(binding: ViewDataBinding, viewType: Int) : RecyclerView.ViewHolder(binding.root) {
        val viewType = viewType//0,1,2 = textview //3 = ratingbar //4 = textview //5 = imageview
        val binding = binding

        fun bind(item: String) {//type이 4,5 뷰홀더는 이후 포지션에서 재활용 됨 //0,1,2,3는 포지션이 0,1,2,3일 때만 재활용됨 그래서 뷰 속성 수정했을때 안 바꿔도 됨
            Log.d("뷰홀더",viewType.toString())
            when (viewType) {
                0 -> {
                    val temp = binding as OffItemTextBinding
                    temp.title.text = item
                    temp.title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25F)
                }
                1 -> {
                    val temp = binding as OffItemTextBinding
                    temp.title.text = item
                    temp.title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20F)
                }
                2 -> {
                    val temp = binding as OffItemTextBinding
                    temp.title.text = item
                    temp.title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20F)
                }
                3 -> {
                    val temp = binding as OffItemRatingBinding
                    temp.ratingBar.rating = item.toFloat()
                }
                4 -> {
                    val temp = binding as OffItemTextBinding
                    temp.title.text = item
                    temp.title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20F)
                }
                5 -> {
                    Glide.with(itemView)
                        .load(item).override(360,640)
                        .into((binding as OffItemImageBinding).image)

                }
            }
        }
    }

}