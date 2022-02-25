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

@end
