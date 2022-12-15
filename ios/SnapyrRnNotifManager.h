#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/**
 * Simple queue structure to dedupe notifications by ID. Keeps track of the last N notification IDs that were checked. Checking `shouldNotifyForId`returns false if the ID is already on the list; otherwise adds it to the list and returns true.
 * If the queue reaches the max size, the first (oldest) ID is popped to make room for the new (latest) ID.
 * There are multiple AppDelegate calls that may process the same notification, so this ensures any duplicate triggers on the same notification only result in one callback being triggered over in JS code.
 */
@interface SnapyrRnNotifManager : NSObject
- (instancetype)initWithSize:(int)size;
- (BOOL)shouldNotifyForId:(NSString *)notificationId;
@end

NS_ASSUME_NONNULL_END
