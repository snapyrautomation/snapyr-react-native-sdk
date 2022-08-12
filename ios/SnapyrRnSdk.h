#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface SnapyrRnSdk : RCTEventEmitter <RCTBridgeModule>

+ (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken;
+ (void)didReceiveRemoteNotification:(NSDictionary *)notification;

@end
