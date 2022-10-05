package com.project.sharingrestaurants.viewmodel

import android.app.Activity
import androidx.core.os.trace
import androidx.lifecycle.*
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.storage.StorageReference
import com.project.sharingrestaurants.firebase.BoardEntity
import com.project.sharingrestaurants.firebase.CountEntity
import com.project.sharingrestaurants.firebase.UserEntity
import com.project.sharingrestaurants.room.ItemRepository
import com.project.sharingrestaurants.util.DataTrans
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OnLineViewModel(private val repository: ItemRepository): ViewModel() {

    val currentLatitude: MutableLiveData<Double> = MutableLiveData()
    val currentLongitude: MutableLiveData<Double> = MutableLiveData()

    fun getList(): LiveData<List<BoardEntity>>{
        val liveData: MutableLiveData<List<BoardEntity>> = MutableLiveData()
        CoroutineScope(Dispatchers.Main).launch {
            val boardList = repository.getBoardList() as ArrayList
            val countList = repository.getCountList()
            val newBoardList = boardList.clone() as ArrayList<BoardEntity>
            var a = 0
            for (board in boardList){
                var b = 0
                for (count in countList) {
                    if (board.documentId.equals(count.boardId)) {
                        val count = countList.get(a)
                        board.like = count.like
                        board.likeUsers = count.likeUsers
                        board.look = count.look
                        board.comments = count.comments
                        b++
                        break
                    }
                }
                if (b == 0){//해당 글에 해당하는 count가 없으면 목록 리스트에서 해당 글 삭제
                    newBoardList.removeAt(a)
                }
                a++
            }
            liveData.value = newBoardList
        }
        return liveData
    }

    suspend fun getBoard(boardId: String): BoardEntity{
        val board = repository.getBoard(boardId)
        val count = repository.getCount(boardId)
        if (!board.equals("")) {
            if (!count.equals("")) {
                board.like = count.like
                board.likeUsers = count.likeUsers
                board.look = count.look
                board.comments = count.comments
                return board
            }
            return BoardEntity()//삭제된 글
        }else{
            return BoardEntity()//삭제된 글
        }
    }

    fun getCurrentGPS(activity: Activity?): LiveData<DataTrans.gps> {
        return DataTrans.requestLastLocation(activity!!)
    }

    fun signIn(account: GoogleSignInAccount, activity: Activity?, callback: () -> Unit) {
        repository.signInGoogle(account, activity!!, callback)
    }
    fun getIsLogin(): Boolean {
        return repository.getIsLogin()
    }
    fun getAuth(): UserEntity {
        return repository.getAuth()
    }

    fun addFBAuth(){
        CoroutineScope(Dispatchers.Main).launch {
            repository.addFBAuth()//회원정보 추가
        }
    }

    fun getGoogleSignInClient(): GoogleSignInClient {
        return repository.getGoogleSignInClient()
    }

    fun getStorageRef(): StorageReference{
        return repository.getFBStorageRef()
    }
}