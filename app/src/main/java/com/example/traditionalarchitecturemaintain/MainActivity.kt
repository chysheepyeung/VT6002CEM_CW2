package com.example.traditionalarchitecturemaintain

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    var isAdmin: String? = null
    var _orderList: MutableList<Order>? = ArrayList()

    var _orderListener: ValueEventListener = object: ValueEventListener{
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            loadOrderList(dataSnapshot)
        }
        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Item failed, log a message
            Log.w("MainActivity", "loadItem:onCancelled", databaseError.toException())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val _db = FirebaseDatabase.getInstance("https://vtc-mobileapp-cw2-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("order")



        _db.orderByKey().addValueEventListener(_orderListener)
    }

    private fun loadOrderList(dataSnapshot: DataSnapshot){
        Log.d("loadTaskList", "loadTaskList")

        for(orderObj in dataSnapshot.children){
            var order = orderObj.getValue(Order::class.java)
            Log.d("order class", order.toString())

            _orderList!!.add(order!!)
        }

    }

    fun goToCreateOrder(view: View){
        val intent= Intent(this,CreateOrderActivity::class.java)

        startActivity(intent)
    }
}