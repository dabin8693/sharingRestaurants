package com.project.sharingrestaurants.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ItemDao {
    @Query("SELECT * FROM item ORDER BY id DESC")//item테이블 기본키 내림차순 기준으로 전부 가져오기
    fun getList(): LiveData<List<ItemEntity>>

    @Query("SELECT * FROM item WHERE (item.title LIKE :query OR item.place LIKE :query)")
    fun searchByTitleOrPlace(query: String?): LiveData<List<ItemEntity>>//제목 또는 본문에 해당 문자열이 있으면 검색

    @Query("SELECT * FROM item WHERE item.title LIKE :query")
    fun searchByTitle(query: String?): LiveData<List<ItemEntity>>//제목에 문자열이 있으면 검색

    @Insert(onConflict = OnConflictStrategy.REPLACE)//충돌이 발생할 경우 덮어쓰기
    fun insert(itemEntity: ItemEntity)//수정

    @Delete
    fun delete(itemEntity: ItemEntity)//삭제
}