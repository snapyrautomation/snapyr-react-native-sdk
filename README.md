# snapyr-react-native-sdk

A React Native bridge for the Snapyr native iOS and Android SDKs.

## Usage in React Native

### Install the SDK in your project:

```sh
npm install @snapyr/react-native-sdk
```

### Use the SDK in your project:

```js
import { 
    configure,
    identify,
    track,
    onSnapyrInAppMessage,
    trackInAppMessageImpression,
    trackInAppMessageClick,
    trackInAppMessageDismiss,
    checkIosPushAuthorization,
    requestIosPushAuthorization,
    onSnapyrNotificationReceived,
    onSnapyrNotificationResponse,
} from '@snapyr/react-native-sdk';

// ...

await configure('writeKey');
await identify('userId@here.com', { traits: 'optional' });
await track('someEvent', { some: 'properties', for: 'example' });

// --- In-App messaging section (optional) ---

// listen to in-app messages from Snapyr 
onSnapyrInAppMessage((inappMessage: SnapyrInAppMessage) => {
    const actionToken = inappMessage.actionToken;
    // example... stash this message in a useState so we can read its properties elsewhere
    setCurrentInAppMessage(inappMessage);
    if (inappMessage.content.payloadType == "html") {
        // example... use HTML template somewhere in your app
        setHtmlContent(inappMessage.content.payload);
    }
});

// track how users are interacting with your in-app message
// ... after we've shown a message to the user...
trackInAppMessageImpression(currentInAppMessage.actionToken);
// ... after we've determined the user interacted with our message...
trackInAppMessageClick(currentInAppMessage.actionToken, {exampleExtraProperty: "someId"});
// ... or, if the user dismissed our message, and we haven't recorded any other interaction...
trackInAppMessageDismiss(currentInAppMessage.actionToken);

// --- Push notifications section ---
// On Android, push permissions are enabled by default.
// On iOS, they need to be explicitly requested from the user. You can check the current push authorization status:
if (Platform.OS === 'ios') {
    const authStatus = await checkIosPushAuthorization();
    if (authStatus == SnapyrIosPushAuthStatus.authorized) {
        console.log("Push authorized!");
        pushAuthorized = true;
    } else {
        console.log("Push not authorized:", authStatus);
    }
}

// When appropriate, call this function to trigger a push permission request from the user:
if (Platform.OS === 'ios') {
    // The first time this is called, iOS displays a prompt, and returns true/false depending on the user's response.
    // On subsequent calls, iOS immediately returns true/false based on the stored value.
    const didAuthorize = await requestIosPushAuthorization();
    if (didAuthorize) {
        console.log("Push permissions granted!");
    } else {
        console.log("User rejected push permissions");
    }
}

// Register a callback to be triggered when a push notification is received on the device
onSnapyrNotificationReceived((notification: SnapyrPushNotificationPayload) => {
    console.log("Push notification received!", {
        'notificationId': notification.notificationId,
        'title': notification.titleText,
        'subtitle': notification.subtitleText,
        'content': notification.contentText,
        'imageUrl': notification.imageUrl,
        'deepLinkUrl': notification.deepLinkUrl,
    });
});

// Register a callback to be triggered when there is a response to a push notification, i.e. when the user taps a notification
onSnapyrNotificationResponse((notification: SnapyrPushNotificationResponsePayload) => {
    console.log("Push notification response received!", {
        'notificationId': notification.notificationId,
        'title': notification.titleText,
        'subtitle': notification.subtitleText,
        'content': notification.contentText,
        'imageUrl': notification.imageUrl,
        'deepLinkUrl': notification.deepLinkUrl,
    });
}, {
    // optional - setting this to true will cause the SDK to record any push response that may have occurred before the JS code finished initializing / before we registered our callback.
    // this is useful to be able to react to a push notification tap that launched the app, as the OS / native code will process the tap before React Native initialization completes
    fireQueuedPayloads: true
});
```

## Development

This repository includes example iOS and Android applications for testing changes to the React Native module itself. There are 2 simple steps to local development:

* `yarn` installs all dependencies and pods
* `yarn example ios` launches the React Native server in a separate process and also launches the iOS example app in the simulator
* `yarn example android` launches the React Native server in a separate process and also launches the Android example app in a configured simulator

Note: You must have your Android development environment configured prior to running the Yarn command.

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

_built from https://www.npmjs.com/package/create-react-native-module_

## License

MIT
