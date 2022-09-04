package com.project.sharingrestaurants.firebase
import android.app.Activity
import android.content.ContentValues.TAG
import android.net.Uri
import android.provider.Settings.Global.getString
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
import com.project.sharingrestaurants.R


object FBAuth {

    private var activity: Activity?= null
    private var mAuth: FirebaseAuth?= null
    private var currentUser: FirebaseUser?= null
    private var _googleSignInClient: GoogleSignInClient?= null
    val googleSignInClient get() = _googleSignInClient
    private var _isLogin: MutableLiveData<Boolean> = MutableLiveData()
    val isLogin get() = _isLogin
    private var isInitialization = false


    fun initialization(activity: Activity): FBAuth{//필드 초기화
        if (!isInitialization) {//초기화 중복 방지
            Log.d("초기화","ㅇㅁ")
            mAuth = FirebaseAuth.getInstance()
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
                    .requestIdToken(activity.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

            _googleSignInClient = GoogleSignIn.getClient(activity, googleSignInOptions)
            this.activity = activity
            isInitialization = true
            return this
        }else{
            Log.d("초기화x","ㅇㅁ")
            return this
        }
    }

    fun referenceClear(){
        activity = null
        mAuth = null
        currentUser = null
        _googleSignInClient = null
        isInitialization = false
    }

    fun signOut(){
        mAuth!!.signOut()
        isLogin.value = false
    }

    // 사용자가 정상적으로 로그인한 후에 GoogleSignInAccount 개체에서 ID 토큰을 가져와서
    // Firebase 사용자 인증 정보로 교환하고 Firebase 사용자 인증 정보를 사용해 Firebase에 인증합니다.
    fun firebaseAuthWithGoogle(acct: GoogleSignInAccount, lamda: () -> Unit) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(activity!!,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) {
                        currentUser = mAuth!!.currentUser
                        // 로그인 성공
                        Toast.makeText(
                            activity,
                            "로그인 성공",
                            Toast.LENGTH_SHORT
                        ).show()
                        _isLogin.value = true
                        lamda()
                    } else {
                        // 로그인 실패
                        Toast.makeText(activity, "로그인 실패", Toast.LENGTH_SHORT)
                            .show()
                    }
                })
    }

    fun getgoogleSignInClient() = googleSignInClient

    fun signUp(email: String, password: String){//회원가입
        mAuth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener(activity!!){ result ->
            if (result.isSuccessful){
                currentUser = mAuth!!.currentUser!!
            }else{
                Log.w(TAG, "createUserWithEmail:failure", result.getException());
                Toast.makeText(activity, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
            }
        }

    }

    fun signIn(email: String, password: String){//로그인
        mAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener(activity!!){ result ->
            if (result.isSuccessful){
                currentUser = mAuth!!.currentUser!!
            }else{
                Log.w(TAG, "signInWithEmail:failure", result.getException());
                Toast.makeText(activity, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
            }
        }
    }

    fun accessUserInform(){
        if (currentUser!!.isEmailVerified){
            val name: String = currentUser!!.displayName!!
            val email: String = currentUser!!.email!!
            val photoUrl: Uri = currentUser!!.photoUrl!!
        }
    }

}