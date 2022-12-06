import * as React from 'react';

import { StyleSheet, View, Text, Button } from 'react-native';
import { configure, identify, track } from 'snapyr-react-native-sdk';

export default function App() {
  const result = 224;

  React.useEffect(() => {
    console.log('configuring sdk');
    (async function () {
      const APIKey = 'USY3SOyIaG4hZz7EUVIUj0sriqu5Zgna';
      const key = await configure(APIKey, {
        trackApplicationLifecycleEvents: true,
        recordScreenViews: true,
      });
      console.log('configured', key);

      await identify('testId', {
        name: 'Testing Name',
        email: 'test@example.com'
      });

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
      <Button
        title='Send event'
        onPress={() => {
          track('button-pressed');
          console.log('tracked react-native-loaded');
        }}/>
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
