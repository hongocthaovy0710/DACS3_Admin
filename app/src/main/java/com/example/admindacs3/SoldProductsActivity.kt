package com.example.admindacs3

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.admindacs3.adapter.SoldProductAdapter
import com.example.admindacs3.databinding.ActivitySoldProductsBinding
import com.example.admindacs3.model.SoldProduct
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class SoldProductsActivity : AppCompatActivity() {
    private val binding: ActivitySoldProductsBinding by lazy {
        ActivitySoldProductsBinding.inflate(layoutInflater)
    }

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var soldProductsReference: DatabaseReference
    private lateinit var menuReference: DatabaseReference
    private lateinit var soldProductsList: ArrayList<SoldProduct>
    private lateinit var adapter: SoldProductAdapter
    private val TAG = "SoldProductsActivity"

    // Map để lưu trữ doanh thu theo ngày và tháng
    private var dailyRevenueMap = mutableMapOf<String, Double>()
    private var monthlyRevenueMap = mutableMapOf<String, Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Khởi tạo Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        soldProductsReference = database.reference.child("CompletedOrder")
        menuReference = database.reference.child("menu")

        // Thiết lập RecyclerView
        soldProductsList = ArrayList()
        binding.soldProductsRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SoldProductAdapter(this, soldProductsList)
        binding.soldProductsRecyclerView.adapter = adapter

        // Thiết lập biểu đồ
        setupChart()

        // Xử lý RadioGroup
        binding.chartTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioDaily.id -> updateChartData(showDaily = true)
                binding.radioMonthly.id -> updateChartData(showDaily = false)
            }
        }

        // Lấy dữ liệu từ Firebase
        retrieveSoldProducts()

        // Nút quay lại
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupChart() {
        // Thiết lập các thuộc tính cơ bản của biểu đồ
        binding.barChart.description.isEnabled = false
        binding.barChart.setDrawGridBackground(false)
        binding.barChart.legend.isEnabled = true
        binding.barChart.setPinchZoom(false)
        binding.barChart.setScaleEnabled(false)

        // Cấu hình trục X
        val xAxis = binding.barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)

        // Cấu hình trục Y bên trái
        val leftAxis = binding.barChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.axisMinimum = 0f  // Bắt đầu từ 0

        // Cấu hình trục Y bên phải (ẩn)
        binding.barChart.axisRight.isEnabled = false

        // Thêm khoảng trống xung quanh dữ liệu
        binding.barChart.setExtraOffsets(10f, 10f, 10f, 20f)

        // Không có dữ liệu ban đầu
        binding.barChart.setNoDataText("Không có dữ liệu")
    }

    private fun updateChartData(showDaily: Boolean) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        var index = 0f

        if (showDaily) {
            // Sắp xếp các ngày theo thứ tự tăng dần
            val sortedDays = dailyRevenueMap.keys.sortedBy {
                val parts = it.split("/")
                val day = parts[0].toInt()
                val month = parts[1].toInt()
                month * 100 + day
            }

            for (dayStr in sortedDays) {
                val revenue = dailyRevenueMap[dayStr] ?: 0.0
                entries.add(BarEntry(index, revenue.toFloat()))
                labels.add(dayStr)
                index++
            }
        } else {
            // Sắp xếp các tháng theo thứ tự tăng dần
            val sortedMonths = monthlyRevenueMap.keys.sorted()

            for (monthStr in sortedMonths) {
                val revenue = monthlyRevenueMap[monthStr] ?: 0.0
                entries.add(BarEntry(index, revenue.toFloat()))
                labels.add(monthStr)
                index++
            }
        }

        // Không có dữ liệu
        if (entries.isEmpty()) {
            binding.barChart.clear()
            binding.barChart.setNoDataText("Không có dữ liệu")
            binding.barChart.invalidate()
            return
        }

        // Cập nhật nhãn trục X
        binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        // Tạo dataset
        val dataSet = BarDataSet(entries, "Doanh thu")
        dataSet.setColors(resources.getColor(R.color.purple_500, theme))

        // Tạo dữ liệu biểu đồ và cập nhật
        val data = BarData(dataSet)
        data.setValueTextSize(12f)
        binding.barChart.data = data

        // Điều chỉnh số lượng item hiển thị
        if (entries.size > 10) {
            binding.barChart.setVisibleXRangeMaximum(10f)
        }

        // Cập nhật biểu đồ
        binding.barChart.invalidate()
    }

    private fun retrieveSoldProducts() {
        binding.progressBar.visibility = View.VISIBLE

        // Bước 1: Lấy thông tin tất cả sản phẩm trong menu để có giá chính xác
        val menuPriceMap = mutableMapOf<String, Double>()
        val menuImageMap = mutableMapOf<String, String>()

        menuReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(menuSnapshot: DataSnapshot) {
                // Lưu thông tin giá từ menu
                for (itemSnapshot in menuSnapshot.children) {
                    val foodName = itemSnapshot.child("foodName").getValue(String::class.java) ?: ""
                    val foodPrice = itemSnapshot.child("foodPrice").getValue(String::class.java) ?: "0"
                    val foodImage = itemSnapshot.child("foodImage").getValue(String::class.java) ?: ""

                    val price = foodPrice.replace("$", "").toDoubleOrNull() ?: 0.0
                    menuPriceMap[foodName] = price
                    menuImageMap[foodName] = foodImage
                }

                // Bước 2: Sau khi đã có thông tin giá, tiếp tục lấy đơn hàng đã hoàn thành
                fetchCompletedOrders(menuPriceMap, menuImageMap)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load menu data: ${error.message}")
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@SoldProductsActivity, "Không thể tải dữ liệu menu", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchCompletedOrders(menuPriceMap: Map<String, Double>, menuImageMap: Map<String, String>) {
        soldProductsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                soldProductsList.clear()
                dailyRevenueMap.clear()
                monthlyRevenueMap.clear()

                // Tạo map để thống kê số lượng bán cho mỗi sản phẩm
                val productCountMap = mutableMapOf<String, SoldProduct>()

                for (orderSnapshot in snapshot.children) {
                    try {
                        // Thử đọc dữ liệu thủ công thay vì dùng getValue(OrderDetails::class.java)
                        processOrderManually(orderSnapshot, productCountMap, menuPriceMap, menuImageMap)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing order: ${e.message}")
                    }
                }

                // Chuyển từ map sang danh sách để hiển thị
                soldProductsList.addAll(productCountMap.values)

                // Sắp xếp theo số lượng bán (giảm dần)
                soldProductsList.sortByDescending { it.quantity }

                // Cập nhật số liệu tổng quan
                updateSummary()

                // Cập nhật biểu đồ (mặc định theo ngày)
                updateChartData(showDaily = true)

                // Cập nhật adapter
                adapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load order data: ${error.message}")
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@SoldProductsActivity, "Không thể tải dữ liệu đơn hàng", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun processOrderManually(orderSnapshot: DataSnapshot, productCountMap: MutableMap<String, SoldProduct>,
                                     menuPriceMap: Map<String, Double>, menuImageMap: Map<String, String>) {

        // Lấy timestamp từ currentTime
        val currentTime = orderSnapshot.child("currentTime").getValue(Long::class.java)

        // Nếu không có currentTime, thử dùng System.currentTimeMillis()
        val finalTimestamp = currentTime ?: System.currentTimeMillis()

        // Tạo các key cho biểu đồ theo ngày và tháng
        val date = Date(finalTimestamp)
        val dayFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MM/yyyy", Locale.getDefault())

        val dayKey = dayFormat.format(date)
        val monthKey = monthFormat.format(date)

        // Lấy tên sản phẩm
        val foodNamesSnapshot = orderSnapshot.child("foodNames")
        val foodNames = mutableListOf<String>()
        foodNamesSnapshot.children.forEach {
            val name = it.getValue(String::class.java)
            if (name != null) foodNames.add(name)
        }

        // Lấy số lượng
        val foodQuantitiesSnapshot = orderSnapshot.child("foodQuantities")
        val foodQuantities = mutableListOf<Int>()
        foodQuantitiesSnapshot.children.forEach {
            val quantityValue = it.getValue()

            // Xử lý trường hợp giá trị có thể là String hoặc Long trong Firebase
            val quantity = when (quantityValue) {
                is String -> quantityValue.toIntOrNull() ?: 1
                is Long -> quantityValue.toInt()
                is Int -> quantityValue
                else -> 1
            }

            foodQuantities.add(quantity)
        }

        // Lấy giá (nếu có)
        val foodPricesSnapshot = orderSnapshot.child("foodPrices")
        val foodPrices = mutableListOf<Double>()
        foodPricesSnapshot.children.forEach {
            val priceStr = it.getValue(String::class.java)
            val price = priceStr?.replace("$", "")?.toDoubleOrNull() ?: 0.0
            foodPrices.add(price)
        }

        // Kiểm tra dữ liệu có hợp lệ không
        if (foodNames.isEmpty()) {
            Log.w(TAG, "Đơn hàng không có thông tin sản phẩm")
            return
        }

        // Ghép cặp và xử lý
        for (i in foodNames.indices) {
            if (i < foodQuantities.size) {
                val productName = foodNames[i]
                val quantity = foodQuantities[i]

                // Lấy giá từ menu hoặc từ đơn hàng
                val price = menuPriceMap[productName] ?: run {
                    if (i < foodPrices.size) foodPrices[i] else 0.0
                }

                // Lấy hình ảnh từ map
                val image = menuImageMap[productName] ?: ""

                // Cập nhật dữ liệu theo ngày và tháng
                val revenue = price * quantity
                dailyRevenueMap[dayKey] = (dailyRevenueMap[dayKey] ?: 0.0) + revenue
                monthlyRevenueMap[monthKey] = (monthlyRevenueMap[monthKey] ?: 0.0) + revenue

                updateProductCountMap(productCountMap, productName, quantity, price, image, finalTimestamp)
            }
        }
    }

    private fun updateProductCountMap(productCountMap: MutableMap<String, SoldProduct>,
                                      productName: String, quantity: Int, price: Double,
                                      imageUrl: String, timestamp: Long) {
        // Cập nhật vào map thống kê
        if (productCountMap.containsKey(productName)) {
            val existingProduct = productCountMap[productName]!!
            existingProduct.quantity += quantity
            existingProduct.totalRevenue += price * quantity
            // Không cập nhật timestamp vì chúng ta chỉ quan tâm đến tổng kết
        } else {
            val newProduct = SoldProduct(
                name = productName,
                quantity = quantity,
                price = price,
                totalRevenue = price * quantity,
                imageUrl = imageUrl,
                timestamp = timestamp
            )
            productCountMap[productName] = newProduct
        }
    }

    private fun updateSummary() {
        var totalItems = 0
        var totalRevenue = 0.0

        for (product in soldProductsList) {
            totalItems += product.quantity
            totalRevenue += product.totalRevenue
        }

        binding.totalProductsSold.text = totalItems.toString()
        binding.totalRevenue.text = "$${String.format("%.2f", totalRevenue)}"
        binding.totalProductTypes.text = soldProductsList.size.toString()

        // Hiển thị thông báo nếu không có dữ liệu
        if (soldProductsList.isEmpty()) {
            binding.statisticsTitle.text = "Chưa có sản phẩm nào được bán"
        } else {
            binding.statisticsTitle.text = "Chi tiết sản phẩm bán chạy"
        }
    }
}