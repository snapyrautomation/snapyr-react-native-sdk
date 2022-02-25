# react-native-snapyr-sdk

A React Native bridge for the Snapyr native ios and android SDKs.  This is intended to be published to npm so that it can be added to React Native applications.

_built from https://www.npmjs.com/package/create-react-native-module_

## Development

This repository includes example ios and android applications for testing changes to the React Native module itself.  There are 2 simple steps to local development:

* `yarn` will install all latest dependencies and install pods
* `yarn example ios` launches the React Native server in a separate process and also launches the iOS example app in the simulator
* `yarn example android` launches the React Native server in a separate process and also launches the android example app in a configured simulator

Note: You must have your android development environment configured prior to running the yarn command.

## Usage in React Native

```sh
npm install react-native-snapyr-sdk
```

## Usage

```js
import { configure, identify, track } from "react-native-snapyr-sdk";

// ...

await configure('writeKey');
await identify('userId@here.com', { traits: 'optional' });
await track('someEvent',{ some: 'properties', for: 'example' });
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
