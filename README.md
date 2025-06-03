Yookassa SDK Checkout on React Native
=====

Android library: [7.1.0]

iOS library: [8.0.1]

Install
=======

```bash
yarn add rn-yookassa-pay
```

Usage
=====

```ts
import { tokenize, confirmPayment, dismiss, YooKassaError, ErrorCodesEnum, PaymentMethodTypesEnum } from 'rn-yookassa-pay';


try {
  const { paymentToken, paymentMethodType } = await tokenize({
        clientApplicationKey: CLIENT_APPLICATION_KEY,
        shopId: SHOP_ID,
        title: 'Товар',
        subtitle: 'Описание',
        price: 100,
        paymentMethodTypes: [
          PaymentMethodTypesEnum.BANK_CARD,
          PaymentMethodTypesEnum.SBP,
        ],
        isDebug: true,
      });

  console.warn(paymentToken.token) // payment token
  console.warn(paymentToken.type) // payment method type

// send token to your backend and get confirmationUrl for payment confirmation
// const confirmationUrl = <FETCH_FROM_BACKEND>

    await confirmPayment({
     confirmationUrl,
     paymentMethodType,
     clientApplicationKey: CLIENT_APPLICATION_KEY,
     shopId: SHOP_ID,
    });


  dismiss();
} catch (e) {
  console.error('Payment error')
}
```

Android
-------

minSdkVersion = 24

1. Add file `android/build.grade`
```xml
allprojects {
    repositories {
        maven {url 'https://developer.huawei.com/repo/'}
    }
}
```

iOS
---

Min CocoaPods version: 1.13.0

Min iOS version: 15.0

1. Add dependency in `ios/Podfile`
```ruby
source 'https://github.com/CocoaPods/Specs.git'
source 'https://git.yoomoney.ru/scm/sdk/cocoa-pod-specs.git'

...

...

target 'MyApp' do
  pod 'ReachabilitySwift', '~> 5.2.3'
  pod 'YooKassaPayments', :git => 'https://git.yoomoney.ru/scm/sdk/yookassa-payments-swift.git', :tag => '8.0.1'
  pod 'YandexMobileMetrica', '~> 4.5.0'
  pod 'CardIO', '~> 5.4.1'

# ... other dependencies

end
```

2. Install pods in `ios`
```bash
pod install
```
