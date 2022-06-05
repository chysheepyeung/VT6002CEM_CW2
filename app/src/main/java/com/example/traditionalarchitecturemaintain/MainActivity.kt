package com.example.traditionalarchitecturemaintain

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun goToCreateOrder(){
        val intent= Intent(this,CreateOrderActivity::class.java)

        startActivity(intent)
    }
}