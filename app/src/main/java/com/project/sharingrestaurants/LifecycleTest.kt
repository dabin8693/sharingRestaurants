package com.project.sharingrestaurants

import android.util.Log
import androidx.lifecycle.*

class LifecycleTest(val name: String): LifecycleEventObserver {
    private val TAG = "[DEBUG]" + name


    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        Log.d(TAG, event.name)
        //fragment는 onAttack, onDetach감지 안됨
    }
}