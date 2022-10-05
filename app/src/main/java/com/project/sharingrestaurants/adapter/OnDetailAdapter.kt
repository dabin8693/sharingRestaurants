package com.project.sharingrestaurants.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.databinding.*
import com.project.sharingrestaurants.firebase.BoardEntity
import com.project.sharingrestaurants.firebase.CommentEntity
import com.project.sharingrestaurants.firebase.ReplyEntity
import com.project.sharingrestaurants.viewmodel.OnDetailViewModel
import java.text.SimpleDateFormat

class OnDetailAdapter(private val entity: BoardEntity, val viewModel: OnDetailViewModel) : RecyclerView.Adapter<OnDetailAdapter.ViewHolder>() {
    private val viewTypeList: ArrayList<String> = ArrayList()
    private lateinit var commentItems: List<Any>//CommentEntity, ReplyEntity
    private var commentsSize: Int = 0
    private lateinit var binding: ViewDataBinding
    private lateinit var context: Context

    init {
        viewTypeList.add("head")
        for (index in 0 until (entity.body.size-1)){
            viewTypeList.add("text")
            viewTypeList.add("image")
        }
        viewTypeList.add("text")
        viewTypeList.add("footer")
    }

    override fun getItemViewType(position: Int): Int {
        when(viewTypeList.get(position)){
            "head" -> return 0
            "text" -> return 1
            "image" -> return 2
            "footer" -> return 3
            "comment" -> return 4
            "reply" -> return 5
        }
        return 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        when (viewType) {
            0 -> {//head
                binding = OnDetailHeadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolder(binding, viewType)
            }
            1 -> {//text
                binding = OffItemTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolder(binding, viewType)
            }
            2 -> {//image
                binding = OffItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolder(binding, viewType)
            }
            3 -> {//footer
                binding = OnDetailFooterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolder(binding, viewType)
            }
            4 -> {//comment
                binding = OnDetailCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolder(binding, viewType)
            }
            5 -> {//reply
                binding = OnDetailReplyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolder(binding, viewType)
            }
        }
        binding = OffItemTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return viewTypeList.size
    }
/////////////////////////////////////////////////////
    //비동기 호출
    fun setBodyItem(item: BoardEntity){
        entity.look = item.look//조회수
        entity.profileImage = item.profileImage//프로필 이미지
        entity.nickname = item.nickname//프로필 닉네임
        entity.comments = item.comments//댓글 수
        notifyDataSetChanged()
        //notifyItemChanged(0)
    }

    fun setLookItem(item: BoardEntity){
        entity.look = item.look//조회수
        entity.comments = item.comments//댓글 수
        notifyDataSetChanged()
        //notifyItemChanged(0)
    }

    fun setCommentItem(commentItems: List<Any>) {
        val size: Int = viewTypeList.size
        this.commentItems = commentItems//댓글,답글 데이터 담기
        for (obj in commentItems) {
            if (obj is CommentEntity){
                viewTypeList.add("comment")
            }else{
                viewTypeList.add("reply")
            }
        }
        commentsSize = viewTypeList.size - size
        notifyDataSetChanged()
        //notifyItemRangeChanged(size, commentItems.size)
    }
    //비동기 호출
/////////////////////////////////////////////////////
    inner class ViewHolder(val binding: ViewDataBinding, val viewType: Int): RecyclerView.ViewHolder(binding.root){

        fun bind(position: Int){
            when(viewType){
                0 -> {//head
                    binding as OnDetailHeadBinding
                    binding.title.text = entity.tilte
                    binding.locate.text = entity.locate
                    binding.place.text = entity.place
                    binding.rating.rating = entity.priority
                    if (!entity.profileImage.equals("")) {
                        Glide.with(itemView)//uid비교 //나중에 비동기 초기화
                            .load(entity.profileImage)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .override(360, 640)
                            .into(binding.profileimage)
                            .onLoadFailed(
                                ResourcesCompat.getDrawable(
                                    context.resources,
                                    R.mipmap.ic_launcher,
                                    null
                                )
                            )
                    }
                    binding.profilenickname.text = entity.nickname//uid비교//나중에 비동기 초기화
                    val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
                    binding.time.text = format.format(entity.timestamp)
                    binding.look.text = entity.look.toString()//나중에 비동기 초기화
                }
                1 -> {//text
                    binding as OffItemTextBinding
                    binding.title.text = entity.body.get((position-1)/2)//1,3,5,7 //0,1,2,3
                }
                2 -> {//image
                    binding as OffItemImageBinding
                    Glide.with(itemView)
                        .load(viewModel.getStorageRef().child(entity.image.get((position/2)-1).substring(1)))//2,4,6,8 //0,1,2,3
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .override(360,640)
                        .into(binding.image)
                        .onLoadFailed(
                            ResourcesCompat.getDrawable(
                                context.resources,
                                R.mipmap.ic_launcher,
                                null
                            )
                        )
                }
                3 -> {//footer
                    binding as OnDetailFooterBinding
                    binding.like.setOnClickListener {
                        viewModel.incrementLike(entity.documentId)
                        entity.like++
                        binding.likenum.text = entity.like.toString()
                        viewModel.isRecomment = true
                        it.isClickable = false
                    }//추천클릭
                    binding.likenum.text = entity.like.toString()
                    binding.comments.text = entity.comments.toString()//나중에 비동기 초기화
                    binding.send.setOnClickListener {  }//댓글 전송 클릭
                }
                4 -> {//댓글
                    binding as OnDetailCommentBinding
                    val firstCommentPosition: Int = viewTypeList.size - commentsSize
                    val commentEntity: CommentEntity = commentItems.get(position-firstCommentPosition) as CommentEntity
                    binding.profilenickname.text = commentEntity.nickname //uid비교
                    Glide.with(itemView)//uid비교
                        .load(commentEntity.profileImage)
                        .override(360,640)
                        .into(binding.profileimage)
                        .onLoadFailed(
                            ResourcesCompat.getDrawable(
                                context.resources,
                                R.mipmap.ic_launcher,
                                null
                            )
                        )
                    binding.body.text = commentEntity.body
                    val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
                    binding.time.text = format.format(commentEntity.timestamp)
                    binding.replybutton.visibility = View.VISIBLE //uid비교
                    binding.insertbutton.visibility = View.GONE //uid비교
                    binding.deletebutton.visibility = View.GONE //uid비교
                }
                5 -> {//답글
                    binding as OnDetailReplyBinding
                    val firstCommentPosition: Int = viewTypeList.size - commentsSize
                    val replyEntity: ReplyEntity = commentItems.get(position-firstCommentPosition) as ReplyEntity
                    binding.profilenickname.text = replyEntity.nickname //uid비교
                    Glide.with(itemView)//uid비교
                        .load(replyEntity.profileImage)
                        .override(360,640)
                        .into(binding.profileimage)
                        .onLoadFailed(
                            ResourcesCompat.getDrawable(
                                context.resources,
                                R.mipmap.ic_launcher,
                                null
                            )
                        )
                    binding.yournickname.text = "@"+replyEntity.commentNickname
                    binding.body.text = replyEntity.body
                    val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
                    binding.time.text = format.format(replyEntity.timestamp)
                    binding.replybutton.visibility = View.VISIBLE //uid비교
                    binding.insertbutton.visibility = View.GONE //uid비교
                    binding.deletebutton.visibility = View.GONE //uid비교
                }
            }
        }
    }
}