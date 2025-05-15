package com.example.admindacs3.model

import android.os.Parcel
import android.os.Parcelable

class OrderDetails() : Parcelable {
    var userUid: String? = null
    var userName: String? = null
    var foodNames: MutableList<String> = mutableListOf()
    var foodImages: MutableList<String> = mutableListOf()
    var foodPrices: MutableList<String> = mutableListOf()
    var foodQuantities: MutableList<String> = mutableListOf() // Sử dụng String thay vì Int để phù hợp với Firebase
    var address: String? = null
    var totalPrice: String? = null
    var phoneNumber: String? = null
    var orderAccepted: Boolean = false
    var paymentReceived: Boolean = false
    var itemPushKey: String? = null
    var currentTime: Long = 0   // Giữ nguyên kiểu Long cho timestamp

    constructor(parcel: Parcel) : this() {
        userUid = parcel.readString()
        userName = parcel.readString()
        parcel.readStringList(foodNames)
        parcel.readStringList(foodImages)
        parcel.readStringList(foodPrices)
        parcel.readStringList(foodQuantities)
        address = parcel.readString()
        totalPrice = parcel.readString()
        phoneNumber = parcel.readString()
        orderAccepted = parcel.readByte() != 0.toByte()
        paymentReceived = parcel.readByte() != 0.toByte()
        itemPushKey = parcel.readString()
        currentTime = parcel.readLong()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userUid)
        parcel.writeString(userName)
        parcel.writeStringList(foodNames)
        parcel.writeStringList(foodImages)
        parcel.writeStringList(foodPrices)
        parcel.writeStringList(foodQuantities)
        parcel.writeString(address)
        parcel.writeString(totalPrice)
        parcel.writeString(phoneNumber)
        parcel.writeByte(if (orderAccepted) 1 else 0)
        parcel.writeByte(if (paymentReceived) 1 else 0)
        parcel.writeString(itemPushKey)
        parcel.writeLong(currentTime)
    }

    companion object CREATOR : Parcelable.Creator<OrderDetails> {
        override fun createFromParcel(parcel: Parcel): OrderDetails {
            return OrderDetails(parcel)
        }

        override fun newArray(size: Int): Array<OrderDetails?> {
            return arrayOfNulls(size)
        }
    }
}