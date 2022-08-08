import * as React from 'react';

import { StyleSheet, View, Text } from 'react-native';
import { configure, identify, track } from 'snapyr-react-native-sdk';

export default function App() {
  const result = 224;

  React.useEffect(() => {
    console.log('configuring sdk');
    (async function () {
      const key = await configure('<write-key-here>', {
        trackApplicationLifecycleEvents: true,
        recordScreenViews: true,
      });
      console.log('configured', key);
      await identify('test@example.com');
      console.log('identified test@example.com');
      await track('react-native-loaded', {
        rn_client_type: 'local',
        project_name: 'snapyr-react-native-sdk',
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
