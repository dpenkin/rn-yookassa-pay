package com.rnyookassasdk

import android.app.Activity
import android.content.Intent
import androidx.annotation.NonNull
import com.facebook.react.bridge.*
import com.rnyookassasdk.callbackError.CallbackError
import com.rnyookassasdk.callbackError.CallbackErrorTypes
import com.rnyookassasdk.callbackSuccess.CallbackTokenizeSuccess
import com.rnyookassasdk.SettingsPayment
import ru.yoomoney.sdk.kassa.payments.Checkout
import ru.yoomoney.sdk.kassa.payments.TokenizationResult
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.*

import java.math.BigDecimal
import java.util.*

class RnYookassaSdkModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val reactContext: ReactApplicationContext = reactContext
    private var paymentCallback: Callback? = null

    companion object {
        private const val REQUEST_CODE_TOKENIZE = 33
        private const val REQUEST_CODE_3DSECURE = 35
    }

    // Инициализация mActivityEventListener сразу при объявлении
    private val mActivityEventListener = object : BaseActivityEventListener() {
        override fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, data: Intent?) {
            when (requestCode) {
                REQUEST_CODE_TOKENIZE -> handleTokenizeResult(resultCode, data)
                REQUEST_CODE_3DSECURE -> handle3DSecureResult(resultCode)
            }
        }
    }

    init {
        // Добавление слушателя событий активности
        reactContext.addActivityEventListener(mActivityEventListener)
    }

    override fun getName(): String = "RnYookassaSdk"

    @ReactMethod
    fun tokenize(params: ReadableMap, callback: Callback) {
        paymentCallback = callback

        val clientApplicationKey = params.getString("clientApplicationKey")!!
        val shopId = params.getString("shopId")!!
        val title = params.getString("title")!!
        val subtitle = params.getString("subtitle")!!
        val amount = BigDecimal(params.getDouble("price"))
        val paymentTypes = params.getArray("paymentMethodTypes")
        val customerId = params.getString("customerId")
        val authCenterClientId = params.getString("authCenterClientId")
        val userPhoneNumber = params.getString("userPhoneNumber")
        val gatewayId = params.getString("gatewayId")
        val returnUrl = params.getString("returnUrl")
        val googlePaymentTypes = params.getArray("googlePaymentMethodTypes")
        val isDebug = params.hasKey("isDebug") && params.getBoolean("isDebug")

        val settings = SettingsPayment(reactContext)
        val paymentMethodTypes = getPaymentMethodTypes(paymentTypes, authCenterClientId != null)
        val googlePaymentMethodTypes = getGooglePaymentMethodTypes(googlePaymentTypes)
        val savePaymentMethod = settings.getSavePaymentMethod()

        val paymentParameters = PaymentParameters(
            Amount(amount, Currency.getInstance("RUB")),
            title,
            subtitle,
            clientApplicationKey,
            shopId,
            savePaymentMethod,
            paymentMethodTypes,
            gatewayId,
            returnUrl,
            userPhoneNumber,
            GooglePayParameters(googlePaymentMethodTypes),
            authCenterClientId,
            customerId,
        )

        val testParameters = TestParameters(
            showLogs = false,
            googlePayTestEnvironment = true,
            mockConfiguration = MockConfiguration(
                completeWithError = false,
                paymentAuthPassed = true,
                linkedCardsCount = 5,
                serviceFee = Amount(BigDecimal.TEN, Currency.getInstance("RUB"))
            )
        )

        val intent = if (isDebug) {
            Checkout.createTokenizeIntent(reactContext, paymentParameters, testParameters)
        } else {
            Checkout.createTokenizeIntent(reactContext, paymentParameters)
        }

        currentActivity?.startActivityForResult(intent, REQUEST_CODE_TOKENIZE)
    }

    @ReactMethod
    fun confirmPayment(params: ReadableMap, callback: Callback) {
        paymentCallback = callback

        val confirmationUrl = params.getString("confirmationUrl")!!
        val clientApplicationKey = params.getString("clientApplicationKey")!!
        val shopId = params.getString("shopId")!!
        val paymentMethodType = PaymentMethodType.valueOf(params.getString("paymentMethodType")!!)

        val activity = currentActivity ?: run {
            val error = CallbackError(CallbackErrorTypes.E_UNKNOWN, "Payment confirmation error.")
            paymentCallback?.invoke(false, generateErrorMapCallback(error))
            return
        }

        val intent = Checkout.createConfirmationIntent(reactContext, confirmationUrl, paymentMethodType, clientApplicationKey, shopId)
        activity.startActivityForResult(intent, REQUEST_CODE_3DSECURE)
    }

    @ReactMethod
    fun dismiss() {
        // No-op
    }

    private fun handleTokenizeResult(resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                val result = Checkout.createTokenizationResult(data!!)
                val token = result.paymentToken
                val type = result.paymentMethodType.name.toUpperCase()

                val callbackResult = CallbackTokenizeSuccess(token, type)
                paymentCallback?.invoke(generateTokenizeSuccessCallback(callbackResult))
            }
            Activity.RESULT_CANCELED -> {
                val error = CallbackError(CallbackErrorTypes.E_PAYMENT_CANCELLED, "Payment cancelled.")
                paymentCallback?.invoke(null, generateErrorMapCallback(error))
            }
        }
    }

    private fun handle3DSecureResult(resultCode: Int) {
        when (resultCode) {
            Activity.RESULT_OK -> paymentCallback?.invoke(true)
            Activity.RESULT_CANCELED, Checkout.RESULT_ERROR -> {
                val error = CallbackError(CallbackErrorTypes.E_PAYMENT_CANCELLED, "Payment cancelled.")
                paymentCallback?.invoke(null, generateErrorMapCallback(error))
            }
        }
    }

    private fun generateTokenizeSuccessCallback(result: CallbackTokenizeSuccess): WritableMap {
        return Arguments.createMap().apply {
            putString("paymentToken", result.paymentToken)
            putString("paymentMethodType", result.paymentMethodType)
        }
    }

    private fun generateErrorMapCallback(error: CallbackError): WritableMap {
        return Arguments.createMap().apply {
            putString("code", error.code.toString())
            putString("message", error.message)
        }
    }

    private fun getPaymentMethodTypes(paymentTypes: ReadableArray?, authCenterClientIdProvided: Boolean): Set<PaymentMethodType> {
        val methodTypes = mutableSetOf<PaymentMethodType>()
        paymentTypes?.let {
            for (i in 0 until it.size()) {
                when (it.getString(i)?.toUpperCase()) {
                    "BANK_CARD" -> methodTypes.add(PaymentMethodType.BANK_CARD)
                    "SBERBANK" -> methodTypes.add(PaymentMethodType.SBERBANK)
                    "SBP" -> methodTypes.add(PaymentMethodType.SBP)
                    "GOOGLE_PAY" -> methodTypes.add(PaymentMethodType.GOOGLE_PAY)
                    "YOO_MONEY" -> if (authCenterClientIdProvided) methodTypes.add(PaymentMethodType.YOO_MONEY)
                }
            }
        } ?: run {
            methodTypes.add(PaymentMethodType.BANK_CARD)
            methodTypes.add(PaymentMethodType.SBERBANK)
            methodTypes.add(PaymentMethodType.SBP)
            methodTypes.add(PaymentMethodType.GOOGLE_PAY)
            if (authCenterClientIdProvided) methodTypes.add(PaymentMethodType.YOO_MONEY)
        }
        return methodTypes
    }

    private fun getGooglePaymentMethodTypes(googlePaymentTypes: ReadableArray?): Set<GooglePayCardNetwork> {
        val googleMethodTypes = mutableSetOf<GooglePayCardNetwork>()
        googlePaymentTypes?.let {
            for (i in 0 until it.size()) {
                when (it.getString(i)?.toUpperCase()) {
                    "AMEX" -> googleMethodTypes.add(GooglePayCardNetwork.AMEX)
                    "DISCOVER" -> googleMethodTypes.add(GooglePayCardNetwork.DISCOVER)
                    "JCB" -> googleMethodTypes.add(GooglePayCardNetwork.JCB)
                    "MASTERCARD" -> googleMethodTypes.add(GooglePayCardNetwork.MASTERCARD)
                    "VISA" -> googleMethodTypes.add(GooglePayCardNetwork.VISA)
                    "INTERAC" -> googleMethodTypes.add(GooglePayCardNetwork.INTERAC)
                    "OTHER" -> googleMethodTypes.add(GooglePayCardNetwork.OTHER)
                }
            }
        } ?: googleMethodTypes.addAll(
            listOf(
                GooglePayCardNetwork.AMEX,
                GooglePayCardNetwork.DISCOVER,
                GooglePayCardNetwork.JCB,
                GooglePayCardNetwork.MASTERCARD,
                GooglePayCardNetwork.VISA,
                GooglePayCardNetwork.INTERAC,
                GooglePayCardNetwork.OTHER
            )
        )
        return googleMethodTypes
    }
}

