package com.project.sharingrestaurants.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
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

class OnDetailAdapter(
    private val entity: BoardEntity,
    val viewModel: OnDetailViewModel,
    var isLike: Boolean,
    val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<OnDetailAdapter.ViewHolder>() {
    private val viewTypeList: ArrayList<String> = ArrayList()
    private var commentItems: List<Any> = emptyList()//CommentEntity, ReplyEntity
    private var commentsSize: Int = 0
    private lateinit var binding: ViewDataBinding
    private lateinit var context: Context
    private lateinit var head: ViewHolder

    init {
        viewTypeList.add("head")
        for (index in 0 until (entity.body.size - 1)) {
            viewTypeList.add("text")
            viewTypeList.add("image")
        }
        viewTypeList.add("text")
        viewTypeList.add("footer")
    }

    override fun getItemViewType(position: Int): Int {
        when (viewTypeList.get(position)) {
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
                binding =
                    OnDetailHeadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                head = ViewHolder(binding, viewType)
                return head
            }
            1 -> {//text
                binding =
                    OffItemTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolder(binding, viewType)
            }
            2 -> {//image
                binding =
                    OffItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolder(binding, viewType)
            }
            3 -> {//footer
                binding = OnDetailFooterBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                (binding as OnDetailFooterBinding).viewModel = viewModel
                binding.lifecycleOwner = lifecycleOwner
                return ViewHolder(binding, viewType)
            }
            4 -> {//comment
                binding = OnDetailCommentBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return ViewHolder(binding, viewType)
            }
            5 -> {//reply
                binding =
                    OnDetailReplyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
    fun setUserItem(item: BoardEntity) {
        entity.profileImage = item.profileImage//프로필 이미지
        entity.nickname = item.nickname//프로필 닉네임
        if (::head.isInitialized) {
            head.bind(0)//position값은 상관 없음
        }
        //notifyItemChanged(0)
    }

    fun setCommentItem(commentItems: List<Any>) {
        Log.d("댓글사이즈", commentItems.size.toString())
        val size: Int =
            viewTypeList.size - this.commentItems.size//처음 초기화가 아닐수도 있어서 commentItems.size를 빼준다
        if (this.commentItems.size>0) {//이전 댓글이 있을 경우
            for (i in 1..this.commentItems.size) {//이전 댓글들을 빼준다
                this.viewTypeList.removeLast()
            }
        }
        this.commentItems = commentItems//댓글,답글 데이터 담기
        for (obj in commentItems) {
            if (obj is CommentEntity) {
                viewTypeList.add("comment")
            } else {
                viewTypeList.add("reply")
            }
        }
        commentsSize = viewTypeList.size - size
        notifyDataSetChanged()
        //notifyItemRangeChanged(size, commentItems.size)
    }

    //비동기 호출
/////////////////////////////////////////////////////
    inner class ViewHolder(val binding: ViewDataBinding, val viewType: Int) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            when (viewType) {
                0 -> {//head
                    binding as OnDetailHeadBinding
                    binding.title.text = entity.tilte
                    binding.locate.text = entity.locate
                    binding.place.text = entity.place
                    binding.rating.rating = entity.priority
                    if (!entity.profileImage.equals("")) {
                        Glide.with(itemView)
                            .load(entity.profileImage)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
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
                    binding.profilenickname.text = entity.nickname
                    val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
                    binding.time.text = format.format(entity.timestamp)
                    binding.look.text = entity.look.toString()//나중에 비동기 초기화
                }
                1 -> {//text
                    binding as OffItemTextBinding
                    binding.title.text = entity.body.get((position - 1) / 2)//1,3,5,7 //0,1,2,3
                }
                2 -> {//image
                    binding as OffItemImageBinding
                    Glide.with(itemView)
                        .load(
                            viewModel.getStorageRef()
                                .child(entity.image.get((position / 2) - 1).substring(1))
                        )//2,4,6,8 //0,1,2,3
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .override(360, 640)
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
                    if (viewModel.getIsLogin()) {
                        binding.like.setOnClickListener {
                            if (viewModel.likeIsUpdate.value == true) {
                                if (!isLike) {
                                    viewModel.incrementLike(entity.documentId)
                                    entity.like++
                                    viewModel.likes.value = entity.like.toString()
                                    viewModel.isLike = true
                                    isLike = true
                                    viewModel.setLikeDrawable(context, true)
                                    //it.isClickable = false
                                } else {
                                    viewModel.decrementLike(entity.documentId)
                                    entity.like--
                                    viewModel.likes.value = entity.like.toString()
                                    viewModel.isLike = false
                                    isLike = false
                                    viewModel.setLikeDrawable(context, false)
                                }
                            }//추천클릭
                        }
                        binding.send.setOnClickListener {
                            viewModel.addComment(entity.documentId)
                        }//댓글 전송 클릭
                    }
                    viewModel.likes.value = entity.like.toString()
                    viewModel.comments.value = entity.comments.toString()

                }
                4 -> {//댓글
                    binding as OnDetailCommentBinding
                    val firstCommentPosition: Int = viewTypeList.size - commentsSize
                    val commentEntity: CommentEntity =
                        commentItems.get(position - firstCommentPosition) as CommentEntity
                    if (!commentEntity.isDelete) {
                        binding.profilenickname.text = commentEntity.nickname
                        Log.d("프로필이미지", commentEntity.profileImage)
                        Glide.with(itemView)//uid비교
                            .load(commentEntity.profileImage)
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
                        binding.body.text = commentEntity.body
                        val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
                        binding.time.text = format.format(commentEntity.timestamp)
                        if (commentEntity.email.equals(viewModel.getAuth().email)) {//내가 작성한 댓글이면
                            binding.replybutton.visibility = View.GONE
                            binding.insertbutton.visibility = View.VISIBLE
                            binding.deletebutton.visibility = View.VISIBLE
                        }
                    } else {
                        binding.time.visibility = View.GONE
                        binding.profilenickname.visibility = View.GONE
                        binding.profileimage.visibility = View.GONE
                        binding.replybutton.visibility = View.GONE
                        binding.insertbutton.visibility = View.GONE
                        binding.deletebutton.visibility = View.GONE
                    }
                }
                5 -> {//답글
                    binding as OnDetailReplyBinding
                    val firstCommentPosition: Int = viewTypeList.size - commentsSize
                    val replyEntity: ReplyEntity =
                        commentItems.get(position - firstCommentPosition) as ReplyEntity
                    if (!replyEntity.isDelete) {
                        binding.profilenickname.text = replyEntity.nickname //uid비교
                        Glide.with(itemView)//uid비교
                            .load(replyEntity.profileImage)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .override(360, 640)
                            .into(binding.profileimage)
                            .onLoadFailed(
                                ResourcesCompat.getDrawable(
                                    context.resources,
                                    R.mipmap.ic_launcher,
                                    null
                                )
                            )
                        binding.yournickname.text = "@" + replyEntity.commentNickname
                        binding.body.text = replyEntity.body
                        val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
                        binding.time.text = format.format(replyEntity.timestamp)
                        if (replyEntity.email.equals(viewModel.getAuth().email)) {
                            binding.replybutton.visibility = View.GONE
                            binding.insertbutton.visibility = View.VISIBLE
                            binding.deletebutton.visibility = View.VISIBLE
                        }
                    } else {
                        binding.time.visibility = View.GONE
                        binding.profilenickname.visibility = View.GONE
                        binding.profileimage.visibility = View.GONE
                        binding.yournickname.visibility = View.GONE
                        binding.replybutton.visibility = View.GONE
                        binding.insertbutton.visibility = View.GONE
                        binding.deletebutton.visibility = View.GONE
                    }
                }
            }
        }
    }
}