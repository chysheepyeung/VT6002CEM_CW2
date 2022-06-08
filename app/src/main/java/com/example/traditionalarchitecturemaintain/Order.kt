package com.example.traditionalarchitecturemaintain


class Order {
    companion object Factory {
        fun create(): Order = Order()
    }
    var orderId: String? = null
    var userId: String? = null
    var title: String? = null
    var description: String? = null
    var location: String? = null
    var pic: String? = null
    var status: Int? = 0  //0 - new, 1 - bidding, 2 - confirm, 3 - complete

}