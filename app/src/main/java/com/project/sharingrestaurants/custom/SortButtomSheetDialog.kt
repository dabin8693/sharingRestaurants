package com.project.sharingrestaurants.custom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.project.sharingrestaurants.R
import com.project.sharingrestaurants.databinding.BottomDialogBinding


class SortButtomSheetDialog(val initialValue: String, val itemClick: (a: String) -> Unit) : BottomSheetDialogFragment() {//off프래그먼트 정렬버튼클릭시
    lateinit var binding: BottomDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(initialValue == resources.getString(R.string.spinner_item_title)){
            binding.dialogText1.isChecked = true
        }else{
            binding.dialogText2.isChecked = true
        }
        binding.dialogText1.setOnClickListener{
            binding.dialogText1.isChecked = true
            itemClick(resources.getString(R.string.spinner_item_title))
            dismiss()
        }
        binding.dialogText2.setOnClickListener{
            binding.dialogText2.isChecked = true
            itemClick(resources.getString(R.string.spinner_item_titleplace))
            dismiss()
        }
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }



}
//behavior.state종류
//STATE_EXPANDED : 완전히 펼쳐진 상태
//STATE_COLLAPSED : 접혀있는 상태
//STATE_HIDDEN : 아래로 숨겨진 상태
//STATE_HALF_EXPANDED : 절반으로 펼쳐진 상태
//STATE_DRAGGING : 드래깅 되고 있는 상태
//STATE_SETTING : 드래그/스와이프 직후 고정된 상태

//val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
//val behavior = BottomSheetBehavior.from<View>(bottomSheet!!)//바텀 시트 스타일 적용
//behavior.state = BottomSheetBehavior.STATE_EXPANDED
// behavior.addBottomSheetCallback()
//xml app:layout_behavior="@string/bottom_sheet_behavior"