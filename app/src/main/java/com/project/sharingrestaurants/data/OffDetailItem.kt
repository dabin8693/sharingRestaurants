package com.project.sharingrestaurants.data

import java.io.Serializable


data class OffDetailItem(

    var id: Long,

    var title: String,

    var locate: String,

    var place: String,

    var priority: Float,

    var body: List<String>,

    var imageURL: List<String>,

): Serializable
