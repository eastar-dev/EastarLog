package android.log;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

public class LActivity extends FragmentActivity {
    @Override
    public void sendBroadcast(Intent intent) {
        Log.sendBroadcast(getClass(), intent);
        DialogInterface.OnCancelListener sss = null;
        Context context = null;
        new AlertDialog.Builder(context).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss).setOnCancelListener(sss);
        super.sendBroadcast(intent);
    }

    protected void onCreate(Bundle savedInstanceState) {
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
        if ((requestCode >> 16) == 0 && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            Log.startActivityForResult(getClass(), intent, requestCode);
        super.startActivityForResult(intent, requestCode);
    }

    @SuppressLint("RestrictedApi")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        if ((requestCode >> 16) == 0)
            Log.startActivityForResult(getClass(), intent, requestCode, options);
        else
            Log.po(Log.ERROR, "onActivityResult", "▶▶", getClass());
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
        if ((requestCode >> 16) == 0)
            Log.onActivityResult(getClass(), requestCode, resultCode, data);
        else
            Log.po(Log.ERROR, "onActivityResult", "◀◀", getClass());
        super.onActivityResult(requestCode, resultCode, data);
    }
}
