package com.project.sharingrestaurants.custom

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.project.sharingrestaurants.R//수동으로
import com.project.sharingrestaurants.databinding.SpinnerInnerViewBinding
import com.project.sharingrestaurants.databinding.SpinnerOuterViewBinding


class CustomSpinner(context: Context, list: List<String>) : BaseAdapter() {//안씀
    lateinit var text: String
    val list = list
    val context = context
    val inflater: LayoutInflater =
        (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
    lateinit var bindingIn: SpinnerInnerViewBinding
    lateinit var bindingOut: SpinnerOuterViewBinding

    override fun getCount(): Int {
        if (list != null)
            return list.size
        else
            return 0;
    }

    override fun getItem(position: Int): Any {
        return list.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    // 화면에 들어왔을 때 보여지는 텍스트뷰 설정
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        bindingOut = SpinnerOuterViewBinding.inflate(inflater, parent, false)
        var newView: View ?= null
        if (convertView == null) {//레이아웃 인플레이터
            //newView = inflater.inflate(R.layout.spinner_outer_view, parent, false)
            newView = bindingOut.root
        }else{
            newView = convertView
        }
        if (list != null) {//스피너 목록이 존재하면 초기값 설정
            text = list.get(position)
            bindingOut.spinnerInnerText.setText(text)
        }
        return newView!!
    }
    // 클릭 후 나타나는 텍스트뷰 설정
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        bindingIn = SpinnerInnerViewBinding.inflate(inflater, parent, false)
        var newView: View ?= null
        if (convertView == null) {//레이아웃 인플레이터
            //newView = inflater.inflate(R.layout.spinner_outer_view, parent, false)
            newView = bindingIn.root
        }else{
            newView = convertView
        }
        if (list != null) {//스피너 목록이 존재하면 초기값 설정
            text = list.get(position)
            bindingIn.spinnerText.setText(text)
        }
        return newView!!
    }
}