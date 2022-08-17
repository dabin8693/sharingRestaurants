package com.project.sharingrestaurants.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "item")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long?,//null들어가면 자동 생성

    @ColumnInfo(name = "title")
    var title: String,

    @ColumnInfo(name = "place")
    var place: String,

    @ColumnInfo(name = "priority")
    var priority: Int,

    @ColumnInfo(name = "nickname")
    var nickname: String,

    @ColumnInfo(name = "body")
    var body: String,

    @ColumnInfo(name = "imageURL")
    var imageURL: String,

    @ColumnInfo(name = "mapURL")
    var mapURL: String,

): Serializable //intent를 위한 직렬화