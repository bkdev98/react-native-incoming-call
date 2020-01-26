package com.incomingcall;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

public class IncomingCallModule extends ReactContextBaseJavaModule {

    public static ReactApplicationContext reactContext;
    public static Activity mainActivity;

    public IncomingCallModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
        mainActivity = getCurrentActivity();
    }

    @Override
    public String getName() {
        return "IncomingCall";
    }

    @ReactMethod
    public void display(String uuid, String displayName, String body, String avatar, String packageName) {
        if (UnlockScreenActivity.active) {
            return;
        }
        if (reactContext != null) {
            Bundle bundle = new Bundle();
            bundle.putString("uuid", uuid);
            bundle.putString("displayName", displayName);
            bundle.putString("body", body);
            bundle.putString("avatar", avatar);
            bundle.putString("packageName", packageName);
            Intent i = new Intent(reactContext, UnlockScreenActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            i.putExtras(bundle);
            reactContext.startActivity(i);
        }
    }

    @ReactMethod
    public void dismiss() {
        final Activity activity = reactContext.getCurrentActivity();

        // if (MainActivity.active) {
        //     Intent i = new Intent(reactContext, MainActivity.class);
        //     i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //     reactContext.getApplicationContext().startActivity(i);
        // }
        assert activity != null;
    }
}
