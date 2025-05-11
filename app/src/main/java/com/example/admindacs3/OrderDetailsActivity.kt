package com.example.admindacs3

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.admindacs3.adapter.OrderDetailsAdapter
import com.example.admindacs3.databinding.ActivityOrderDetailsBinding
import com.example.admindacs3.model.OrderDetails

class OrderDetailsActivity : AppCompatActivity() {
    private val binding : ActivityOrderDetailsBinding by lazy {
        ActivityOrderDetailsBinding.inflate(layoutInflater)
    }

    private var userName: String? = null
    private var address: String? = null
    private var phoneNumber: String? = null
    private var totalPrice: String? = null
    private var foodNames: ArrayList<String> = arrayListOf()
    private var foodImages: ArrayList<String> = arrayListOf()
    private var foodQuantity: ArrayList<Int> = arrayListOf()
    private var foodPrices: ArrayList<String> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.backeButton.setOnClickListener {
            finish()

        }
        getDataFromIntent()

    }

    private fun getDataFromIntent() {
        val receivedOrderDetails = intent.getSerializableExtra("UserOrderDetails") as OrderDetails?
        receivedOrderDetails?.let { orderDetails ->

            userName = receivedOrderDetails.userName
            foodNames = receivedOrderDetails.foodNames as ArrayList<String>
            foodImages = receivedOrderDetails.foodImages as ArrayList<String>
            foodQuantity = receivedOrderDetails.foodQuantities as ArrayList<Int>
            foodPrices = receivedOrderDetails.foodPrices as ArrayList<String>
            address = receivedOrderDetails.address
            phoneNumber = receivedOrderDetails.phoneNumber
            totalPrice = receivedOrderDetails.totalPrice

            setUserDetail()
            setAdapter()



        }

    }


    private fun setUserDetail() {
        binding.name.text = userName
        binding.address.text = address
        binding.phone.text = phoneNumber
        binding.totalPay.text = totalPrice

    }
    private fun setAdapter() {
        binding.orderDetailRecyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = OrderDetailsAdapter(this, foodNames, foodImages, foodQuantity, foodPrices)
        binding.orderDetailRecyclerView.adapter = adapter

    }
}