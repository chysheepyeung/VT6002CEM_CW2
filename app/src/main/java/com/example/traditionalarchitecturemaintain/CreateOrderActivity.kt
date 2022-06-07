package com.example.traditionalarchitecturemaintain

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class CreateOrderActivity : AppCompatActivity() {
//    private val myLocation = 10001;
    val CAMERA_PERM_CODE: Int = 101
    val WRITE_PERM_CODE:Int = 103
    val CAMERA_REQUEST_CODE = 102
    val GALLERY_REQUEST_CODE = 105
    var selectedImage: ImageView? = null
    var currentPhotoPath: String? = null
    var storageReference: StorageReference? = null
    //val _db = Firebase.firestore
    lateinit var _db: DatabaseReference
    var photoURI :Uri = Uri.EMPTY

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

        selectedImage = findViewById(R.id.displayImageView)
        val cameraBtn = findViewById<Button>(R.id.cameraBtn)
        val galleryBtn = findViewById<Button>(R.id.galleryBtn)
        storageReference = FirebaseStorage.getInstance().getReference()

        _db = FirebaseDatabase.getInstance().getReference("order")

        cameraBtn.setOnClickListener{view -> takePhoto() }

        galleryBtn.setOnClickListener{
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(gallery, GALLERY_REQUEST_CODE)
        }
    }

    private fun takePhoto() {
        if(checkPermission())
            dispatchTakePictureIntent()
    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA
                ),
                CAMERA_PERM_CODE
            )
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                WRITE_PERM_CODE
            )
        }

        return ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(
                    this,
                    "Camera Permission is Required to Use camera.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
                }
            }
        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun getFileExt(contentUri: Uri): String? {
        val c = contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(c.getType(contentUri))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val f = File(currentPhotoPath)
                selectedImage!!.setImageURI(Uri.fromFile(f))
                Log.d("tag", "Absolute Url of Image is " + Uri.fromFile(f))
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                val contentUri = Uri.fromFile(f)
                mediaScanIntent.data = contentUri
                this.sendBroadcast(mediaScanIntent)
                uploadImageToFirebase(f.name, contentUri)
            }
        }
        if (requestCode == GALLERY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val contentUri = data!!.data
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                val imageFileName = "JPEG_" + timeStamp + "." + getFileExt(contentUri!!)
                Log.d("tag", "onActivityResult: Gallery Image Uri:  $imageFileName")
                selectedImage!!.setImageURI(contentUri)
                uploadImageToFirebase(imageFileName, contentUri)
            }
        }
    }

    fun submitOrder(view: View){
        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etDesc = findViewById<EditText>(R.id.etDesc)
        val etLocation = findViewById<EditText>(R.id.etLocation)

        val title = etTitle.text.toString()
        val desc = etDesc.text.toString()
        val location = etLocation.text.toString()


        val order = Order.create()
        order.userId = Statics.userId
        order.title = title
        order.description = desc
        order.location = location
        order.pic = photoURI
        order.status = 0

//        _db.collection(Statics.FIREBASE_USER)
//            .whereEqualTo("userId", Statics.userId)
//            .get()
//            .addOnSuccessListener { documents ->
//                if(!documents.isEmpty){
//                    var user = documents.documents[0].toObject<User>()
//
//                    order.userName = user!!.firstName + user!!.lastName
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.w("order", "Error getting documents: ", exception)
//            }

        createOrderToFB(order)
        Toast.makeText(this, "Order create successfully", Toast.LENGTH_LONG).show()
        finish()
    }

    fun findMyLocation(view: View){
        val intent = Intent(this, MapsActivity::class.java)
        getResult.launch(intent)

    }

    fun createOrderToFB(order:Order){
//        _db.collection(Statics.FIREBASE_ORDER).add(order).addOnSuccessListener { documentReference ->
//            Log.d("tag", "DocumentSnapshot added with ID: ${documentReference.id}")
//        }.addOnFailureListener { e ->
//            Log.w("tag", "Error adding document", e)
//        }

        order.objectId = "1"
        try{
            _db.setValue(order)
        }catch(e: Exception){
            Log.d("Order Fail", "${e.toString()}")
        }

    }

    private fun uploadImageToFirebase(name: String, contentUri: Uri) {
        val image = storageReference!!.child("pictures/$name")
        image.putFile(contentUri).addOnSuccessListener {
            image.downloadUrl.addOnSuccessListener { uri ->
                Log.d(
                    "tag",
                    "onSuccess: Uploaded Image URl is $uri"
                )
                photoURI = uri
            }
            Toast.makeText(this, "Image Is Uploaded.", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(
                this,
                "Upload Failled.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}