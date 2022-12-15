import { NativeEventEmitter, NativeModules, Platform } from 'react-native';

const SNAPYR_LISTENER_NOTIFICATION = 'snapyrDidReceiveNotification';
const SNAPYR_LISTENER_NOTIFICATION_RESPONSE =
  'snapyrDidReceiveNotificationResponse';
const SNAPYR_LISTENER_INAPP_MESSAGE = 'snapyrInAppMessage';

const LINKING_ERROR =
  `The package 'snapyr-react-native-sdk' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const SnapyrRnSdk = NativeModules.SnapyrRnSdk
  ? NativeModules.SnapyrRnSdk
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export enum SnapyrEnvironment {
  SnapyrEnvironmentDefault,
  SnapyrEnvironmentStage,
  SnapyrEnvironmentDev,
}

export enum SnapyrIosPushAuthStatus {
  authorized = 'authorized',
  denied = 'denied',
  undetermined = 'undetermined',
}

export type SnapyrConfigOptions = {
  trackApplicationLifecycleEvents: boolean;
  recordScreenViews: boolean;
  flushQueueSize: number;
  debug: boolean;
  snapyrEnvironment: SnapyrEnvironment;
};

export type SnapyrEventCallbackOptions = {
  /**
   * If set to true, at the time that you register a callback, Snapyr will check for an event that arrived before registration. If one found, your callback will immediately be triggered once with the stored event, then continue listening.
   *
   * This can be useful for responding to events that occur early in the application lifecycle, such as a push notification response that launched the app.
   *
   * Snapyr only stores one event of each type. If multiple events have come in before you register a callback, only the latest event will fire.
   * @default false
   */
  fireQueuedPayloads?: boolean;
};

export enum SnapyrInAppActionType {
  Custom = 'custom',
  Overlay = 'overlay',
}

export enum SnapyrInAppPayloadType {
  JSON = 'json',
  HTML = 'html',
}

export declare type SnapyrInAppContent =
  | { payloadType: SnapyrInAppPayloadType.JSON; payload: Record<string, any> }
  | { payloadType: SnapyrInAppPayloadType.HTML; payload: string };

export declare type SnapyrInAppMessage = {
  timestamp: string;
  actionType: SnapyrInAppActionType;
  userId: string;
  actionToken: string;
  content: SnapyrInAppContent;
};

export declare type SnapyrPushNotificationPayload = {
  notificationId: number;
  titleText: string;
  contentText: string;
  subtitleText: string | null;
  deepLinkUrl: string | null;
  imageUrl: string | null;
};

// Same data for now but will likely change, e.g. to include action button ID or cancellation data, so establishing separate type
export declare type SnapyrPushNotificationResponsePayload =
  SnapyrPushNotificationPayload;

type SnapyrEventCallback<T> = (eventPayload: T) => void;

export const SnapyrEmitter = new NativeEventEmitter(SnapyrRnSdk);
// Client code can register listeners (callbacks) on SDK events; use a map to limit to 1 listener per event type
const _eventCallbacks = new Map<string, SnapyrEventCallback<any>>();
// If we receive an event before client has registered a callback for it, store the payload so it can optionally be fired once a callback is registered. Only stores one payload (the last one to be received) for each event at this time.
const _pendingEvents = new Map<string, any>();

/**
 * Generate an event listener registration callback for the given (native) event name, and start listening for native events.
 *
 * This will catch and store any event fired from the native side even if no user callback has been registered yet, allowing for a "replay" of the latest event at callback registration time.
 */
function setupEventListener<T>(
  eventName: string
): (
  callback: SnapyrEventCallback<T>,
  options?: SnapyrEventCallbackOptions
) => void {
  // NB register the NativeEventEmitter listener immediately so we catch any events fired from native, even if client callbacks have not yet been registered
  SnapyrEmitter.addListener(eventName, (event: T) => {
    const existingCallback = _eventCallbacks.get(eventName);
    if (existingCallback !== undefined) {
      existingCallback(event);
    } else {
      // Received event from native before any client callbacks registered - store it for potential later use
      _pendingEvents.set(eventName, event);
    }
  });

  return (
    callback: SnapyrEventCallback<T>,
    options?: SnapyrEventCallbackOptions
  ): void => {
    _eventCallbacks.set(eventName, callback);
    // If there's a stored event from before this registration, fire it now (if configured to do so), and clear it out
    if (_pendingEvents.has(eventName)) {
      if (options?.fireQueuedPayloads === true) {
        callback(_pendingEvents.get(eventName));
      }
      _pendingEvents.delete(eventName);
    }
  };
}

// Not currently supported... @todo finish implementation or remove
// const SNAPYR_LISTENER_REGISTER = 'snapyrDidRegister';
// const onSnapyrDidRegister = setupEventListener<string>(
//   SNAPYR_LISTENER_REGISTER
// );

/**
 * Registers a callback that will be called when Snapyr receives an in-app message.
 *
 * Only one callback can be registered at a time. Calling this function for a second time will replace the first callback with the second one.
 */
export const onSnapyrInAppMessage = setupEventListener<SnapyrInAppMessage>(
  SNAPYR_LISTENER_INAPP_MESSAGE
);

/**
 * Registers a callback that will be called when Snapyr receives a notification response, i.e. a tap on a notification.
 *
 * Only one callback can be registered at a time. Calling this function for a second time will replace the first callback with the second one.
 */
export const onSnapyrNotificationResponse =
  setupEventListener<SnapyrPushNotificationResponsePayload>(
    SNAPYR_LISTENER_NOTIFICATION_RESPONSE
  );

/**
 * Registers a callback that will be called when Snapyr receives a notification.
 *
 * Only one callback can be registered at a time. Calling this function for a second time will replace the first callback with the second one.
 */
export const onSnapyrNotificationReceived =
  setupEventListener<SnapyrPushNotificationPayload>(
    SNAPYR_LISTENER_NOTIFICATION
  );

/**
 * iOS only - trigger OS prompt to request permission from the user to send push notifications.
 * The prompt will display only if the user has not already accepted or denied such a prompt in the past;
 * otherwise, this method will immediately return with the push authorization status.
 * Snapyr push notifications will only work after the user has granted push authorization for this app.
 */
export function requestIosPushAuthorization(): Promise<boolean> {
  if (Platform.OS !== 'ios') {
    throw new Error(
      "Function valid only for iOS. Try wrapping call in `if (Platform.OS === 'ios') {...}"
    );
  }
  // promise resolved with boolean for whether user allowed; or rejected if there was an error during authorization attempt
  return SnapyrRnSdk.requestPushAuthorization();
}

/**
 * iOS only - get the current push authorization status. Push notifications are authorized/enabled only if the result is `SnapyrIosPushAuthStatus.authorized`.
 */
export function checkIosPushAuthorization(): Promise<SnapyrIosPushAuthStatus> {
  if (Platform.OS !== 'ios') {
    throw new Error(
      "Function valid only for iOS. Try wrapping call in `if (Platform.OS === 'ios') {...}"
    );
  }
  return SnapyrRnSdk.checkPushAuthorization();
}

export function configure(
  key: string,
  options?: Partial<SnapyrConfigOptions>
): Promise<string> {
  return SnapyrRnSdk.configure(key, options);
}

export function identify(id: string, traits?: any): Promise<string> {
  return SnapyrRnSdk.identify(id, traits);
}

export function track(eventName: string, properties?: any): Promise<string> {
  return SnapyrRnSdk.track(eventName, properties);
}

export function setPushNotificationToken(token: string) {
  return SnapyrRnSdk.setPushNotificationToken(token);
}

export function pushNotificationReceived(snapyrData: any): Promise<void> {
  return SnapyrRnSdk.pushNotificationReceived(snapyrData);
}

export function pushNotificationTapped(
  snapyrData: any,
  actionId?: string
): Promise<void> {
  return SnapyrRnSdk.pushNotificationTapped(snapyrData, actionId);
}

export function trackInAppMessageImpression(
  actionToken: string
): Promise<void> {
  return SnapyrRnSdk.trackInAppMessageImpression(actionToken);
}

export function trackInAppMessageClick(
  actionToken: string,
  properties: Record<string, any> = {}
): Promise<void> {
  return SnapyrRnSdk.trackInAppMessageClick(actionToken, properties);
}

export function trackInAppMessageDismiss(actionToken: string): Promise<void> {
  return SnapyrRnSdk.trackInAppMessageDismiss(actionToken);
}

export function reset() {
  return SnapyrRnSdk.reset();
}
