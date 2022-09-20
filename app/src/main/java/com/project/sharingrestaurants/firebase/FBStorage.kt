package com.project.sharingrestaurants.firebase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class FBStorage {

    fun addImageSaved(uris: String): LiveData<String>{
        val liveData: MutableLiveData<String> = MutableLiveData()

        return liveData
    }
}