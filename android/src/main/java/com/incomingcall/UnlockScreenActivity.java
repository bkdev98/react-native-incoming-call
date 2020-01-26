package com.incomingcall;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

// import com.bumptech.glide.Glide;
// import com.bumptech.glide.request.RequestOptions;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class UnlockScreenActivity extends AppCompatActivity implements UnlockScreenActivityInterface {

    private static final String TAG = "MessagingService";
    private TextView tvBody;
    private TextView tvName;
    private ImageView ivAvatar;
    private String uuid = "";
    private String packageName = "";
    static boolean active = false;

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_call_incoming);

        tvBody = findViewById(R.id.tvBody);
        tvName = findViewById(R.id.tvName);
        ivAvatar = findViewById(R.id.ivAvatar);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("body")) {
                String body = bundle.getString("body");
                tvBody.setText(body);
            }
            if (bundle.containsKey("displayName")) {
                String displayName = bundle.getString("displayName");
                tvName.setText(displayName);
            }
            if (bundle.containsKey("packageName")) {
                String packageName = bundle.getString("packageName");
            }
            if (bundle.containsKey("avatar")) {
                String avatar = bundle.getString("avatar");
                // Glide.with(this)
                //         .load(avatar)
                //         .centerCrop()
                //         .placeholder(R.drawable.ic_avatar_default)
                //         .apply(RequestOptions.circleCropTransform())
                //         .into(ivAvatar);
            }
            if (bundle.containsKey("uuid")) {
                uuid = bundle.getString("uuid");
            }
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        AnimateImage acceptCallBtn = findViewById(R.id.ivAcceptCall);
        acceptCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    acceptDialing();
                } catch (Exception e) {
                    WritableMap params = Arguments.createMap();
                    params.putString("message", e.getMessage())
                    sendEvent("error", params);
                    dismissDialing();
                }
            }
        });

        AnimateImage rejectCallBtn = findViewById(R.id.ivDeclineCall);
        rejectCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissDialing();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Dont back
    }

    static final int STATIC_RESULT = 69;

    private void acceptDialing() {
        Intent i = IncomingCallModule.reactContext.getPackageManager().getLaunchIntentForPackage(packageName);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra("uuid", uuid);
        startActivityForResult(i, STATIC_RESULT);

        WritableMap params = Arguments.createMap();
        params.putBoolean("done", true);
        params.putString("uuid", uuid);
        sendEvent("answerCall", params);
        finish();
    }

    private void dismissDialing() {
        WritableMap params = Arguments.createMap();
        params.putBoolean("done", false);
        params.putString("uuid", uuid);

        sendEvent("endCall", params);
        finish();
    }

    @Override
    public void onConnected() {
        Log.d(TAG, "onConnected: ");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "onDisconnected: ");

    }

    @Override
    public void onConnectFailure() {
        Log.d(TAG, "onConnectFailure: ");

    }

    @Override
    public void onIncoming(ReadableMap params) {
        Log.d(TAG, "onIncoming: ");
    }

    private void sendEvent(String eventName, WritableMap params) {
        IncomingCallModule.reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }
}