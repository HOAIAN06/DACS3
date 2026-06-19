package com.fastdash.app.utils

import android.content.Context

class PendingPaymentStore(context: Context) {
    private val prefs = context.getSharedPreferences("fastdash_pending_payment", Context.MODE_PRIVATE)

    fun save(orderId: Long, paymentUrl: String?) {
        prefs.edit()
            .putLong(KEY_ORDER_ID, orderId)
            .putString(KEY_PAYMENT_URL, paymentUrl)
            .apply()
    }

    fun getOrderId(): Long? {
        val value = prefs.getLong(KEY_ORDER_ID, -1L)
        return value.takeIf { it > 0L }
    }

    fun getPaymentUrl(): String? {
        return prefs.getString(KEY_PAYMENT_URL, null)?.takeIf { it.isNotBlank() }
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_ORDER_ID = "order_id"
        private const val KEY_PAYMENT_URL = "payment_url"
    }
}
