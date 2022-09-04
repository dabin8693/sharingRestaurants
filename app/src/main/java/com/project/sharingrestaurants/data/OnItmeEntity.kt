package com.project.sharingrestaurants.data

import java.io.Serializable

data class OnItmeEntity(
    var title: String,
    var locate: String,
    var place: String,
    var priority: Float,
    var number: Int,//추천수
    var nickname: String,
    var id: String,
    var latitude: Double,
    var longitude: Double
):Serializable
