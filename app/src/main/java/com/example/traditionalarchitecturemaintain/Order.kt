package com.example.traditionalarchitecturemaintain

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class Order {
    companion object Factory {
        fun create(): Order = Order()
    }
    var objectId: String? = null
    var userId: String? = null
    var title: String? = null
    var description: String? = null
    var location: String? = null
    var pic: Uri? = null
    var status: Int? = 0  //0 - new, 1 - bidding, 2 - confirm, 3 - complete


//    fun getUserName(userId: String){
//        val db = Firebase.firestore
//        db.collection(Statics.FIREBASE_USER)
//            .whereEqualTo("userId", userId)
//            .get()
//            .addOnSuccessListener { documents ->
//                if(!documents.isEmpty){
//                    var user = documents.documents[0].toObject<User>()
//
//                    userName = user!!.firstName + user!!.lastName
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.w("order", "Error getting documents: ", exception)
//            }
//    }
}