package com.example.traditionalarchitecturemaintain


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var  auth: FirebaseAuth




    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)




        auth= FirebaseAuth.getInstance()




    }
    fun login(view: View){
        val editTextEmailAddress = findViewById<EditText>(R.id.editTextEmailAddress)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val email=editTextEmailAddress.text.toString()
        val password=editTextPassword.text.toString()


        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->

            if(task.isSuccessful){
                val user = auth.currentUser
                Statics.userId = user!!.uid

                var isAdmin: Boolean = false

                val db = Firebase.firestore
                db.collection(Statics.FIREBASE_USER)
                    .whereEqualTo("userId", user!!.uid)
                    .get()
                    .addOnSuccessListener { documents ->
                        if(!documents.isEmpty){
                            var user = documents.documents[0].toObject<User>()
                            isAdmin = user!!.isAdmin
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("order", "Error getting documents: ", exception)
                    }

                val intent= Intent(this, MainActivity::class.java)
                if(isAdmin){
                    intent.putExtra("isAdmin", true)
                }else{
                    intent.putExtra("isAdmin", false)
                }
                startActivity(intent)

                finish()

            }

        }.addOnFailureListener { exception ->

            Toast.makeText(applicationContext,exception.localizedMessage, Toast.LENGTH_LONG).show()

        }

    }

    fun goToRegister(view: View){
        val intent= Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

}
