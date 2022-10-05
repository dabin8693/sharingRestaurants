package com.project.sharingrestaurants.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Database

@Database(entities = [ItemEntity::class], version = 7, exportSchema = false)
abstract class ItemDatabase : RoomDatabase() {
    abstract fun dao(): ItemDao

    companion object{//추상클래스는 객체를 생성할 수 없어 인스턴스를 클래스 변수로 사용해야 된다.
        private var INSTANCE: ItemDatabase? = null

        fun getInstance(context: Context): ItemDatabase {//스레드 경합없음으로 synchronized필요없음
            if (INSTANCE == null) {//중복 생성 방지
                INSTANCE =
                    Room.databaseBuilder(context, ItemDatabase::class.java, "itemDB")
                        .fallbackToDestructiveMigration().build()
            }
            return INSTANCE ?: Room.databaseBuilder(context, ItemDatabase::class.java, "itemDB")
                .fallbackToDestructiveMigration().build() //null이면 재생성
        }
    }
}