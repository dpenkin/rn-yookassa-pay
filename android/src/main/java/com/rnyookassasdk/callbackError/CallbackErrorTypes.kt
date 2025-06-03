package com.rnyookassasdk.callbackError

enum class CallbackErrorTypes {
    E_UNKNOWN {
        override fun toString(): String {
            return "E_UNKNOWN"
        }
    },
    E_PAYMENT_CANCELLED {
        override fun toString(): String {
            return "E_PAYMENT_CANCELLED"
        }
    }
}
