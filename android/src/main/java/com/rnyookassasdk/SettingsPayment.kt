package com.rnyookassasdk

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod

class SettingsPayment(context: Context) {

    companion object {
        const val KEY_SAVE_PAYMENT_METHOD = "save_payment_method"
    }

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun getSavePaymentMethod(): SavePaymentMethod {
        return getSavePaymentMethod(getSavePaymentMethodId())
    }

    private fun getSavePaymentMethodId(): Int {
        return sharedPreferences.getInt(KEY_SAVE_PAYMENT_METHOD, 0)
    }

    private fun getSavePaymentMethod(value: Int): SavePaymentMethod {
        return when (value) {
            1 -> SavePaymentMethod.ON
            2 -> SavePaymentMethod.OFF
            else -> SavePaymentMethod.USER_SELECTS
        }
    }
}
