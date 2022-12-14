#import "SnapyrRnNotifManager.h"

@interface SnapyrRnNotifManager ()
@property (nonatomic, assign) int listSize;
@property (nonatomic, strong) NSMutableArray<NSString *> *list;
@end

@implementation SnapyrRnNotifManager

- (instancetype)initWithSize:(int)size
{
    if (self = [self init]) {
        _listSize = size;
        _list = [NSMutableArray arrayWithCapacity:size];
    }
    return self;
}

- (BOOL)shouldNotifyForId:(NSString *)notificationId
{
    @synchronized (self) {
        if ([_list containsObject:notificationId]) {
            return NO;
        }
        if ([_list count] >= _listSize) {
            // pop oldest entry off the top
            [_list removeObjectAtIndex:0];
        }
        [_list addObject:notificationId];
        return YES;
    }
}

@end
