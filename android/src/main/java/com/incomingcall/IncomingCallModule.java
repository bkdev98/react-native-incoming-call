package com.incomingcall;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.WindowManager;
import android.content.Context;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;

public class IncomingCallModule extends ReactContextBaseJavaModule {

    public static ReactApplicationContext reactContext;
    public static Activity mainActivity;

    private static final String TAG = "RNIC:IncomingCallModule";

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
    public void display(String uuid, String name, String avatar, String info) {
        if (UnlockScreenActivity.active) {
            return;
        }
        if (reactContext != null) {
            Bundle bundle = new Bundle();
            bundle.putString("uuid", uuid);
            bundle.putString("name", name);
            bundle.putString("avatar", avatar);
            bundle.putString("info", info);
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

    private Context getAppContext() {
        return this.reactContext.getApplicationContext();
    }

    @ReactMethod
    public void backToForeground() {
        Context context = getAppContext();
        String packageName = context.getApplicationContext().getPackageName();
        Intent focusIntent = context.getPackageManager().getLaunchIntentForPackage(packageName).cloneFilter();
        Activity activity = getCurrentActivity();
        boolean isOpened = activity != null;
        Log.d(TAG, "backToForeground, app isOpened ?" + (isOpened ? "true" : "false"));

        if (isOpened) {
            focusIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(focusIntent);
        }
    }

    @ReactMethod
    public void getLaunchParameters(final Promise promise) {
        final Activity activity = getCurrentActivity();
        final Intent intent = activity.getIntent();
        Bundle b = intent.getExtras();
        String value = "";
        if (b != null) {
            value = b.getString("uuid", "");            
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
