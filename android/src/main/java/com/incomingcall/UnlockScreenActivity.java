package com.incomingcall;

import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.net.Uri;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.AudioAttributes;
import android.provider.Settings;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import com.squareup.picasso.Picasso;

public class UnlockScreenActivity extends AppCompatActivity implements UnlockScreenActivityInterface {

    private static final String TAG = "MessagingService";
    private TextView tvName;
    private TextView tvInfo;
    private ImageView ivAvatar;
    private Integer timeout = 0;
    private String uuid = "";
    static boolean active = false;
    private static Vibrator v = (Vibrator) IncomingCallModule.reactContext.getSystemService(Context.VIBRATOR_SERVICE);
    private long[] pattern = {0, 1000, 800};
    private static MediaPlayer player = MediaPlayer.create(IncomingCallModule.reactContext, Settings.System.DEFAULT_RINGTONE_URI);
    private static Activity fa;
    private Timer timer;


    @Override
    public void onStart() {
        super.onStart();
        if (this.timeout > 0) {
              this.timer = new Timer();
              this.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // this code will be executed after timeout seconds
                    dismissIncoming();
                }
            }, timeout);
        }
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

        fa = this;

        setContentView(R.layout.activity_call_incoming);

        tvName = findViewById(R.id.tvName);
        tvInfo = findViewById(R.id.tvInfo);
        ivAvatar = findViewById(R.id.ivAvatar);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("uuid")) {
                uuid = bundle.getString("uuid");
            }
            if (bundle.containsKey("name")) {
                String name = bundle.getString("name");
                tvName.setText(name);
            }
            if (bundle.containsKey("info")) {
                String info = bundle.getString("info");
                tvInfo.setText(info);
            }
            if (bundle.containsKey("avatar")) {
                String avatar = bundle.getString("avatar");
                if (avatar != null) {
                    Picasso.get().load(avatar).transform(new CircleTransform()).into(ivAvatar);
                }
            }
            if (bundle.containsKey("timeout")) {
                this.timeout = bundle.getInt("timeout");
            }
            else this.timeout = 0;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build();
            VibrationEffect ve = VibrationEffect.createWaveform(
                pattern,
                0
            );
            v.vibrate(ve, audioAttributes);    
        } else {
            v.vibrate(pattern, 0);
        }
        player.start();

        AnimateImage acceptCallBtn = findViewById(R.id.ivAcceptCall);
        acceptCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    v.cancel();
                    player.stop();
                    player.prepareAsync();
                    acceptDialing();
                } catch (Exception e) {
                    WritableMap params = Arguments.createMap();
                    params.putString("message", e.getMessage());
                    sendEvent("error", params);
                    dismissDialing();
                }
            }
        });

        AnimateImage rejectCallBtn = findViewById(R.id.ivDeclineCall);
        rejectCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.cancel();
                player.stop();
                player.prepareAsync();
                dismissDialing();
            }
        });

    }

    @Override
    public void onBackPressed() {
        // Dont back
    }

    public void dismissIncoming() {
        v.cancel();
        player.stop();
        player.prepareAsync();
        dismissDialing();
    }

    private void acceptDialing() {
        WritableMap params = Arguments.createMap();
        params.putBoolean("accept", true);
        params.putString("uuid", uuid);
        if (timer != null){
          timer.cancel();
        }
        if (!IncomingCallModule.reactContext.hasCurrentActivity()) {
            params.putBoolean("isHeadless", true);
        }
        KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        if (mKeyguardManager.isDeviceLocked()) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mKeyguardManager.requestDismissKeyguard(this, new KeyguardManager.KeyguardDismissCallback() {
              @Override
              public void onDismissSucceeded() {
                super.onDismissSucceeded();
              }
            });
          }
        }

        sendEvent("answerCall", params);
        finish();
    }

    private void dismissDialing() {
        WritableMap params = Arguments.createMap();
        params.putBoolean("accept", false);
        params.putString("uuid", uuid);
        if (timer != null) {
          timer.cancel();
        }
        if (!IncomingCallModule.reactContext.hasCurrentActivity()) {
            params.putBoolean("isHeadless", true);
        }

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
