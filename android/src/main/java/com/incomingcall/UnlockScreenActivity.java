package com.incomingcall;

import android.app.KeyguardManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.os.Vibrator;
import android.content.Context;

import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
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
    private static Vibrator vibrator;
    private static Ringtone ringtone;
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

        startRinging();


        AnimateImage acceptCallBtn = findViewById(R.id.ivAcceptCall);
        acceptCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    stopRinging();
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
                stopRinging();
                dismissDialing();
            }
        });

    }

    @Override
    public void onBackPressed() {
        // Dont back
    }

    public void dismissIncoming() {
        stopRinging();
        dismissDialing();
    }

    private void startRinging() {
      long[] pattern = {0, 1000, 800};
      vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
      int ringerMode = ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).getRingerMode();
      if(ringerMode == AudioManager.RINGER_MODE_SILENT) return;

      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        VibrationEffect vibe = VibrationEffect.createWaveform(pattern, 2);
        vibrator.vibrate(vibe);
      }else{
        vibrator.vibrate(pattern, 0);
      }
      if(ringerMode == AudioManager.RINGER_MODE_VIBRATE) return;

      ringtone = RingtoneManager.getRingtone(this, RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE));
      ringtone.play();
    }

    private void stopRinging() {
      if (vibrator != null){
        vibrator.cancel();
      }
      int ringerMode = ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).getRingerMode();
      if(ringerMode == AudioManager.RINGER_MODE_SILENT) return;
      ringtone.stop();
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
