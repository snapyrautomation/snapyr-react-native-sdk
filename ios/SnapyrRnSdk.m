#import "SnapyrRnSdk.h"
#import <Snapyr/SnapyrSDK.h>
#import <Snapyr/SnapyrInAppMessage.h>

@implementation SnapyrRnSdk

RCT_EXPORT_MODULE()

// See https://reactnative.dev/docs/native-modules-ios

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
    if ([_options objectForKey:@"snapyrEnvironment"]) {
        NSNumber *e = [_options valueForKey:@"snapyrEnvironment"];
        // NB this relies on integer-based enums with the same values between React and iOS
        configuration.snapyrEnvironment = (SnapyrEnvironment)e.integerValue; // Test against a Snapyr dev environment *internal only*
    }
    configuration.actionHandler = ^(SnapyrInAppMessage *message){
        [self handleSnapyrInAppMessage:message];
    };
    // makes every event flush to network immediately
    configuration.flushAt = 1;
    
    [SnapyrSDK setupWithConfiguration:configuration];
    
    [self addListener:@"snapyrDidRegister"];
    
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

RCT_EXPORT_METHOD(trackInAppMessageImpression:(NSString *)_actionToken)
{
    [[SnapyrSDK sharedSDK] trackInAppMessageImpressionWithActionToken:_actionToken];
}
                  
RCT_EXPORT_METHOD(trackInAppMessageClick:(NSString *)_actionToken properties:(NSDictionary *)_properties)
{
    [[SnapyrSDK sharedSDK] trackInAppMessageClickWithActionToken:_actionToken withProperties:_properties];
}

RCT_EXPORT_METHOD(trackInAppMessageDismiss:(NSString *)_actionToken)
{
    [[SnapyrSDK sharedSDK] trackInAppMessageDismissWithActionToken:_actionToken];
}

RCT_EXPORT_METHOD(reset)
{
    NSLog(@"SnapyrRnSdk: reset: not implemented");
    // Do nothing, for now... stub method to maintain compat w/ Android
}


/**
 Native iOS -> React Native events (trigger RN callbacks from native)
 */

- (NSArray<NSString *> *)supportedEvents
{
  return @[
      @"snapyrDidRegister",
      @"snapyrDidReceiveNotification",
      @"snapyrDidReceiveNotificationResponse",
      @"snapyrTestListener",
      @"snapyrTest",
      @"snapyrInAppMessage",
  ];
}

// https://reactnative.dev/docs/native-modules-ios#sending-events-to-javascript
// Will be called when this module's first listener is added.
// NB (PS): React Native auto initializes an instance of this class at startup and `startObserving` is
// called to register any listeners we need.
//
// We provide class methods (i.e. static methods) for end user code to call so they don't need
// to manage lifecycle. Those class methods use NSNotificationCenter to pass the data along through
// events; we listen to those events here in the actual instance, where we can then pass data back
// into React Native.
-(void)startObserving {
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
    
    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(handleSnapyrDidReceiveNotificationResponse:)
     name:@"snapyrDidReceiveNotificationResponse"
     object:nil];
}

// Will be called when this module's last listener is removed, or on dealloc.
-(void)stopObserving {
    // Remove upstream listeners, stop unnecessary background tasks
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)handleSnapyrInAppMessage:(SnapyrInAppMessage *)message
{
    // Body of events to RN must be JSON serializable so call asDict
    [self sendEventWithName:@"snapyrInAppMessage" body:[message asDict]];
}

- (void)handleSnapyrDidRegister:(NSNotification *)notification
{
    NSString *token = notification.userInfo[@"token"];
    [self sendEventWithName:@"snapyrDidRegister" body:token];
}

- (void)handleSnapyrDidReceiveNotification:(NSNotification *)notification
{
    NSDictionary *notif = notification.userInfo[@"notification"];
    [self sendEventWithName:@"snapyrDidReceiveNotification" body:notif];
}

- (void)handleSnapyrDidReceiveNotificationResponse:(NSNotification *)notification
{
    [self sendEventWithName:@"snapyrDidReceiveNotificationResponse" body:notification.userInfo];
}


- (void)handleTest:(NSNotification *)notification
{
    NSString *notif = notification.userInfo[@"notification"];
    [self sendEventWithName:@"snapyrTest" body:notif];
}

// Class method the consumer can call when application(_:didRegisterForRemoteNotificationsWithDeviceToken:)
// has been triggered on the AppDelegate.
// Decodes token and passes it through NSNotificationCenter so it can be processed by the instance method
// `handleSnapyrDidRegister`
+ (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
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

+ (void)didReceiveRemoteNotification:(NSDictionary *)notification
{
    [[NSNotificationCenter defaultCenter]
     postNotificationName:@"snapyrDidReceiveNotification"
     object:self
     userInfo:@{@"notification": notification}];
}

+ (void)didReceiveNotificationResponse:(UNNotificationResponse *)response
API_AVAILABLE(ios(10.0))
{
    NSString *actionIdentifier = response.actionIdentifier;
    NSDictionary *userInfo = [response.notification.request.content.userInfo copy];
    [[NSNotificationCenter defaultCenter]
     postNotificationName:@"snapyrDidReceiveNotificationResponse"
     object:self
     userInfo:@{@"actionIdentifier": actionIdentifier, @"userInfo": userInfo}];
}

+ (BOOL)requiresMainQueueSetup
{
    return NO;
}

@end
