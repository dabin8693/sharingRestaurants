package com.project.sharingrestaurants.ui.on

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.sharingrestaurants.LifecycleTest
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
    //private lateinit var addCallBack: ActivityResultLauncher<Intent>//OnItemAdd액티비티에 대한 콜백

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getLifecycle().addObserver(LifecycleTest("activityOnDetail"))
        initStart()
        item = intent.getSerializableExtra("BoardEntity") as BoardEntity
        viewModel.isLike = false
        viewModel.setLikeDrawable(this,false)

        viewModel.incrementLook(item.documentId)//조회수 증가
        if (viewModel.getIsLogin()) {//로그인 상태일때 초기화
            viewModel.getWriteCommentList()
            viewModel.getLikeListAuth(this)
            viewModel.nicknameMap.set(viewModel.getAuth().email!!, viewModel.getAuth().nickname)
            viewModel.profileImageMap.set(viewModel.getAuth().email!!, viewModel.getAuth().profileImage)
            if (item.uid.equals(viewModel.getAuth().uid)) {//내가 작성한 글일 경우
                binding.insert.visibility = View.VISIBLE
                binding.delete.visibility = View.VISIBLE
                item.nickname = viewModel.getAuth().nickname
                item.profileImage = viewModel.getAuth().profileImage
            }
        }

        adapter = OnDetailAdapter(item, viewModel, viewModel.isLike, this)
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
        viewModel.getLoadCommentData(item.documentId).observe(this){//처음 가져올때
            adapter.setCommentItem(it)//댓글 목록 가져오기
        }
        viewModel.getCommentObserver().observe(this){//댓글 추가될때
            viewModel.getLoadCommentData(item.documentId).observe(this){
                adapter.setCommentItem(it)//댓글 목록 가져오기
            }
        }

        binding.insert.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val item = viewModel.getBoard(item.documentId)
                val intent = Intent(this@OnItemDetailShowActivity, OnItemAddActivity::class.java)
                intent.putExtra("BoardEntity", item)
                startActivity(intent)
                finish()
            }
        }

        binding.delete.setOnClickListener {
            //remove
        }

        binding.back.setOnClickListener {
            finish()
        }
    }

    private fun initStart(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_on_item_detail_show)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

    }

    override fun onRestart() {
        super.onRestart()

    }
}