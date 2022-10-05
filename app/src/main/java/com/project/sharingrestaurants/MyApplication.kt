package com.project.sharingrestaurants

import android.app.Application
import com.bumptech.glide.Glide
import com.project.sharingrestaurants.room.ItemRepository


//@HiltAndroidApp
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

    override fun onCreate() {//여기서 의존성 주입됨
        super.onCreate()
        REPOSITORY = getRepository()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Glide.get(this).clearMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Glide.get(this).trimMemory(level)
    }
}