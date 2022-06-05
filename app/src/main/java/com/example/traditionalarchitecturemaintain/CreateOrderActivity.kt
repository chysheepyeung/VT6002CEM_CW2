package com.example.traditionalarchitecturemaintain

import android.R.attr
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity


class CreateOrderActivity : AppCompatActivity() {
//    private val myLocation = 10001;

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val location = it.data?.getStringExtra("location")
                val etLocation = findViewById<EditText>(R.id.etLocation)
                etLocation.setText(location)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_order)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode === myLocation) {
//            if (resultCode === RESULT_OK) {
//                val location: String = data?.getStringExtra("location")
//            }
//        }
//    }

    fun submitOrder(view: View){
        Toast.makeText(this, "Order added to the list successfully", Toast.LENGTH_SHORT).show()

    }

    fun findMyLocation(view: View){
//        var intent = Intent(this, MapsActivity::class.java)
//
//        startActivityForResult(intent, myLocation);

        val intent = Intent(this, MapsActivity::class.java)
        getResult.launch(intent)



    }
}