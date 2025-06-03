import { TouchableOpacity, StyleSheet, View, Text } from 'react-native';
import {
  tokenize,
  confirmPayment,
  dismiss,
  YooKassaError,
  ErrorCodesEnum,
  PaymentMethodTypesEnum,
} from 'rn-yookassa-pay';
import { CLIENT_APPLICATION_KEY, CONFIRMATION_URL, SHOP_ID } from './constants';

export default function App() {
  const emulateApiRequest = async (_paymentToken: string): Promise<string> => {
    return new Promise<string>((res) => {
      setTimeout(() => {
        res(CONFIRMATION_URL);
      }, 3000);
    });
  };

  const onPayPress = async () => {
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

      console.log(
        `Tokenization was successful. paymentToken: ${paymentToken}, paymentMethodType: ${paymentMethodType}`
      );

      const confirmationUrl = await emulateApiRequest(paymentToken);

      console.log(`Got confirmationUrl from your API: ${confirmationUrl}`);

      await confirmPayment({
        confirmationUrl,
        paymentMethodType,
        clientApplicationKey: CLIENT_APPLICATION_KEY,
        shopId: SHOP_ID,
      });

      console.log('Payment was confirmed!');

      dismiss();
    } catch (err) {
      if (err instanceof YooKassaError) {
        switch (err.code) {
          case ErrorCodesEnum.E_PAYMENT_CANCELLED:
            console.log('User cancelled YooKassa module.');
        }
      }
    }
  };

  return (
    <View style={styles.container}>
      <TouchableOpacity style={styles.button} onPress={onPayPress}>
        <Text style={styles.buttonTitle}>Оплатить</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  button: {
    backgroundColor: 'green',
    width: 300,
    height: 60,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 10,
  },
  buttonTitle: {
    color: 'white',
    fontSize: 20,
  },
});
