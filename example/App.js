import React, {useEffect, useState} from 'react';
import {
  View,
  Text,
  ToastAndroid,
  StyleSheet,
  TouchableHighlight,
  DeviceEventEmitter,
  Image,
  PermissionsAndroid,
  AppState,
} from 'react-native';
import RNBootSplash from 'react-native-bootsplash';
import messaging from '@react-native-firebase/messaging';
import Clipboard from '@react-native-community/clipboard';
import IncomingCall from 'react-native-incoming-call';
import RNCallKeep from 'react-native-callkeep';

const USE_CALLKEEP = false;

const PRIMARY_COLOR = USE_CALLKEEP ? '#1AD1EC' : '#8271FC';
const SECONDARY_COLOR = USE_CALLKEEP ? '#F55994' : '#FDDA97';
const BTN_LABEL_COLOR = USE_CALLKEEP ? '#FFFFFF' : '#21346E';

function setupCallKeep() {
  const options = {
    android: {
      alertTitle: 'Permissions Required',
      alertDescription:
        'This application needs to access your phone calling accounts to make calls',
      cancelButton: 'Cancel',
      okButton: 'ok',
      imageName: 'ic_launcher',
      additionalPermissions: [PermissionsAndroid.PERMISSIONS.READ_CONTACTS],
    },
  };

  try {
    RNCallKeep.setup(options);
    RNCallKeep.setAvailable(true); // Only used for Android, see doc above.
  } catch (err) {
    console.error('initializeCallKeep error:', err.message);
  }
}

USE_CALLKEEP && setupCallKeep();

export function handleRemoteMessage(remoteMessage, isHeadless) {
  if (remoteMessage?.notification?.title === 'Incoming call') {
    console.log('ready...');
    const callUUID = '23727631';
    if (USE_CALLKEEP) {
      setupCallKeep();
      RNCallKeep.displayIncomingCall(
        callUUID,
        'khanh@quocs.com',
        'Quoc Khanh',
        'email',
        true,
      );
      RNCallKeep.addEventListener('answerCall', ({callUUID: uuid}) => {
        RNCallKeep.setCurrentCallActive(uuid);
        if (isHeadless) {
          RNCallKeep.openAppFromHeadlessMode(uuid);
        } else {
          console.log(uuid);
          RNCallKeep.backToForeground();
        }
      });
    } else {
      IncomingCall.display(
        callUUID,
        'Quocs',
        'https://avatars3.githubusercontent.com/u/16166195',
        'Incomming Call'
      );
      DeviceEventEmitter.addListener('endCall', payload => {
        // End call action here
        console.log('endCall', payload);
      });
      DeviceEventEmitter.addListener('answerCall', payload => {
        // Start call action here
        console.log('answerCall', payload);
        if (payload.isHeadless) {
          IncomingCall.openAppFromHeadlessMode(payload.uuid);
        } else {
          IncomingCall.backToForeground();
        }
      });
    }
    // Could also persist data here for later uses
  } else if (remoteMessage?.notification?.title === 'Missed call') {
    console.log('dismiss goes here');
    IncomingCall.dismiss();
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

    AppState.addEventListener('change', state =>
      console.log('state change', state),
    );

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
    const launchPayload = await IncomingCall.getExtrasFromHeadlessMode();
    console.log('getExtrasFromHeadlessMode', launchPayload);
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

  async function handleCallKeep() {
    const extras = await RNCallKeep.getExtrasFromHeadlessMode();

    if (extras) {
      console.log('getExtrasFromHeadlessMode', extras);
    }

    const scs = await RNCallKeep.supportConnectionService();

    console.log('supportConnectionService: ', scs);

    RNCallKeep.addEventListener('answerCall', payload => {
      // Do your normal `Answering` actions here.
      setCallPayload(payload);
      console.log('answerCall', payload);
      RNCallKeep.backToForeground();
    });

    RNCallKeep.addEventListener('endCall', payload => {
      // Do your normal `Hang Up` actions here
      setCallPayload(payload);
      console.log('endCall', payload);
    });

    RNCallKeep.addEventListener('didDisplayIncomingCall', payload => {
      // you might want to do following things when receiving this event:
      // - Start playing ringback if it is an outgoing call
      console.log('didDisplayIncomingCall', payload);
    });
  }

  useEffect(() => {
    if (USE_CALLKEEP) {
      handleCallKeep();
    } else {
      handleIncomingCall();
    }
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
      <Text style={styles.appDesc}>
        example for{' '}
        {!USE_CALLKEEP ? 'react-native-incoming-call' : 'react-native-callkeep'}
      </Text>
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
            <Image style={styles.image} source={require('./images/waiting-call.jpg')} />
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
    backgroundColor: PRIMARY_COLOR,
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
    backgroundColor: SECONDARY_COLOR,
    justifyContent: 'center',
    alignItems: 'center',
    alignSelf: 'center',
    elevation: 5,
  },
  btnLabel: {
    fontSize: 14,
    color: BTN_LABEL_COLOR,
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
