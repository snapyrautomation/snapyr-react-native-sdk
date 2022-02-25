import * as React from 'react';

import { StyleSheet, View, Text } from 'react-native';
import { configure, identify, track } from 'react-native-snapyr-sdk';

export default function App() {
  const result = 22;

  React.useEffect(() => {
    console.log('configuring sdk');
    (async function () {
      const key = await configure('mTGaCfFmNEskJ4Bqz36mcOGnRt6OSz9r', {
        enableDevMode: true,
        trackApplicationLifecycleEvents: true,
        recordScreenViews: true,
      });
      console.log('configured', key);
      await identify('react.native.app@withjam.com');
      console.log('identified react.native.app@withjam.com');
      await track('react-native-loaded', {
        rn_client_type: 'local',
        project_name: 'react-native-snapyr-sdk',
        dev_mode: true,
      });
      console.log('tracked react-native-loaded');
    })();
  }, []);

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
