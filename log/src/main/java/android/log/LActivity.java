package android.log;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.view.WindowManager;

public class LActivity extends android.support.v7.app.AppCompatActivity {
    PowerManager.WakeLock wakeLock;

    private void acquireWakeLock(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, context.getClass().getName());
        if (wakeLock != null) {
            wakeLock.acquire(5000);
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    @Override
    public void sendBroadcast(Intent intent) {
        Log.sendBroadcast(getClass(), intent);
        super.sendBroadcast(intent);
    }

    protected void onCreate(Bundle savedInstanceState) {
        if (Log.LOG) {
            acquireWakeLock(this);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        Log.onCreate(getClass());
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        Log.onDestroy(getClass());
        super.onDestroy();

    }

    @Override
    protected void onStart() {
        Log.onStart(getClass());
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.onStop(getClass());
        super.onStop();
    }

    @Override
    protected void onRestart() {
        Log.onRestart(getClass());
        super.onRestart();
    }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            Log.startActivityForResult(getClass(), intent, requestCode);
        super.startActivityForResult(intent, requestCode);
    }

    @SuppressLint("RestrictedApi")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        Log.startActivityForResult(getClass(), intent, requestCode, options);
        super.startActivityForResult(intent, requestCode, options);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void startActivities(Intent[] intents) {
        Log.startActivities(getClass(), intents);
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            try {
                intents[1].setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent.getActivities(this, 0, intents, PendingIntent.FLAG_ONE_SHOT).send();
            } catch (CanceledException e) {
                e.printStackTrace();
            }
        } else
            super.startActivities(intents);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.onActivityResult(getClass(), requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
