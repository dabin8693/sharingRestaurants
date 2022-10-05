package com.project.sharingrestaurants.firebase

import android.app.Activity
import android.content.ContentValues
import android.util.Log
import android.widget.Toast
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

class FBAuth(val context: MyApplication) {

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance() //싱글톤 객체임
    private var googleSignInClient: GoogleSignInClient
    private var isLogin: Boolean = false
    private var currentUser: FirebaseUser?= null
    private var userEntity: UserEntity = UserEntity()



    companion object{
        private var INSTANCE: FBAuth? = null

        fun getInstance(context: MyApplication): FBAuth {//synchronized필요없음
            if (INSTANCE == null){//중복 생성 방지
                INSTANCE = FBAuth(context)
            }
            return INSTANCE ?: FBAuth(context)//null이면  재생성
        }
    }

    init {
        if (mAuth.currentUser != null) {//로그인 여부 체크
            currentUser = mAuth.currentUser
            isLogin = true
            userEntity.nickname = currentUser!!.email!!.split("@").get(0)//임시
            userEntity.uid = currentUser!!.uid
            userEntity.email = currentUser!!.email!!
            userEntity.profileImage = currentUser!!.photoUrl.toString()
        } else {
            isLogin = false
        }
        val googleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(this.context!!.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this.context!!, googleSignInOptions)
    }

    fun getUser(): UserEntity{
        return userEntity
    }

    fun getIsLogin(): Boolean{
        return isLogin
    }

    fun getGoogleSignInClient(): GoogleSignInClient{
        return googleSignInClient
    }

    // 사용자가 정상적으로 로그인한 후에 GoogleSignInAccount 개체에서 ID 토큰을 가져와서
    // Firebase 사용자 인증 정보로 교환하고 Firebase 사용자 인증 정보를 사용해 Firebase에 인증합니다.
    //activit = java.lang.ref.WeakReference(activity).get()
    fun firebaseAuthWithGoogle(acct: GoogleSignInAccount, context: Activity, callback: () -> Unit) {//리스너 중복클릭 못 하게 해야됨!!!(메모리 릭)
        //로그인 -> isAuth -false-> addAuth(db추가)
        //                -true->
        //isAuth 있으면 닉네임 가져옴 없으면 닉네임 초기값 저장
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(
                context!!,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) {//닉네이 제외하고 정보 저장
                        currentUser = mAuth.currentUser
                        // 로그인 성공
                        Toast.makeText(
                            context,
                            "로그인 성공",
                            Toast.LENGTH_SHORT
                        ).show()
                        isLogin = true
                        userEntity.profileImage = currentUser!!.photoUrl.toString()
                        userEntity.email = currentUser!!.email!!
                        userEntity.uid = currentUser!!.uid

                        callback()

                    } else {
                        // 로그인 실패
                        Toast.makeText(context, "로그인 실패", Toast.LENGTH_SHORT)
                            .show()
                    }
                })

    }


    fun signUp(email: String, password: String, context: Activity){//회원가입
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(context!!){ result ->
            if (result.isSuccessful){
                currentUser = mAuth.currentUser
            }else{
                Log.w(ContentValues.TAG, "createUserWithEmail:failure", result.getException());
                Toast.makeText(
                    context, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
            }
        }

    }

    fun signIn(email: String, password: String, context: Activity){//로그인
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(context!!){ result ->
            if (result.isSuccessful){
                currentUser = mAuth.currentUser
            }else{
                Log.w(ContentValues.TAG, "signInWithEmail:failure", result.getException());
                Toast.makeText(
                    context, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
            }
        }
    }

    fun signOut(){
        mAuth.signOut()
        isLogin = false
        currentUser = null
        userEntity = UserEntity()
    }

}