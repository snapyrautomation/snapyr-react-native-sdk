#import "SnapyrRnSdk.h"
#import <Snapyr/SnapyrSDK.h>
#import <Snapyr/SnapyrInAppMessage.h>
#import <Snapyr/SnapyrNotification.h>

static SnapyrNotification *__receivedNotification = nil;
static SnapyrNotification *__responseNotification = nil;

@implementation SnapyrRnSdk

RCT_EXPORT_MODULE_NO_LOAD(SnapyrRnSdk, SnapyrRnSdk)

+ (void)load
{
    // + load is executed very early on in app lifecycle (at startup, before main app instance is even launched).
    // Listen to notifications from Snapyr SDK that may be triggered before the RN SDK (or even the RN app itself) is initialized.
    // We will stash the last received and responded notification record so that we can "replay" them after RN initialization.
    // This allows for an app launch, triggered by a notification receipt or tap, to pass this data to the RN app
    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(didReceiveRemoteNotification:)
     name:@"snapyr.didReceiveNotification"
     object:nil];
    
    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(didReceiveNotificationResponse:)
     name:@"snapyr.didReceiveNotificationResponse"
     object:nil];
    
    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(registeredForRemoteNotificationsWithDeviceToken:)
     name:@"snapyr.registeredForRemoteNotificationsWithDeviceToken"
     object:nil];
}

// See https://reactnative.dev/docs/native-modules-ios

RCT_REMAP_METHOD(configure,
                 withKey:(nonnull NSString*)key
                 options:(NSDictionary*)_options
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
{
    if ([_options objectForKey:@"debug"]) {
        [SnapyrSDK debug:true];
    }
    SnapyrSDKConfiguration *configuration = [SnapyrSDKConfiguration configurationWithWriteKey:key];
    if ([_options objectForKey:@"trackApplicationLifecycleEvents"]) {
        configuration.trackApplicationLifecycleEvents = YES; // Enable this to record certain application events automatically
    }
    if ([_options objectForKey:@"recordScreenViews"]) {
        configuration.recordScreenViews = YES; // Enable this to record screen views automatically
    }
    if ([_options objectForKey:@"snapyrEnvironment"] != nil) {
        NSNumber *e = [_options valueForKey:@"snapyrEnvironment"];
        // NB this relies on integer-based enums with the same values between React and iOS
        configuration.snapyrEnvironment = (SnapyrEnvironment)e.integerValue; // Test against a Snapyr dev environment *internal only*
    }
    if ([_options objectForKey:@"flushQueueSize"] != nil) {
        NSNumber *e = [_options valueForKey:@"flushQueueSize"];
        configuration.flushAt = [e intValue]; // Enable this to record screen views automatically
    } else {
        // default - makes every event flush to network immediately
        configuration.flushAt = 1;
    }
    configuration.actionHandler = ^(SnapyrInAppMessage *message){
        [self handleSnapyrInAppMessage:message];
    };
    
    [SnapyrSDK setupWithConfiguration:configuration];
    // Adding first listener kicks off `startObserving` so we're wired into events/notifications, even if JS code hasn't yet requested anything
    [self addListener:@"snapyrDidRegister"];
    
    // Check for updated push token - if AppDelegate has the requisite code wired up, this will result in the current push token being sent to Snapyr for the currently-identified user
    [self requestPushTokenIfAuthorized];
    
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

RCT_EXPORT_METHOD(checkPushAuthorization:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
    [center getNotificationSettingsWithCompletionHandler:^(UNNotificationSettings *settings){
        
        if (settings.authorizationStatus == UNAuthorizationStatusAuthorized) {
            resolve(@"authorized");
        } else if (settings.authorizationStatus == UNAuthorizationStatusDenied) {
            resolve(@"denied");
        } else {
            resolve(@"undetermined");
        }
    }];
}

RCT_EXPORT_METHOD(requestPushAuthorization:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
    UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
//    [center auth]
    
    // NB: `requestAuthorizationWithOptions...` prompts user for permission and is needed to display alerts/badges/sounds, BUT...
    // `registerForRemoteNotifications` is what actually triggers APNs registration and the `didRegisterForRemoteNotificationsWithDeviceToken` callback.
    // `registerForRemoteNotifications` can be called BEFORE requesting authorization to get a push token (but only silent push will work until you get authorization).
    // might want to do this early in swizzling to get device token, then allow client code to request authorization later?
    [center requestAuthorizationWithOptions:(UNAuthorizationOptionSound | UNAuthorizationOptionAlert | UNAuthorizationOptionBadge) completionHandler:^(BOOL granted, NSError * _Nullable error) {
        if(!error) {
            // This triggers the AppDelegate's `didRegisterForRemoteNotificationsWithDeviceToken:` method if successful.
            // NB up to AppDelegate to actually implement this method and pass back to Snapyr!
            dispatch_async(dispatch_get_main_queue(), ^{
                [[UIApplication sharedApplication] registerForRemoteNotifications];
            });
            resolve([NSNumber numberWithBool:granted]);
        } else {
            NSLog(@"Push registration FAILED");
            reject(@"error", @"Encountered error during authorization attempt", error);
        }
    }];
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
- (void)startObserving {
    // Set up any upstream listeners or background tasks as necessary
    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(handleSnapyrDidRegister:)
     name:@"snapyrRN.didRegister"
     object:nil];
    
    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(handleSnapyrDidReceiveNotification:)
     name:@"snapyrRN.didReceiveNotification"
     object:nil];
    
    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(handleSnapyrDidReceiveNotificationResponse:)
     name:@"snapyrRN.didReceiveNotificationResponse"
     object:nil];
    
    if (__receivedNotification) {
        [self sendEventWithName:@"snapyrDidReceiveNotification" body:[__receivedNotification asDict]];
    }
    if (__responseNotification) {
        [self sendEventWithName:@"snapyrDidReceiveNotificationResponse" body:[__responseNotification asDict]];
    }
}

// Will be called when this module's last listener is removed, or on dealloc.
- (void)stopObserving {
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
    SnapyrNotification *snapyrNotification = notification.userInfo[@"snapyrNotification"];
    [self sendEventWithName:@"snapyrDidReceiveNotification" body:[snapyrNotification asDict]];
}

- (void)handleSnapyrDidReceiveNotificationResponse:(NSNotification *)notification
{
    SnapyrNotification *snapyrNotification = notification.userInfo[@"snapyrNotification"];
    [self sendEventWithName:@"snapyrDidReceiveNotificationResponse" body:[snapyrNotification asDict]];
}

// Helper - fire a `registerForRemoteNotifications` call IFF the app already has notification authorization
- (void)requestPushTokenIfAuthorized
{
    UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
    [center getNotificationSettingsWithCompletionHandler:^(UNNotificationSettings *settings){
        // Only try to get push token if user gave authorization (otherwise, we might "successfully" register the device with Snapyr as having a push token, with a token that can only be used for silent/background notifications
        if (settings.authorizationStatus == UNAuthorizationStatusAuthorized) {
            // This triggers the AppDelegate's `didRegisterForRemoteNotificationsWithDeviceToken:` method if successful.
            // NB up to AppDelegate to actually implement this method and pass back to Snapyr!
            dispatch_async(dispatch_get_main_queue(), ^{
                [[UIApplication sharedApplication] registerForRemoteNotifications];
            });
        }
  }];
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
     postNotificationName:@"snapyrRN.didRegister"
     object:self
     userInfo:@{@"token" : [hexString copy]}];
}

#pragma mark - static notification listeners/forwarders
// These methods listen for notifications from the Snapyr iOS SDK, and forward them (by sending a new notification) along to instance methods in the Snapyr RN SDK

+ (void)didReceiveRemoteNotification:(NSNotification *)notification
{
    SnapyrNotification *snapyrNotif = notification.userInfo[@"snapyrNotification"];
    // Record of this notification for later forwarding, if RN SDK is initialized after this notification
    __receivedNotification = snapyrNotif;

    [[NSNotificationCenter defaultCenter]
     postNotificationName:@"snapyrRN.didReceiveNotification"
     object:self
     userInfo:@{@"snapyrNotification": snapyrNotif}];
}

+ (void)didReceiveNotificationResponse:(NSNotification *)notification
API_AVAILABLE(ios(10.0))
{
    SnapyrNotification *snapyrNotif = notification.userInfo[@"snapyrNotification"];
    // Record of this notification for later forwarding, if RN SDK is initialized after this notification
    __responseNotification = snapyrNotif;
    
    NSString *actionIdentifier = notification.userInfo[@"actionIdentifier"];
    
    [[NSNotificationCenter defaultCenter]
     postNotificationName:@"snapyrRN.didReceiveNotificationResponse"
     object:self
     userInfo:@{@"actionIdentifier": actionIdentifier, @"snapyrNotification": snapyrNotif}];
}

+ (void)registeredForRemoteNotificationsWithDeviceToken:(NSNotification *)notification
{
    NSString *tokenString = notification.userInfo[@"tokenString"];

    [[NSNotificationCenter defaultCenter]
     postNotificationName:@"snapyrRN.registeredForRemoteNotificationsWithDeviceToken"
     object:self
     userInfo:@{@"tokenString": tokenString}];
}

+ (BOOL)requiresMainQueueSetup
{
    return NO;
}

@end
