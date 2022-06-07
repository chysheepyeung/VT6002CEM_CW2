package com.example.traditionalarchitecturemaintain

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
    var userId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var textViewNoOrder = findViewById<TextView>(R.id.noOrder)

        val db = Firebase.firestore
        db.collection(Statics.FIREBASE_USER)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                if(!documents.isEmpty){
                    var user = documents.documents[0].toObject<User>()

                    textViewNoOrder.text = user!!.firstName + user.lastName
                }
            }
            .addOnFailureListener { exception ->
                Log.w("order", "Error getting documents: ", exception)
            }
        Toast.makeText(this, textViewNoOrder.text, Toast.LENGTH_LONG)
    }

    fun goToCreateOrder(view: View){
        val intent= Intent(this,CreateOrderActivity::class.java)

        startActivity(intent)
    }
}