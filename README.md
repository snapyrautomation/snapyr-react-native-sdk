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
    pushNotificationReceived,
    pushNotificationTapped,
    onSnapyrInAppMessage,
    trackInAppMessageImpression,
    trackInAppMessageClick,
    trackInAppMessageDismiss,
} from '@snapyr/react-native-sdk';

// ...

await configure('writeKey');
await identify('userId@here.com', { traits: 'optional' });
await track('someEvent', { some: 'properties', for: 'example' });

// after receiving a push notification payload, track received metric to Snapyr
const snapyrData = notification.data?.snapyr;
await pushNotificationReceived(snapyrData);

// after receiving a push interaction callback, track tapped metric to Snapyr
const snapyrData = notification.data?.snapyr;
await pushNotificationTapped(snapyrData);

// --- In-App messaging section (optional) ---

// listen to in-app messages from Snapyr 
onSnapyrInAppMessage((inappMessage: SnapyrInAppMessage) => {
    const actionToken = inappMessage.actionToken;
    // example... stash this message in a useState so we can read its properties elsewhere
    setCurrentInAppMessage(inappMessage);
    if (inappMessage.actionType == "custom" && inappMessage.content.payloadType == "html") {
        // example... use HTML template somewhere in your app
        setHtmlContent(inappMessage.content.payload);
    }
});

// track how users are interacting with your in-app message
// ... after we've shown a message to the user...
trackInAppMessageImpression(currentInAppMessage.actionToken);
// ... after we've determined the user interacted with our message...
trackInAppMessageClick(currentInAppMessage.actionToken, {exampleExtraProperty: "someId"});
// ... if the user dismissed our message without interacting with it...
trackInAppMessageDismiss(currentInAppMessage.actionToken);

// --- End of In-App messaging section ---
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
