# react-native-incoming-call

> React Native module to display incoming call activity, best result when using with firebase background messaging. Only for Android since iOS we have VoIP.

Yes I heard you could use **self managed ConnectionService** thing. But since I'm not an Android expert, this is a solution I found acceptable.

You could also wait for [this feature request](https://github.com/react-native-webrtc/react-native-callkeep/issues/43) from `react-native-callkeep` to be resolved and have an easier life.

## Getting started

`$ npm install react-native-incoming-call --save`

or

`$ yarn add react-native-incoming-call`

### Addition installation step

For RN >= 0.60, it's done. Otherwise:

`$ react-native link react-native-incoming-call`

## Usage
```javascript
import {useEffect} from 'react';
import IncomingCall from 'react-native-incoming-call';

// Display incoming call activity. Should be called in backgroundHandler function of react-native-firebase.
IncomingCall.display(payload);

// Dismiss current activity. Should be called when call ended.
IncomingCall.dismiss();

// Listen to cancel and answer call events
useEffect(() => {
  if (Platform.OS === "android") {
    // Handle end call action. Should call IncomingCall.dismiss() and other exit room actions.
    DeviceEventEmitter.addListener("endCall", payload => {});
    // Handle answer call action. Should navigate user to call screen.
    DeviceEventEmitter.addListener("answerCall", payload => {});
  }
}, []);
```
