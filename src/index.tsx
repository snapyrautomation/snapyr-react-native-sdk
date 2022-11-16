import {
  EmitterSubscription,
  NativeEventEmitter,
  NativeModules,
  Platform,
} from 'react-native';

const SNAPYR_LISTENER_REGISTER = 'snapyrDidRegister';
const SNAPYR_LISTENER_NOTIFICATION = 'snapyrDidReceiveNotification';
const SNAPYR_LISTENER_NOTIFICATION_RESPONSE =
  'snapyrDidReceiveNotificationResponse';
const SNAPYR_LISTENER_INAPP_MESSAGE = 'snapyrInAppMessage';
const SNAPYR_LISTENER_DEEPLINK = 'deepLinkUrlReceived';
// const SNAPYR_LISTENER_NOTIFICATION = 'notificationReceived';

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

export type SnapyrConfigOptions = {
  trackApplicationLifecycleEvents: boolean;
  recordScreenViews: boolean;
  snapyrEnvironment: SnapyrEnvironment;
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

export const SnapyrEmitter = new NativeEventEmitter(SnapyrRnSdk);
// Client code can register listeners (callbacks) on SDK events; use a map to limit to 1 listener per event type
const _eventListeners = new Map<string, EmitterSubscription>();

export function onSnapyrDidRegister(callback: (token: string) => void): void {
  const listener = SnapyrEmitter.addListener(
    SNAPYR_LISTENER_REGISTER,
    (token) => callback(token)
  );
  // Remove/unsubscribe previous listener, if any
  _eventListeners.get(SNAPYR_LISTENER_REGISTER)?.remove();
  _eventListeners.set(SNAPYR_LISTENER_REGISTER, listener);
}

export function onSnapyrDidReceiveNotification(
  callback: (notification: any) => void
): void {
  const listener = SnapyrEmitter.addListener(
    SNAPYR_LISTENER_NOTIFICATION,
    (notification) => {
      console.error("RN::: RECEIVED NOTIFICATION!!!", notification);
      callback(notification);
    }
  );
  // Remove/unsubscribe previous listener, if any
  _eventListeners.get(SNAPYR_LISTENER_NOTIFICATION)?.remove();
  _eventListeners.set(SNAPYR_LISTENER_NOTIFICATION, listener);
}

export function onSnapyrInAppMessage(
  callback: (message: SnapyrInAppMessage) => void
): void {
  const listener = SnapyrEmitter.addListener(
    SNAPYR_LISTENER_INAPP_MESSAGE,
    (message: SnapyrInAppMessage) => {
      callback(message);
    }
  );
  // Remove/unsubscribe previous listener, if any
  _eventListeners.get(SNAPYR_LISTENER_INAPP_MESSAGE)?.remove();
  _eventListeners.set(SNAPYR_LISTENER_INAPP_MESSAGE, listener);
}

export function onSnapyrDeeplinkReceived(
  callback: (message: SnapyrInAppMessage) => void
): void {
  const listener = SnapyrEmitter.addListener(
    SNAPYR_LISTENER_DEEPLINK,
    (message: SnapyrInAppMessage) => {
      console.error("RN::: RECEIVED DEEPLINK CALLBACK!!!", message);
      callback(message);
    }
  );
  // Remove/unsubscribe previous listener, if any
  _eventListeners.get(SNAPYR_LISTENER_DEEPLINK)?.remove();
  _eventListeners.set(SNAPYR_LISTENER_DEEPLINK, listener);
}

export function onSnapyrDidReceiveNotificationResponse(
  callback: ({
    actionIdentifier,
    userInfo,
  }: {
    actionIdentifier: string;
    userInfo: Record<string, any>;
  }) => void
): void {
  const listener = SnapyrEmitter.addListener(
    SNAPYR_LISTENER_NOTIFICATION_RESPONSE,
    (responseData) => {
      callback(responseData);
    }
  );
  // Remove/unsubscribe previous listener, if any
  _eventListeners.get(SNAPYR_LISTENER_NOTIFICATION_RESPONSE)?.remove();
  _eventListeners.set(SNAPYR_LISTENER_NOTIFICATION_RESPONSE, listener);
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
