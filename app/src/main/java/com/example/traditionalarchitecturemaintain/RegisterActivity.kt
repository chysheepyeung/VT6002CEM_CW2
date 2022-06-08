package com.example.traditionalarchitecturemaintain

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var  auth: FirebaseAuth
    val _db = Firebase.firestore




    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)




        auth= FirebaseAuth.getInstance()

        //_db = FirebaseDatabase.getInstance("https://vtc-mobileapp-cw2-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

    }

    fun register(view: View){
        val editTextEmailAddress = findViewById<EditText>(R.id.editTextEmailAddress)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val editTextFirstName = findViewById<EditText>(R.id.editTextFirstName)
        val editTextLastName = findViewById<EditText>(R.id.editTextLastName)
        val editTextRegisterCode = findViewById<EditText>(R.id.editTextRegisterCode)

        val email = editTextEmailAddress.text.toString()
        val password = editTextPassword.text.toString()
        val firstName = editTextFirstName.text.toString()
        val lastName = editTextLastName.text.toString()
        val registerCode = editTextRegisterCode.text.toString()




        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->

            if(task.isSuccessful) {
                val user = auth.currentUser
                Toast.makeText(this, "${user!!.uid}", Toast.LENGTH_LONG).show()

                val userObj = User.create()
                userObj.userId = user.uid
                userObj.email = user.email
                userObj.firstName = firstName
                userObj.lastName = lastName
                userObj.isAdmin = registerCode == "IAMADMIN"

                _db.collection(Statics.FIREBASE_USER).add(userObj).addOnSuccessListener { documentReference ->
                    Log.d("tag", "DocumentSnapshot added with ID: ${documentReference.id}")
                }.addOnFailureListener { e ->
                    Log.w("tag", "Error adding document", e)
                }


                val intent = Intent(this, MainActivity::class.java)
                Statics.userId = user.uid
                Statics.isAdmin = userObj.isAdmin
                Statics.userName = "$firstName $lastName"

                startActivity(intent)

                finish()

            }

        }.addOnFailureListener { exception ->

            Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()

        }

    }

    fun goToLogin(view: View){

        val intent= Intent(this, LoginActivity::class.java)

        startActivity(intent)

    }



}
