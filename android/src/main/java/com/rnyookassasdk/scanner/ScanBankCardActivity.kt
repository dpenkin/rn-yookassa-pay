package com.rnyookassasdk.scanner

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import io.card.payment.CardIOActivity
import io.card.payment.CreditCard
import ru.yoomoney.sdk.kassa.payments.Checkout

class ScanBankCardActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {
            val scanIntent = Intent(this, CardIOActivity::class.java).apply {
                putExtra(CardIOActivity.EXTRA_SCAN_EXPIRY, true)
                putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true)
                putExtra(CardIOActivity.EXTRA_GUIDE_COLOR, Color.WHITE)
                putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, true)
                putExtra(CardIOActivity.EXTRA_HIDE_CARDIO_LOGO, true)
            }
            startActivityForResult(scanIntent, REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE) {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                val scanResult: CreditCard? = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT)
                scanResult?.let {
                    val cardNumber = it.formattedCardNumber

                    if (it.isExpiryValid && !it.redactedCardNumber.isNullOrEmpty()) {
                        val scanBankCardResult = Checkout.createScanBankCardIntent(
                            cardNumber,
                            it.expiryMonth,
                            it.expiryYear % 100
                        )
                        setResult(RESULT_OK, scanBankCardResult)
                    } else {
                        setResult(RESULT_CANCELED)
                    }
                } ?: setResult(RESULT_CANCELED)
            } else {
                setResult(RESULT_CANCELED)
            }
            finish()
        }
    }
}
