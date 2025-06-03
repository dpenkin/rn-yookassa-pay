import type { PaymentMethodTypesEnum } from './';

export interface TokenizationResult {
  paymentToken: string;
  paymentMethodType: PaymentMethodTypesEnum;
}
