package com.example.traditionalarchitecturemaintain

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BiddingActivity : AppCompatActivity() {
    var orderId:String = ""
    var context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bidding)
        Log.d("bidding", "onCreate")

        orderId = intent.getStringExtra("orderId").toString()
        val title:String = intent.getStringExtra("title").toString()
        val location:String = intent.getStringExtra("location").toString()
        val desc:String = intent.getStringExtra("desc").toString()
        val pic:String = intent.getStringExtra("pic").toString()
        val status:Int = intent.getIntExtra("status", 0)
        Log.d("bidding", "get Extra")

        val tvTitle = findViewById<TextView>(R.id.tv_bidding_title)
        val tvLocation = findViewById<TextView>(R.id.tv_bidding_location)
        val tvDesc = findViewById<TextView>(R.id.tv_bidding_desc)
        val ivPic = findViewById<ImageView>(R.id.iv_bidding_pic)
        val etBudget = findViewById<EditText>(R.id.et_bidding_budget)
        val btnApply = findViewById<Button>(R.id.btn_bidding_apply)
        val btnSend = findViewById<Button>(R.id.btn_bidding_send)
        val btnDirect = findViewById<Button>(R.id.btn_bidding_direct)

        Log.d("bidding", "getView")

        tvTitle.text = title
        tvLocation.text = location
        tvDesc.text = desc

        Log.d("bidding", "set Text")
        Picasso.get().load(pic).into(ivPic)
        Log.d("bidding", "set Pic")

        if(status >= 2){
            btnApply.visibility = View.GONE
            btnDirect.visibility = View.VISIBLE
        }

        Log.d("bidding", "set btnApply Onclick")
        btnApply.setOnClickListener{
            etBudget.visibility = View.VISIBLE
            btnSend.visibility = View.VISIBLE
            btnApply.visibility = View.GONE
        }

        Log.d("bidding", "set btnSend Onclick")
        btnSend.setOnClickListener{
            SendBidding()
        }

        Log.d("bidding", "set btnDirect Onclick")
        btnDirect.setOnClickListener{
            val intent = Intent(this, DirectionActivity::class.java)

            intent.putExtra("location", location)
            startActivity(intent)
        }
    }

    private fun SendBidding(){
        val etBudget = findViewById<EditText>(R.id.et_bidding_budget)
        val budget = etBudget.text.toString()

        val _db = FirebaseDatabase.getInstance("https://vtc-mobileapp-cw2-default-rtdb.asia-southeast1.firebasedatabase.app/")
        var newBid = _db.getReference(Statics.FIREBASE_BIDDING).push();
        var bid = Bidding.create()

        bid.biddingId = newBid.key
        bid.budget = budget.toInt()
        bid.companyId = Statics.userId
        bid.orderId = orderId
        bid.companyName = Statics.userName
        newBid.setValue(bid)

        GlobalScope.launch(Dispatchers.IO){
            var query = _db.getReference(Statics.FIREBASE_ORDER).child(orderId)
            var orderDoc = query.get().await()

            withContext(Dispatchers.Main) {
                var order = orderDoc.getValue(Order::class.java)
                order!!.status = 1
                query.setValue(order)
                Toast.makeText(context, "Apply Order Success", Toast.LENGTH_SHORT)
                finish()
            }
        }
    }
}