import Foundation
import React
import YooKassaPayments

@objc(RnYookassaSdk)
class RnYookassaSdk: RCTViewManager, TokenizationModuleOutput {
  
    var callback: RCTResponseSenderBlock?
    var confirmCallback: RCTResponseSenderBlock?
    var viewController: UIViewController?

    @objc
    func tokenize(_ params: NSDictionary, callbacker callback: @escaping RCTResponseSenderBlock) -> Void {
        self.callback = callback
        guard let clientApplicationKey = params["clientApplicationKey"] as? String,
              let _shopId = params["shopId"] as? String,
              let title = params["title"] as? String,
              let subtitle = params["subtitle"] as? String,
              let amountValue = params["price"] as? NSNumber
        else {
            return
        }

        let paymentTypes = params["paymentMethodTypes"] as? [String]
        let authCenterClientId = params["authCenterClientId"] as? String
        let userPhoneNumber = params["userPhoneNumber"] as? String
        let customerId = params["customerId"] as? String
        let gatewayId = params["gatewayId"] as? String
        let applePayMerchantId = params["applePayMerchantId"] as? String
        let returnUrl = params["returnUrl"] as? String
        let applicationScheme = params["applicationScheme"] as? String
        let isDebug = params["isDebug"] as? Bool

        var paymentMethodTypes: PaymentMethodTypes = []
        
        if let paymentTypes = paymentTypes {
            paymentTypes.forEach { type in
                if let payType = PaymentMethodType(rawValue: type.lowercased()) {
                    if payType == .yooMoney && authCenterClientId == nil { return }
                    paymentMethodTypes.insert(PaymentMethodTypes(rawValue: [payType]))
                }
            }
        } else {
            paymentMethodTypes.insert([.bankCard, .sberbank, .sbp])
            if authCenterClientId != nil {
                paymentMethodTypes.insert(.yooMoney)
            }
        }

        let testModeSettings = TestModeSettings(paymentAuthorizationPassed: false,
                                                cardsCount: 2,
                                                charge: Amount(value: 10, currency: .rub),
                                                enablePaymentError: false)

        let tokenizationSettings = TokenizationSettings(paymentMethodTypes: paymentMethodTypes)
        let customizationSettings = CustomizationSettings(showYooKassaLogo: false)
        let amount = Amount(value: amountValue.decimalValue, currency: .rub)

        let tokenizationModuleInputData = TokenizationModuleInputData(
            clientApplicationKey: clientApplicationKey,
            shopName: title, shopId: _shopId,
            purchaseDescription: subtitle,
            amount: amount,
            gatewayId: gatewayId,
            tokenizationSettings: tokenizationSettings,
            testModeSettings: (isDebug == true ? testModeSettings : nil),
            cardScanning: nil,
            returnUrl: returnUrl,
            isLoggingEnabled: (isDebug == true),
            userPhoneNumber: userPhoneNumber,
            customizationSettings: customizationSettings,
            savePaymentMethod: .off,
            moneyAuthClientId: authCenterClientId,
            applicationScheme: applicationScheme,
            customerId: customerId
        )

        DispatchQueue.main.async {
            let inputData: TokenizationFlow = .tokenization(tokenizationModuleInputData)
            self.viewController = TokenizationAssembly.makeModule(inputData: inputData, moduleOutput: self)
            let rootViewController = UIApplication.shared.keyWindow!.rootViewController!
            rootViewController.present(self.viewController!, animated: true, completion: nil)
        }
    }

    @objc
    func confirmPayment(_ params: NSDictionary, callbacker callback: @escaping RCTResponseSenderBlock) -> Void {
        guard let confirmationUrl = params["confirmationUrl"] as? String,
              let _paymentMethodType = params["paymentMethodType"] as? String,
              let paymentMethodType = PaymentMethodType(rawValue: _paymentMethodType.lowercased())
        else {
            return
        }

        guard let viewController = viewController as? TokenizationModuleInput else { return }
        confirmCallback = callback
        viewController.startConfirmationProcess(confirmationUrl: confirmationUrl, paymentMethodType: paymentMethodType)
    }

    @objc
    func dismiss() {
        DispatchQueue.main.async {
            self.viewController?.dismiss(animated: true)
        }
    }

    func tokenizationModule(_ module: TokenizationModuleInput, didTokenize token: Tokens, paymentMethodType: PaymentMethodType) {
        let result: NSDictionary = [
            "paymentToken" : token.paymentToken,
            "paymentMethodType" : paymentMethodType.rawValue.uppercased()
        ]

        if let callback = callback {
            callback([result])
            self.callback = nil
        }
    }

    func didFinish(on module: TokenizationModuleInput, with error: YooKassaPaymentsError?) {
        let errorResult: NSDictionary = [
            "code" : "E_PAYMENT_CANCELLED",
            "message" : "Payment cancelled."
        ]

        DispatchQueue.main.async {
            self.viewController?.dismiss(animated: true)
        }

      if let callback = self.confirmCallback {
          callback([errorResult])
          self.confirmCallback = nil
      }
    }

    func didFinishConfirmation(paymentMethodType: PaymentMethodType) {
        let result: NSDictionary = [
            "paymentMethodType" : paymentMethodType.rawValue.uppercased(),
            "status": "success"
        ]
        
        DispatchQueue.main.async {
            self.viewController?.dismiss(animated: true)
        }

        if let callback = self.confirmCallback {
            callback([result])
            self.confirmCallback = nil
        }
    }

    func didFailConfirmation(error: YooKassaPaymentsError?) {
        let errorResult: NSDictionary = [
            "code": "E_CONFIRMATION_FAILED",
            "message": error?.localizedDescription ?? "Confirmation failed."
        ]
        
        DispatchQueue.main.async {
            self.viewController?.dismiss(animated: true)
        }

        if let callback = self.confirmCallback {
            callback([NSNull(), errorResult])
            self.confirmCallback = nil
        }
    }

    override class func requiresMainQueueSetup() -> Bool {
        return false
    }

    func didSuccessfullyPassedCardSec(on module: TokenizationModuleInput) {}
}
