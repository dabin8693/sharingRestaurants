package com.project.sharingrestaurants

import android.app.Application
import com.project.sharingrestaurants.room.ItemRepository

class MyApplication : Application() {
    init {
        INSTANCE = this////repository가 myapplication인스턴스를 사용함으로 repository보다 먼저 초기화 되어야 됨
        //REPOSITORY = getRepository()
    }

    companion object{//init보다 먼저 호출됨
        lateinit var INSTANCE: MyApplication
        lateinit var REPOSITORY: ItemRepository

        private fun getRepository(): ItemRepository {
            return ItemRepository.getInstance()
        }
    }

    override fun onCreate() {
        super.onCreate()
        REPOSITORY = getRepository()
    }
}