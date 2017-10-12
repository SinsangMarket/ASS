package com.wjdtncjs.ass.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.wjdtncjs.ass.MyAidl;

import java.util.Timer;
import java.util.TimerTask;


public class ASS extends Service implements ScreenShotListener {
    private final String TAG = this.getClass().getSimpleName();
    private Context mContext;
    private ScreenShotContentObserver mContentObserver;
    private Timer mActivityTransitionTimer;
    private TimerTask mActivityTransitionTimerTask;
    private final long MAX_ACTIVITY_TRANSITION_TIME_MS = 3000;

    private boolean screenshotEnable = true;

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        return binder;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        super.onCreate();
        mContext = getApplicationContext();

        HandlerThread handlerThread = new HandlerThread("content_observer");
        handlerThread.start();
        final Handler handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };
        mContentObserver = new ScreenShotContentObserver(handler, getContentResolver(), this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        binder = null;
        Log.e(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onScreenshotTaken(Bitmap bitmap, String fileName) {
        try {
            Log.e(TAG, "onScreenshotTaken");
            if (screenshotEnable) {
                ScreenShotContentObserver.resultProcess(mContext, bitmap, fileName);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
    }


    MyAidl.Stub binder = new MyAidl.Stub() {
        @Override
        public void unregisterScreenShotObserver() throws RemoteException {
            mActivityTransitionTimer = new Timer();
            mActivityTransitionTimerTask = new TimerTask() {
                public void run() {
                    if (mContentObserver != null) {
                        Log.e(TAG, "mContentObserver.unregister");
                        mContentObserver.unregister();
                    }
                }
            };
            mActivityTransitionTimer.schedule(mActivityTransitionTimerTask, MAX_ACTIVITY_TRANSITION_TIME_MS);
        }

        @Override
        public void registerScreenShotObserver() throws RemoteException {
            if (mActivityTransitionTimerTask != null) {
                mActivityTransitionTimerTask.cancel();
            }

            if (mActivityTransitionTimer != null) {
                mActivityTransitionTimer.cancel();
            }

            if (mContentObserver != null) {
                Log.e(TAG, "mContentObserver.register");
                mContentObserver.register();
            }
        }

        @Override
        public void setScreenShotEnable(boolean enable) throws RemoteException {
            screenshotEnable = enable;
        }
    };
}
