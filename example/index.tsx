import { AppRegistry } from 'react-native';
import App from './src/App';
import { name as appName } from './app.json';
import PushNotificationIOS from '@react-native-community/push-notification-ios';
import PushNotification from 'react-native-push-notification';
import { setPushNotificationToken } from 'snapyr-react-native-sdk';

// https://github.com/zo0r/react-native-push-notification#usage
PushNotification.configure({
  permissions: {
    alert: true,
    badge: true,
    sound: true,
  },
  requestPermissions: true,
  popInitialNotification: true,
  onRegister: function (token: { token: string }) {
    (async () => {
      try {
        console.log('indexToken', token);
        await setPushNotificationToken(token.token);
      } catch (error) {
        console.log(error);
      }
    })();
    console.log('TOKEN:', token.token);
  },
  onNotification: function (notification) {
    console.log('NOTIFICATION:', notification);
    notification.finish(PushNotificationIOS.FetchResult.NoData);
  },
});

AppRegistry.registerComponent(appName, () => App);
