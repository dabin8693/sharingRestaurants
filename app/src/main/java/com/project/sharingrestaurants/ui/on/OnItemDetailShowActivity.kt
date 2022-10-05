package com.project.sharingrestaurants.ui.on

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.adapter.OnDetailAdapter
import com.project.sharingrestaurants.databinding.ActivityOnItemDetailShowBinding
import com.project.sharingrestaurants.firebase.BoardEntity
import com.project.sharingrestaurants.viewmodel.OnDetailViewModel
import com.project.sharingrestaurants.viewmodel.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OnItemDetailShowActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnItemDetailShowBinding
    private lateinit var adapter: OnDetailAdapter
    val viewModel: OnDetailViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory(MyApplication.REPOSITORY)).get(
            OnDetailViewModel::class.java
        )
    }
    lateinit var item: BoardEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initStart()
        item = intent.getSerializableExtra("BoardEntity") as BoardEntity
        viewModel.incrementLook(item.documentId)
        if (viewModel.getIsLogin()) {
            viewModel.nicknameMap.set(viewModel.getAuth().email!!, viewModel.getAuth().nickname)
            if (item.uid.equals(viewModel.getAuth().uid)) {//내가 작성한 글일 경우
                binding.insert.visibility = View.VISIBLE
                binding.delete.visibility = View.VISIBLE
                item.nickname = viewModel.getAuth().nickname
                item.profileImage = viewModel.getAuth().profileImage
            }
        }

        adapter = OnDetailAdapter(item, viewModel)
        binding.recycle.apply {
            this.adapter = this@OnItemDetailShowActivity.adapter
            this.layoutManager =
                LinearLayoutManager(this@OnItemDetailShowActivity, RecyclerView.VERTICAL, false)
            this.setHasFixedSize(true)//사이즈 측정이 필요없다 판단돼면 내부적으로 measure안한다
        }

        if (viewModel.getIsLogin()) {
            if (!item.uid.equals(viewModel.getAuth().uid)) {//내가 작성한 글이 아닐경우
                viewModel.getBoardUser(item.email).observe(this) {
                    adapter.setUserItem(it)//닉네임, 프로필이미지 가져오기
                }
            }
        }
        viewModel.getLoadCommentData(item.documentId).observe(this){
            adapter.setCommentItem(it)//댓글 목록 가져오기
        }

        binding.insert.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val item = viewModel.getBoard(item.documentId)
                val intent = Intent(this@OnItemDetailShowActivity, OnItemAddActivity::class.java)
                intent.putExtra("BoardEntity", item)
                startActivity(intent)
            }
        }

        binding.delete.setOnClickListener {
            //remove
        }
    }

    private fun initStart(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_on_item_detail_show)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

    }


}