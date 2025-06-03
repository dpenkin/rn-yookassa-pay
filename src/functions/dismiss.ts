import { NativeModules } from 'react-native';

const RnYookassa = NativeModules.RnYookassaSdk;

export function dismiss(): void {
  RnYookassa.dismiss();
}
