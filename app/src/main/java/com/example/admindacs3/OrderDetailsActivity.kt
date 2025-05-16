package com.example.admindacs3

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.admindacs3.adapter.OrderDetailsAdapter
import com.example.admindacs3.databinding.ActivityOrderDetailsBinding
import com.example.admindacs3.model.OrderDetails
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.times

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

    private var currentTime: Long = 0L  // thêm biến lưu thời gian đặt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.backeButton.setOnClickListener {
            finish()

        }
        getDataFromIntent()

        // Xử lý nút Xuất hóa đơn
        binding.btnExportInvoice.setOnClickListener {
            showInvoicePopup()
        }

    }

    private fun getDataFromIntent() {
        val receivedOrderDetails = intent.getParcelableExtra<OrderDetails>("UserOrderDetails")
        receivedOrderDetails?.let { orderDetails ->

            userName = receivedOrderDetails.userName
            foodNames = receivedOrderDetails.foodNames as ArrayList<String>
            foodImages = receivedOrderDetails.foodImages as ArrayList<String>
            foodQuantity = receivedOrderDetails.foodQuantities as ArrayList<Int>
            foodPrices = receivedOrderDetails.foodPrices as ArrayList<String>
            address = receivedOrderDetails.address
            phoneNumber = receivedOrderDetails.phoneNumber
            totalPrice = receivedOrderDetails.totalPrice
            currentTime = orderDetails.currentTime  // lấy thời gian đặt
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

    private fun showInvoicePopup() {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_invoice, null)

        val tvCustomerName = dialogView.findViewById<TextView>(R.id.tvCustomerName)
        val tvCustomerAddress = dialogView.findViewById<TextView>(R.id.tvCustomerAddress)

        val tvOrderTime = dialogView.findViewById<TextView>(R.id.tvOrderTime)
        val layoutItemsContainer = dialogView.findViewById<LinearLayout>(R.id.layoutItemsContainer)
        val tvTotalAmount = dialogView.findViewById<TextView>(R.id.tvTotalAmount)

        // Định dạng thời gian
        val date = Date(currentTime)
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val formattedTime = sdf.format(date)

        tvCustomerName.text = "Tên khách hàng: $userName"
        tvCustomerAddress.text = "Địa chỉ: $address"
        tvOrderTime.text = "Thời gian đặt: $formattedTime"

        layoutItemsContainer.removeAllViews()

        for (i in foodNames.indices) {
            val itemRow = LinearLayout(this)
            itemRow.orientation = LinearLayout.HORIZONTAL
            itemRow.setPadding(0, 8, 0, 8)

            val paramsName = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f)
            val paramsOther = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)

            val tvName = TextView(this).apply {
                layoutParams = paramsName
                text = foodNames[i]
                gravity = Gravity.START
            }

            val tvQty = TextView(this).apply {
                layoutParams = paramsOther
                text = foodQuantity[i].toString()
                gravity = Gravity.CENTER
            }

            val tvPrice = TextView(this).apply {
                layoutParams = paramsOther
                text = "${foodPrices[i]}$" // Hiển thị $
                gravity = Gravity.CENTER
            }

            // Loại bỏ ký tự không phải số trước khi parseInt
            val quantity = foodQuantity[i].toString().filter { it.isDigit() }.toInt()
            val price = foodPrices[i].toString().filter { it.isDigit() }.toInt()
            val thanhTien = quantity * price

            val tvTotal = TextView(this).apply {
                layoutParams = paramsOther
                text = "${thanhTien}$"
                gravity = Gravity.CENTER
            }

            itemRow.addView(tvName)
            itemRow.addView(tvQty)
            itemRow.addView(tvPrice)
            itemRow.addView(tvTotal)

            layoutItemsContainer.addView(itemRow)
        }

        tvTotalAmount.text = "Tổng tiền: ${totalPrice}$"

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Xuất") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }



}