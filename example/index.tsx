import { AppRegistry } from 'react-native';
import App from './src/App';
import { name as appName } from './app.json';
import PushNotificationIOS from "@react-native-community/push-notification-ios";
import PushNotification from "react-native-push-notification";
import { setPushNotificationToken } from 'snapyr-react-native-sdk';

PushNotification.configure({
    onRegister: function (token:{token: string }) {
      (async () => {
        try {
          console.log("indexToken", token)
          await setPushNotificationToken(token.token);
        } catch (ex) {
          console.log(ex);
        }
      })();
      console.log("TOKEN:", token.token);
    },
    onNotification: function (notification) {
      console.log("NOTIFICATION:", notification);
      notification.finish(PushNotificationIOS.FetchResult.NoData);
    },

    permissions: {
      alert: true,
      badge: true,
      sound: true,
    },

    popInitialNotification: true,
  
    requestPermissions: true,
  }); 



AppRegistry.registerComponent(appName, () => App);
