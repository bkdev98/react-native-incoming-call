import React, {useEffect, useState} from 'react';
import {
  View,
  Text,
  ToastAndroid,
  StyleSheet,
  TouchableHighlight,
  DeviceEventEmitter,
  Image,
} from 'react-native';
import RNBootSplash from 'react-native-bootsplash';
import messaging from '@react-native-firebase/messaging';
import Clipboard from '@react-native-community/clipboard';
import IncomingCall from 'react-native-incoming-call';

export function handleRemoteMessage(remoteMessage) {
  if (remoteMessage?.notification?.title === 'Incoming call') {
    IncomingCall.display(
      '123',
      'Quocs',
      'https://avatars3.githubusercontent.com/u/16166195',
    );
    // Could also persist data here for later uses
  }
}

const App = () => {
  const [deviceToken, setDeviceToken] = useState(null);
  const [callPayload, setCallPayload] = useState(null);

  useEffect(() => {
    RNBootSplash.hide({duration: 250});

    // Get the device token
    messaging()
      .getToken()
      .then(token => {
        return setDeviceToken(token);
      });

    // Listen to whether the token changes
    return messaging().onTokenRefresh(token => {
      setDeviceToken(token);
    });
  }, []);

  useEffect(() => {
    const unsubscribe = messaging().onMessage(async remoteMessage => {
      handleRemoteMessage(remoteMessage);
    });

    return unsubscribe;
  }, []);

  async function handleIncomingCall() {
    const launchPayload = await IncomingCall.getLaunchParameters();
    console.log('launchParameters', launchPayload);
    IncomingCall.clearLaunchParameters();
    if (launchPayload) {
      // Start call here
      setCallPayload(launchPayload);
    }

    DeviceEventEmitter.addListener('endCall', payload => {
      // End call action here
      setCallPayload(payload);
    });
    DeviceEventEmitter.addListener('answerCall', payload => {
      // Start call action here
      setCallPayload(payload);
    });
  }

  useEffect(() => {
    handleIncomingCall();
  }, []);

  function handleCopyToken() {
    Clipboard.setString(deviceToken);
    ToastAndroid.show('Device token copied!', 2000);
  }

  function handleClearPayload() {
    setCallPayload(null);
  }

  return (
    <View style={styles.wrapper}>
      <Text style={styles.appTitle}>RNCall</Text>
      <Text style={styles.appDesc}>example for react-native-incoming-call</Text>
      <View style={styles.container}>
        {callPayload ? (
          <>
            <Image
              style={styles.image}
              source={require('./images/incoming-call.jpg')}
            />
            <Text style={styles.header}>Incoming Call Payload</Text>
            <Text style={styles.text}>{JSON.stringify(callPayload)}</Text>
            <TouchableHighlight
              onPress={handleClearPayload}
              style={styles.button}
              underlayColor="#C0B7FD">
              <Text style={styles.btnLabel}>Clear</Text>
            </TouchableHighlight>
          </>
        ) : deviceToken ? (
          <>
            <Image
              style={styles.image}
              source={require('./images/waiting-call.jpg')}
            />
            <Text style={styles.header}>FCM Device Token</Text>
            <Text style={styles.text}>{deviceToken}</Text>
            <TouchableHighlight
              onPress={handleCopyToken}
              style={styles.button}
              underlayColor="#C0B7FD">
              <Text style={styles.btnLabel}>Copy To Clipboard</Text>
            </TouchableHighlight>
          </>
        ) : (
          <>
            <Image
              style={styles.image}
              source={require('./images/no-device-token.jpg')}
            />
            <Text style={styles.text}>No Device Token Found!</Text>
          </>
        )}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  wrapper: {
    flex: 1,
    backgroundColor: '#8271FC',
  },
  appTitle: {
    textAlign: 'center',
    marginTop: 40,
    fontSize: 24,
    color: '#FFFFFF',
    fontFamily: 'monospace',
    fontWeight: '700',
  },
  appDesc: {
    textAlign: 'center',
    marginTop: 20,
    fontSize: 10,
    marginHorizontal: 20,
    color: '#FFFFFF',
    fontFamily: 'monospace',
    textTransform: 'uppercase',
    letterSpacing: 2,
  },
  container: {
    marginTop: 40,
    paddingTop: 20,
    paddingHorizontal: 20,
    paddingBottom: 50,
    marginHorizontal: 20,
    borderRadius: 10,
    backgroundColor: '#FFFFFF',
    elevation: 5,
  },
  header: {
    fontSize: 20,
    color: '#21346E',
    marginBottom: 10,
    fontFamily: 'monospace',
    fontWeight: '700',
  },
  text: {
    fontSize: 14,
    color: '#7984A7',
    fontFamily: 'monospace',
  },
  button: {
    position: 'absolute',
    height: 50,
    borderRadius: 10,
    paddingHorizontal: 20,
    bottom: -25,
    backgroundColor: '#FDDA97',
    justifyContent: 'center',
    alignItems: 'center',
    alignSelf: 'center',
    elevation: 5,
  },
  btnLabel: {
    fontSize: 14,
    color: '#21346E',
    fontFamily: 'monospace',
    fontWeight: '700',
  },
  image: {
    alignSelf: 'center',
    width: 300,
    height: 300,
  },
});

export default App;
