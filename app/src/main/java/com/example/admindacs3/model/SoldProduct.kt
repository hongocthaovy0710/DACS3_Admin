package com.example.admindacs3.model

data class SoldProduct(
    val name: String = "",
    var quantity: Int = 0,
    val price: Double = 0.0,
    var totalRevenue: Double = 0.0,
    val imageUrl: String = "",
    val timestamp: Long = 0 // Thêm trường timestamp để lưu thời gian

)