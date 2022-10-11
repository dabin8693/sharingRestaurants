package com.project.sharingrestaurants.viewmodel

import android.app.Activity
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

    fun getList(lifecycle: LifecycleOwner): LiveData<List<BoardEntity>>{
        val liveData: MutableLiveData<List<BoardEntity>> = MutableLiveData()
        var isChanged: Boolean = false
        repository.isChangedBoard().observe(lifecycle){
            isChanged = true
        }
        repository.isChangedCount().observe(lifecycle){
            isChanged = true
        }
        CoroutineScope(Dispatchers.Main).launch {
            var boardList: MutableList<BoardEntity>
            var countList: List<CountEntity>
            while (true){
                isChanged = false
                boardList = repository.getBoardList().toMutableList()
                countList = repository.getCountList()
                if (isChanged == false){
                    var board: BoardEntity
                    var count: CountEntity
                    for (index in 0 until boardList.size){
                        board = boardList.get(index)
                        count = countList.get(index)
                        board.like = count.like
                        board.look = count.look
                        board.comments = count.comments
                        boardList.set(index, board)
                    }
                    liveData.value = boardList
                    break
                }
            }

        }

        return liveData
    }

    suspend fun getBoard(boardId: String): BoardEntity{
        val board = repository.getBoard(boardId)
        val count = repository.getCount(boardId)
        if (!board.equals("")) {
            if (!count.equals("")) {
                board.like = count.like
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