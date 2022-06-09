package com.example.traditionalarchitecturemaintain

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView
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
import java.util.*

class MainActivity : AppCompatActivity() {
    var _orderList: MutableList<Order>? = null
    lateinit var _adapter: OrderAdapter




    var _orderListener: ValueEventListener = object: ValueEventListener{
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            loadOrderList(dataSnapshot)
        }
        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Item failed, log a message
            Log.w("loadTaskList", "loadItem:onCancelled", databaseError.toException())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val orderButton = findViewById<Button>(R.id.addOrder)
        if(Statics.isAdmin){
            orderButton.visibility = View.GONE
        }

        val _db = FirebaseDatabase.getInstance("https://vtc-mobileapp-cw2-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("order")

        _orderList = mutableListOf()
        _adapter = OrderAdapter(this, _orderList!!)


        var listViewOrder = findViewById<ListView>(R.id.listviewOrder)
        listViewOrder.setOnItemClickListener { parent, view, position, id ->
            Log.d("bidding", _orderList!![position].orderId.toString())

            if(Statics.isAdmin){
                var intent = Intent(this, BiddingActivity::class.java)

                Log.d("bidding", "set intent")
                intent.putExtra("orderId", _orderList!![position].orderId.toString())
                intent.putExtra("title", _orderList!![position].title.toString())
                intent.putExtra("location", _orderList!![position].location.toString())
                intent.putExtra("desc", _orderList!![position].description.toString())
                intent.putExtra("pic", _orderList!![position].pic.toString())

                Log.d("bidding", "set extra")
                startActivity(intent)
                Log.d("bidding", "start bidding")
            }else{
                var intent = Intent(this, CompanyActivity::class.java)
                intent.putExtra("orderId", _orderList!![position].orderId.toString())
                startActivity(intent)
            }
        }
        listViewOrder!!.adapter = _adapter

        _db.orderByKey().addValueEventListener(_orderListener)
    }

    private fun loadOrderList(dataSnapshot: DataSnapshot){
        Log.d("loadTaskList", "loadTaskList")
        _orderList!!.clear()

        for(orderObj in dataSnapshot.children){
            var order = orderObj.getValue(Order::class.java)
            Log.d("loadTaskList", order.toString())

            if(Statics.isAdmin){
                if(order!!.status!! < 2 || order.companyId == Statics.userId){
                    _orderList!!.add(order!!)
                }
            }else{
                if(order!!.userId == Statics.userId){
                    _orderList!!.add(order!!)
                }
            }

        }
        val tvnoOrder = findViewById<TextView>(R.id.tv_noOrder)
        if(_orderList!!.isEmpty()){
            if(Statics.isAdmin){
                tvnoOrder.text = "There is no order available now"
            }else{
                tvnoOrder.text = "You have not create order yet"
            }
            tvnoOrder.visibility = View.VISIBLE
        }else{
            tvnoOrder.visibility = View.GONE
        }
        _orderList!!.reverse()
        _adapter.notifyDataSetChanged()
    }



    fun goToCreateOrder(view: View){
        val intent= Intent(this,CreateOrderActivity::class.java)

        startActivity(intent)
    }
}