package com.project.sharingrestaurants.custom

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.SignInButton
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.firebase.FBAuth
import com.project.sharingrestaurants.generated.callback.OnClickListener

class CustomDialog(activity: Activity) : Dialog(activity) {
    private val signbutton: SignInButton
    private val finishbutton: Button


    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)//타이틀바 제거
        //getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))//배경 투명
        setContentView(R.layout.login_item)

        signbutton = findViewById(R.id.signInButton)
        finishbutton = findViewById(R.id.finish)
        FBAuth.isLogin.observe(activity as FragmentActivity){
            if (it == true){
                dismiss()
            }
        }
    }

    fun signOnClick(listener: () -> Unit){
        signbutton.setOnClickListener{listener()}
    }

    fun finshOnclick(listener: () -> Unit){
        finishbutton.setOnClickListener{listener()}
    }
}


