package com.incomingcall;

import com.facebook.react.bridge.ReadableMap;

public interface UnlockScreenActivityInterface {
    static boolean active = false;
    public void onConnected();

    public void onDisconnected();

    public void onConnectFailure();

    public void onIncoming(ReadableMap params);
}
