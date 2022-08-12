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

RCT_EXPORT_METHOD(reset)
{
    NSLog(@"reset");
    // Do nothing, for now... stub method to maintain compat w/ Android
}

@end
