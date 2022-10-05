package com.project.sharingrestaurants.data

import java.io.Serializable


data class OffItem(//room은 list타입을 지원하지 않아 따로 만든 클래스

    var id: Long,

    var title: String,

    var locate: String,

    var place: String,

    var priority: Float,

    var body: List<String>,

    var imageURL: List<String>,

): Serializable
