import {AppRegistry} from 'react-native';
import messaging from '@react-native-firebase/messaging';

import App, {handleRemoteMessage} from './App';
import {name as appName} from './app.json';

messaging().setBackgroundMessageHandler(async remoteMessage => {
  handleRemoteMessage(remoteMessage, true);
});

AppRegistry.registerComponent(appName, () => App);
