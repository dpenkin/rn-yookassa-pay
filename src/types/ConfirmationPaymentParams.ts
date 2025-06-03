import type { PaymentMethodTypesEnum } from './';

export interface ConfirmationPaymentParams {
  confirmationUrl: string;
  paymentMethodType: PaymentMethodTypesEnum;
  clientApplicationKey: string;
  shopId: string;
}
