import { NativeModules, Platform } from 'react-native';

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

export function configure(key: string, options?: any): Promise<string> {
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

export function pushNotificationTapped(snapyrData: any, actionId?: string): Promise<void> {
  return SnapyrRnSdk.pushNotificationTapped(snapyrData, actionId);
}

export function reset() {
  return SnapyrRnSdk.reset();
}