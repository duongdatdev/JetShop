package com.shoppy.shop.screens.checkout.ordersummary

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shoppy.shop.data.DataOrException
import com.shoppy.shop.models.MCart
import com.shoppy.shop.repository.FireCartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class OrderSummaryScreenViewModel @Inject constructor(private val cartRepository: FireCartRepository): ViewModel() {

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val currentUser = FirebaseAuth.getInstance().currentUser
    private val dbOrders = FirebaseFirestore.getInstance().collection("Orders")
    private val dbCart = FirebaseFirestore.getInstance().collection("Cart")
    private val dbBuyNow = FirebaseFirestore.getInstance().collection("BuyNow")
    private val dbAllProducts = FirebaseFirestore.getInstance().collection("AllProducts")

    val fireSummary: MutableState<DataOrException<List<MCart>, Boolean, Exception>> =
        mutableStateOf(
            DataOrException(
                listOf(), true, Exception("")
            )
        )
        
    // For Buy Now items
    val buyNowItem: MutableState<DataOrException<MCart?, Boolean, Exception>> =
        mutableStateOf(
            DataOrException(
                null, true, Exception("")
            )
        )

    init {
        getCartItems()
//        getEmailPhone()
    }

    fun getCartItems() {
        viewModelScope.launch {
            fireSummary.value.loading = true
            fireSummary.value = cartRepository.getCartFromFireBase()
            if (!fireSummary.value.data.isNullOrEmpty()) {
                fireSummary.value.loading = false
            }
        }
    }
    
    // Get a Buy Now item by ID
    fun getBuyNowItem(buyNowId: String) {
        viewModelScope.launch {
            buyNowItem.value.loading = true
            
            try {
                val document = dbBuyNow.document(buyNowId).get().await()
                if (document.exists()) {
                    val item = document.toObject(MCart::class.java)
                    buyNowItem.value.data = item
                }
            } catch (e: Exception) {
                buyNowItem.value.e = e
            } finally {
                buyNowItem.value.loading = false
            }
        }
    }

    //Adding all List values
    fun sumValues(priceList: List<Int>, totalAmount: (Int) -> Unit) {
        //Delaying because values take time to load
        viewModelScope.launch {
            delay(100)
            val total = priceList.sum()
            totalAmount(total)
        }
    }

    fun getName(name: (String) -> Unit) {
        viewModelScope.launch {
            FirebaseFirestore.getInstance().collection("Users").document(currentUser?.email!!).get().addOnSuccessListener { name ->
                name(name.data?.getValue("name").toString()) }
        }
    }

    //Uploading items along with payment method and delivery status to Orders collection and Deleting items from Cart collection
    fun uploadToOrdersAndDeleteCart(itemsList: List<MCart>, paymentMethod: String, deliveryStatus: String, success:() -> Unit){
        viewModelScope.launch {
            for (mCart in itemsList){
                val category = when(mCart.category){
                    "BestSeller" -> "BestSeller"
                    "MobilePhones" -> "MobilePhones"
                    "EarPhones" -> "Earphones"
                    else -> "Tv"
                }

                val dbProducts = FirebaseFirestore.getInstance().collection(category)
                val updatedStock = if (mCart.stock!! > 0) mCart.stock!! - (1 * mCart.item_count!!) else 0

                //Multiplying price with no of items and adding delivery fees and adding 18% GST i.e 100 +180
                val totProductPrice = mCart.product_price!! * mCart.item_count!! + 280

                dbOrders.document(userId + mCart.product_title).set(mCart)
                dbOrders.document(userId + mCart.product_title).update("product_price",totProductPrice)
                //Updating Stock
                dbProducts.document(mCart.product_id!!).update("stock",updatedStock)
                dbAllProducts.document(mCart.product_id!!).update("stock",updatedStock)
                //Delaying to give time to update stock
                delay(1000)
                //Deleting Product Cart
                dbCart.document(userId + mCart.product_title).delete()

                //Uploading payment method to "Orders" collection
                dbOrders.document(userId + mCart.product_title).update("payment_method",paymentMethod)

                //Uploading delivery status to "Orders" collection
                dbOrders.document(userId + mCart.product_title).update("delivery_status",deliveryStatus)

                //Ensure product_id is set in the order
                dbOrders.document(userId + mCart.product_title).update("product_id", mCart.product_id!!)

                //Getting date using SimpleDateFormat()
                val simpleDate = SimpleDateFormat("dd-MM-yyy", Locale.ENGLISH)
                val date = Date()
                val orderDate = simpleDate.format(date).toString()

                //Uploading date to "Orders" collection
                dbOrders.document(userId + mCart.product_title).update("order_date",orderDate)

                //Generating random numbers and uploading as Order Id
                val orderId = UUID.randomUUID().toString()
                dbOrders.document(userId + mCart.product_title).update("order_id",orderId)

                val notificationMap = hashMapOf<String, Any>("notificationCount" to 1)
                dbOrders.document(userId + mCart.product_title).update(notificationMap)
            }
            success()
        }
    }
    
    // Upload a single Buy Now item to orders and delete from BuyNow collection
    fun uploadBuyNowItemToOrders(buyNowId: String, paymentMethod: String, deliveryStatus: String, success:() -> Unit) {
        viewModelScope.launch {
            val mCart = buyNowItem.value.data ?: return@launch
            
            val category = when(mCart.category){
                "BestSeller" -> "BestSeller"
                "MobilePhones" -> "MobilePhones"
                "EarPhones" -> "Earphones"
                else -> "Tv"
            }

            val dbProducts = FirebaseFirestore.getInstance().collection(category)
            val updatedStock = if (mCart.stock!! > 0) mCart.stock!! - (1 * mCart.item_count!!) else 0

            // Add delivery fees and GST (100 + 180 = 280)
            val totProductPrice = mCart.product_price!! * mCart.item_count!! + 280

            dbOrders.document(userId + mCart.product_title).set(mCart)
            dbOrders.document(userId + mCart.product_title).update("product_price", totProductPrice)
            
            // Update stock
            dbProducts.document(mCart.product_id!!).update("stock", updatedStock)
            dbAllProducts.document(mCart.product_id!!).update("stock", updatedStock)
            
            // Wait for stock update
            delay(1000)
            
            // Delete from BuyNow
            dbBuyNow.document(buyNowId).delete()

            // Set payment method
            dbOrders.document(userId + mCart.product_title).update("payment_method", paymentMethod)

            // Set delivery status
            dbOrders.document(userId + mCart.product_title).update("delivery_status", deliveryStatus)

            // Ensure product_id is set
            dbOrders.document(userId + mCart.product_title).update("product_id", mCart.product_id!!)

            // Add order date
            val simpleDate = SimpleDateFormat("dd-MM-yyy", Locale.ENGLISH)
            val date = Date()
            val orderDate = simpleDate.format(date).toString()
            dbOrders.document(userId + mCart.product_title).update("order_date", orderDate)

            // Generate and set order ID
            val orderId = UUID.randomUUID().toString()
            dbOrders.document(userId + mCart.product_title).update("order_id", orderId)

            // Set notification count
            val notificationMap = hashMapOf<String, Any>("notificationCount" to 1)
            dbOrders.document(userId + mCart.product_title).update(notificationMap)
            
            success()
        }
    }
}