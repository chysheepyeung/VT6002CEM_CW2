package com.example.traditionalarchitecturemaintain

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CompanyActivity : AppCompatActivity(), CompanyRowListener {
    var _companyList: MutableList<Bidding>? = null
    lateinit var _adapter: CompanyAdapter
    val _db = FirebaseDatabase.getInstance("https://vtc-mobileapp-cw2-default-rtdb.asia-southeast1.firebasedatabase.app/")
    lateinit var orderId:String

    var _companyListener: ValueEventListener = object: ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            loadCompanyList(dataSnapshot)
        }
        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Item failed, log a message
            Log.w("loadTaskList", "loadItem:onCancelled", databaseError.toException())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_company)

        orderId = intent.getStringExtra("orderId").toString()

        var biddingCollection = _db.getReference(Statics.FIREBASE_BIDDING)
        _companyList = mutableListOf()
        _adapter = CompanyAdapter(this, _companyList!!)
        var listViewCompany = findViewById<ListView>(R.id.listviewCompany)
        listViewCompany!!.adapter = _adapter

        biddingCollection.orderByKey().addValueEventListener(_companyListener)
    }

    private fun loadCompanyList(dataSnapshot: DataSnapshot){
        Log.d("loadCompanyList", "loadCompanyList")
        _companyList!!.clear()

        for(companyObj in dataSnapshot.children){
            var bid = companyObj.getValue(Bidding::class.java)
            Log.d("loadTaskList", bid.toString())

            if(bid!!.orderId == orderId){
                _companyList!!.add(bid)
            }
        }
        val tvnoCompany = findViewById<TextView>(R.id.tv_noCompany)
        if(_companyList!!.isEmpty()){
            tvnoCompany.visibility = View.VISIBLE
        }else{
            tvnoCompany.visibility = View.GONE
        }
        _companyList!!.reverse()
        _adapter.notifyDataSetChanged()
    }

    override fun onCompanyAccept(orderId: String, biddingId: String, companyId: String) {
        GlobalScope.launch(Dispatchers.IO) {

            val targetOrder = _db.getReference(Statics.FIREBASE_ORDER).child(orderId)
            val targetBidding = _db.getReference(Statics.FIREBASE_BIDDING).child(biddingId)
            val orderCollection = targetOrder.get().await()
            val biddingCollection = targetBidding.get().await()

            withContext(Dispatchers.Main){
                var order = orderCollection.getValue(Order::class.java)
                var bidding = biddingCollection.getValue(Bidding::class.java)

                order!!.status = 2
                order!!.companyId = companyId
                bidding!!.isAccept = true

                targetOrder.setValue(order)
                targetBidding.setValue(bidding)

                finish()
            }
        }
    }
}