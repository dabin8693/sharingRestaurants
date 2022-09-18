package com.project.sharingrestaurants.firebase

import android.app.Activity
import android.app.Application
import android.content.ContentValues
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.room.ItemRepository

class FBAuth(val context: MyApplication) {

    private var _mAuth: FirebaseAuth?= FirebaseAuth.getInstance() //싱글톤 객체임
    private var _googleSignInClient: GoogleSignInClient?= null
    private var _isLogin: MutableLiveData<Boolean> = MutableLiveData()
    private var _photoUrl: MutableLiveData<Uri> = MutableLiveData()
    val mAuth get() = _mAuth
    val googleSignInClient get() = _googleSignInClient
    val isLogin get() = _isLogin
    var currentUser: FirebaseUser?= null
    val photoUrl get() = _photoUrl


    companion object{
        private var INSTANCE: FBAuth? = null

        fun getInstance(context: MyApplication): FBAuth {//스레드 경합없음으로 synchronized필요없음
            if (INSTANCE == null){//중복 생성 방지
                INSTANCE = FBAuth(context)
            }
            return INSTANCE ?: FBAuth(context)//null이면  재생성
        }
    }

    init {
        if (mAuth!!.currentUser != null) {
            currentUser = mAuth!!.currentUser!!
            isLogin.value = true
            Log.d("로그인상태", "ㅇㅁ")
        } else {
            Log.d("로그아웃상태", "ㅇㅁ")
            isLogin.value = false
        }
        val googleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(this.context!!.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        _googleSignInClient = GoogleSignIn.getClient(this.context!!, googleSignInOptions)
    }
}