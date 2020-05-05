package com.incomingcall;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.WindowManager;

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
    public void display(String uuid, String name, String avatar) {
        if (UnlockScreenActivity.active) {
            return;
        }
        if (reactContext != null) {
            Bundle bundle = new Bundle();
            bundle.putString("uuid", uuid);
            bundle.putString("name", name);
            bundle.putString("avatar", avatar);
            Intent i = new Intent(reactContext, UnlockScreenActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED +
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD +
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            
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

    @ReactMethod
    public void getLaunchParameters(final Promise promise) {
        final Activity activity = getCurrentActivity();
        final Intent intent = activity.getIntent();
        Bundle b = intent.getExtras();
        String value = "";
        if (b != null) {
            value = b.getString("param", "");            
        }
        promise.resolve(value);
    }

    @ReactMethod
    public void clearLaunchParameters() {
        final Activity activity = getCurrentActivity();
        final Intent intent = activity.getIntent();
        Bundle b = new Bundle();
        intent.putExtras(b);
    }
}
