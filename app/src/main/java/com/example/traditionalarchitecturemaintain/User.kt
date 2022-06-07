package com.example.traditionalarchitecturemaintain

class User {
    companion object Factory {
        fun create(): User = User()
    }
    var email: String? = null
    var userId: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var isAdmin: Boolean = false
}