#import <React/RCTViewManager.h>
#import <Foundation/Foundation.h>
#import "RnYookassaSdk-Bridging-Header.h"

@interface RCT_EXTERN_MODULE(RnYookassaSdk, RCTViewManager)

RCT_EXTERN_METHOD(tokenize:(NSDictionary *)params
                  callbacker:(RCTResponseSenderBlock)callback)
RCT_EXTERN_METHOD(confirmPayment:(NSDictionary *)params
                  callbacker:(RCTResponseSenderBlock)callback)
RCT_EXTERN_METHOD(dismiss)

@end