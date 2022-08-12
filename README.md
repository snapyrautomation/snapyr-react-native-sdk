# snapyr-react-native-sdk

A React Native bridge for the Snapyr native iOS and Android SDKs.

## Usage in React Native

### Install the SDK in your project:

```sh
npm install @snapyr/react-native-sdk
```

### Use the SDK in your project:

```js
import { configure, identify, track } from '@snapyr/react-native-sdk';

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
