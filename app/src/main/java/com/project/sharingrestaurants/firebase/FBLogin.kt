package com.project.sharingrestaurants.firebase
import android.app.Activity
import android.app.Application
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings.Global.getString
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.project.sharingrestaurants.MyApplication
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.room.ItemRepository
import com.project.sharingrestaurants.util.CameraWork


class FBLogin(val Auth: FBAuth) {


    // 사용자가 정상적으로 로그인한 후에 GoogleSignInAccount 개체에서 ID 토큰을 가져와서
    // Firebase 사용자 인증 정보로 교환하고 Firebase 사용자 인증 정보를 사용해 Firebase에 인증합니다.
    //activit = java.lang.ref.WeakReference(activity).get()
    fun firebaseAuthWithGoogle(acct: GoogleSignInAccount, context: Activity, callback: () -> Unit) {//리스너 중복클릭 못 하게 해야됨!!!(메모리 릭)
        //val liveData: MutableLiveData<Boolean> = MutableLiveData()
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        Auth.mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                context!!,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) {
                        Auth.currentUser = Auth.mAuth!!.currentUser
                        // 로그인 성공
                        Toast.makeText(
                            context,
                            "로그인 성공",
                            Toast.LENGTH_SHORT
                        ).show()
                        Auth.isLogin.value = true

                        for (profile in Auth.currentUser!!.providerData) {
                            Log.d("authNumber","1")
                            Auth.photoUrl.value = profile.photoUrl!!
                        }
                        callback()//addOnCompleteListener비동기 종료후 참조 해제됨(여기가 가장 마지막으로 참조해제되는 곳)//livedata로 써도 됨
                        Log.d("포토uri:",Auth.photoUrl.value.toString())
                        //liveData.postValue(true)
                    } else {
                        // 로그인 실패
                        Toast.makeText(context, "로그인 실패", Toast.LENGTH_SHORT)
                            .show()
                        //liveData.postValue(false)
                    }
                })

    }


    fun signUp(email: String, password: String, context: Activity){//회원가입
        Auth.mAuth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener(context!!){ result ->
            if (result.isSuccessful){
                Auth.currentUser = Auth!!.currentUser!!
            }else{
                Log.w(ContentValues.TAG, "createUserWithEmail:failure", result.getException());
                Toast.makeText(
                    context, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
            }
        }

    }

    fun signIn(email: String, password: String, context: Activity){//로그인
        Auth.mAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener(context!!){ result ->
            if (result.isSuccessful){
                Auth.currentUser = Auth!!.currentUser!!
            }else{
                Log.w(ContentValues.TAG, "signInWithEmail:failure", result.getException());
                Toast.makeText(
                    context, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
            }
        }
    }

    fun signOut(){
        Auth.mAuth!!.signOut()
        Auth.isLogin.value = false
        Auth.currentUser = null
        Auth.photoUrl.value = null
    }

    fun getUserInform(){
        if (Auth.isLogin.value == true) {
            if (Auth.currentUser!!.isEmailVerified) {
                val name: String = Auth.currentUser!!.displayName!!
                val email: String = Auth.currentUser!!.email!!
                val photoUrl: Uri = Auth.currentUser!!.photoUrl!!
            }
        }
    }



}