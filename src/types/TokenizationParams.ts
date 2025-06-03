import type { PaymentMethodTypesEnum, GooglePaymentMethodTypesEnum } from './';

export interface TokenizationParams {
  clientApplicationKey: string;
  shopId: string;
  title: string;
  subtitle: string;
  price: number;
  paymentMethodTypes?: PaymentMethodTypesEnum[];
  authCenterClientId?: string;
  userPhoneNumber?: string;
  gatewayId?: string;
  customerId?: string;
  returnUrl?: string;
  googlePaymentMethodTypes?: GooglePaymentMethodTypesEnum[];
  applePayMerchantId?: string;
  isDebug?: boolean;
  applicationScheme?: string;
}
