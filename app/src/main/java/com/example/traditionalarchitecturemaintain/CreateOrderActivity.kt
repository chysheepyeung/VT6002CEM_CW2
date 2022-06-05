package com.example.traditionalarchitecturemaintain

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast

class CreateOrderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_order)
    }

    fun submitOrder(view: View){
        Toast.makeText(this, "Order added to the list successfully", Toast.LENGTH_SHORT).show()
        var intent = Intent(this, MapsActivity::class.java)

        startActivity(intent)
    }
}