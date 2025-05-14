package com.example.admindacs3

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.admindacs3.databinding.ActivityAddItemBinding
import com.example.admindacs3.model.AllMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddItemActivity : AppCompatActivity() {

    // Food item details
    private lateinit var foodName: String
    private lateinit var foodPrice: String
    private lateinit var foodDescription: String
    private lateinit var foodIngredient: String
    private var foodImageUri: Uri? = null

    // Firebase
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    private val binding: ActivityAddItemBinding by lazy {
        ActivityAddItemBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Init Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Init Cloudinary
        val config: MutableMap<String, String> = HashMap()
        config["cloud_name"] = "dogljqijs"
        config["api_key"] = "257597465547465"
        config["api_secret"] = "fW87431vn3ockgOd81r8HTscF-w"
        MediaManager.init(this, config)

        binding.AddItemButton.setOnClickListener {
            // Get input
            foodName = binding.foodName.text.toString().trim()
            foodPrice = binding.foodPrice.text.toString().trim()
            foodDescription = binding.description.text.toString().trim()
            foodIngredient = binding.ingredint.text.toString().trim()

            if (foodName.isNotBlank() && foodPrice.isNotBlank() &&
                foodDescription.isNotBlank() && foodIngredient.isNotBlank()) {
                uploadData()
            } else {
                Toast.makeText(this, "Fill all the details", Toast.LENGTH_SHORT).show()
            }
        }

        binding.selectImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun uploadData() {
        val menuref = database.getReference("menu")
        val newItemKey = menuref.push().key

        if (foodImageUri != null && newItemKey != null) {
            MediaManager.get().upload(foodImageUri)
                .option("resource_type", "image")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {
                        Toast.makeText(this@AddItemActivity, "Uploading image...", Toast.LENGTH_SHORT).show()
                    }

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                        val imageUrl = resultData?.get("secure_url").toString()

                        val newItem = AllMenu(
                            newItemKey,
                            foodName = foodName,
                            foodPrice = foodPrice,
                            foodDescription = foodDescription,
                            foodImage = imageUrl,
                            foodIngredient = foodIngredient

                        )

                        menuref.child(newItemKey).setValue(newItem)
                            .addOnSuccessListener {
                                Toast.makeText(this@AddItemActivity, "Item Added Successfully", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@AddItemActivity, "Failed to upload data", Toast.LENGTH_SHORT).show()
                            }
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Toast.makeText(this@AddItemActivity, "Upload failed: ${error?.description}", Toast.LENGTH_LONG).show()
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                        Toast.makeText(this@AddItemActivity, "Upload rescheduled: ${error?.description}", Toast.LENGTH_LONG).show()
                    }
                })
                .dispatch()
        } else {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            binding.selectedImage.setImageURI(uri)
            foodImageUri = uri
        }
    }
}
