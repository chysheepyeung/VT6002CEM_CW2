package com.example.traditionalarchitecturemaintain

class Bidding {
    companion object Factory {
        fun create(): Bidding = Bidding()
    }
    var biddingId: String? = null
    var orderId: String? = null
    var companyId: String? = null
    var budget: Int = 0
}