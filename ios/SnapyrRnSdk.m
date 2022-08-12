#import "SnapyrRnSdk.h"
#import <Snapyr/Snapyr.h>

@implementation SnapyrRnSdk

RCT_EXPORT_MODULE()

// See // https://reactnative.dev/docs/native-modules-ios

RCT_REMAP_METHOD(configure,
                 withKey:(nonnull NSString*)key
                 options:(NSDictionary*)_options
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
{
    SnapyrSDKConfiguration *configuration = [SnapyrSDKConfiguration configurationWithWriteKey:key];
    if ([_options objectForKey:@"trackApplicationLifecycleEvents"]) {
        configuration.trackApplicationLifecycleEvents = YES; // Enable this to record certain application events automatically
    }
    if ([_options objectForKey:@"recordScreenViews"]) {
        configuration.recordScreenViews = YES; // Enable this to record screen views automatically
    }
    if ([_options objectForKey:@"enableDevMode"]) {
        configuration.enableDevEnvironment = YES; // Test against a Snapyr dev environment *internal only*
    }
    
    // makes every event flush to network immediately
    configuration.flushAt = 1;
    
    [SnapyrSDK setupWithConfiguration:configuration];
    
    resolve(key);
}

RCT_EXPORT_METHOD(identify:(NSString*)_userId traits:(NSDictionary*)_traits)
{

    if([_userId isEqual:@""])
    {
        [[SnapyrSDK sharedSDK] identify:nil traits:_traits];
        return;
    }
    [[SnapyrSDK sharedSDK] identify:_userId traits:_traits];
}

RCT_EXPORT_METHOD(track:(NSString*)_event traits:(NSDictionary*)_traits)
{
    [[SnapyrSDK sharedSDK] track:_event properties:_traits];
}

RCT_EXPORT_METHOD(setPushNotificationToken:(NSString*)_token)
{
    NSLog(@"%@ tokenNSLOG", _token);
    [[SnapyrSDK sharedSDK] setPushNotificationToken: _token];
}

RCT_EXPORT_METHOD(pushNotificationReceived:(NSDictionary*)_snapyrData)
{
    [[SnapyrSDK sharedSDK] pushNotificationReceived:[_snapyrData copy]];
}

RCT_EXPORT_METHOD(pushNotificationTapped:(NSDictionary*)_snapyrData actionId:(NSString* _Nullable)actionId)
{
    // TODO: add back actionId after releasing iOS SDK update w/ Action Buttons support
    //    [[SnapyrSDK sharedSDK] pushNotificationTapped:[_snapyrData copy] actionId:actionId];
    [[SnapyrSDK sharedSDK] pushNotificationTapped:[_snapyrData copy]];
}


/**
 Native iOS -> React Native events (trigger RN callbacks from native)
 */

- (NSArray<NSString *> *)supportedEvents
{
  return @[
      @"snapyrDidRegister",
      @"snapyrDidReceiveNotification",
  ];
}

// https://reactnative.dev/docs/native-modules-ios#sending-events-to-javascript
// Will be called when this module's first listener is added.
// NB (PS): React Native auto initializes an instance of this class at startup and `startObserving` is
// called to register any listeners we need.
//
// We provide class methods (kinda like static methods) for end user code to call so they don't need
// to manage lifecycle. Those class methods use NSNotificationCenter to pass the data along through
// events; we listen to those events here in the actual instance, where we can then pass data back
// into React Native.
-(void)startObserving {
    NSLog(@"XXX: startObserving");
    // Set up any upstream listeners or background tasks as necessary
    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(handleSnapyrDidRegister:)
     name:@"snapyrDidRegister"
     object:nil];
    
    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(handleSnapyrDidReceiveNotification:)
     name:@"snapyrDidReceiveNotification"
     object:nil];
}

// Will be called when this module's last listener is removed, or on dealloc.
-(void)stopObserving {
    NSLog(@"XXX: stopObserving");
    // Remove upstream listeners, stop unnecessary background tasks
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}


- (void)handleSnapyrDidRegister:(NSNotification *)notification
{
    NSLog(@"XXX: handleSnapyrDidRegister: %@", notification.userInfo);
    NSString *token = notification.userInfo[@"token"];
    [self sendEventWithName:@"snapyrDidRegister" body:token];
}

- (void)handleSnapyrDidReceiveNotification:(NSNotification *)notification
{
    NSLog(@"XXX: handleSnapyrDidReceiveNotification: %@", notification.userInfo);
    NSDictionary *notif = notification.userInfo[@"notification"];
    [self sendEventWithName:@"snapyrDidReceiveNotification" body:notif];
}

+ (void)didReceiveRemoteNotification:(NSDictionary *)notification
{
    NSLog(@"XXX: didReceiveRemoteNotification: %@", notification);
    [[NSNotificationCenter defaultCenter]
     postNotificationName:@"snapyrDidReceiveNotification"
     object:self
     userInfo:@{@"notification": notification}];
}

// Class method the consumer can call when application(_:didRegisterForRemoteNotificationsWithDeviceToken:)
// has been triggered on the AppDelegate.
// Decodes token and passes it through NSNotificationCenter so it can be processed by the instance method
// `handleSnapyrDidRegister`
+ (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
    NSLog(@"XXX: didRegisterForRemoteNotificationsWithDeviceToken: %@", deviceToken);
    NSUInteger dataLength = deviceToken.length;
    if (dataLength == 0) {
        return;
    }
    
    const unsigned char *dataBuffer = (const unsigned char *)deviceToken.bytes;
    NSMutableString *hexString = [NSMutableString stringWithCapacity:(dataLength * 2)];
    for (int i = 0; i < dataLength; ++i) {
        [hexString appendFormat:@"%02x", dataBuffer[i]];
    }
    
    [[NSNotificationCenter defaultCenter]
     postNotificationName:@"snapyrDidRegister"
     object:self
     userInfo:@{@"token" : [hexString copy]}];
}

@end
