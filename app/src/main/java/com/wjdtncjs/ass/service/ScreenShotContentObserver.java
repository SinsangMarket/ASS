package com.wjdtncjs.ass.service;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

import com.wjdtncjs.ass.Util;

/**
 * Created by soochun on 2017-01-04.
 */

public class ScreenShotContentObserver extends ContentObserver {


    private final String TAG = this.getClass().getSimpleName();
    private static final String[] PROJECTION = new String[]{
            MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.ImageColumns._ID
    };
    private static final long DEFAULT_DETECT_WINDOW_SECONDS = 10;
    private static final String SORT_ORDER = MediaStore.Images.Media.DATE_ADDED + " DESC";

    public static final String FILE_POSTFIX = "FROM_ASS";
    private static final String WATERMARK = "Scott";
    private ScreenShotListener mListener;
    private ContentResolver mContentResolver;
    private String lastPath;

    public ScreenShotContentObserver(Handler handler, ContentResolver contentResolver, ScreenShotListener listener) {
        super(handler);
        mContentResolver = contentResolver;
        mListener = listener;
    }

    @Override
    public boolean deliverSelfNotifications() {
        Log.e(TAG, "deliverSelfNotifications");
        return super.deliverSelfNotifications();
    }

    @Override
    synchronized public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            //above API 16 Pass~!(duplicated call...)
            return;
        }
        Log.e(TAG, "[Start] onChange : " + selfChange);
        try {
            process(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            Log.e(TAG, "[Finish] general");
        } catch (Exception e) {
            Log.e(TAG, "[Finish] error : " + e.toString(), e);
        }
    }

    @Override
    synchronized public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        Log.e(TAG, "[Start] onChange : " + selfChange + " / uri : " + uri.toString());

        if (uri.toString().startsWith(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())) {
            try {
                process(uri);
                Log.e(TAG, "[Finish] general");
            } catch (Exception e) {
                Log.e(TAG, "[Finish] error : " + e.toString(), e);
            }
        } else {
            Log.e(TAG, "[Finish] not EXTERNAL_CONTENT_URI ");
        }
    }

    public void register() {
        Log.d(TAG, "register");
        mContentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, this);
    }

    public void unregister() {
        Log.d(TAG, "unregister");
        mContentResolver.unregisterContentObserver(this);
    }

    public static void resultProcess(Context context, Bitmap bitmap, String fileName) throws Exception {
        String text = WATERMARK;
        for (int i = 0; i < 700; i++) {
            text += " " + WATERMARK;
        }
        Bitmap newBitmap = Util.getOverlayBitmap(context, bitmap, text);
        bitmap.recycle();
        newBitmap = Util.getOverlayBitmap2(context, newBitmap, "Hello. Capture with Android Screenshot Service :)");
        Util.saveImage(context, newBitmap, fileName);
        newBitmap.recycle();
    }

    private boolean process(Uri uri) throws Exception {
        Data result = getLatestData(uri);
        if (result == null) {
            Log.e(TAG, "[Result] result is null!!");
            return false;
        }
        if (lastPath != null && lastPath.equals(result.path)) {
            Log.e(TAG, "[Result] duplicate!!");
            return false;
        }
        long currentTime = System.currentTimeMillis() / 1000;
        if (matchPath(result.path) && matchTime(currentTime, result.dateAdded)) {
            lastPath = result.path;
            Uri screenUri = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString() + "/" + result.id);
            Log.e(TAG, "[Result] This is screenshot!! : " + result.fileName + " | dateAdded : " + result.dateAdded + " / " + currentTime);
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContentResolver, screenUri);
            Bitmap copyBitmap = bitmap.copy(bitmap.getConfig(), true);
            bitmap.recycle();
            int temp = mContentResolver.delete(screenUri, null, null);
            Log.e(TAG, "Delete Result : " + temp);
            if (mListener != null) {
                mListener.onScreenshotTaken(copyBitmap, result.fileName);
            }
            return true;
        } else {
            Log.e(TAG, "[Result] No ScreenShot : " + result.fileName);
        }
        return false;
    }

    private Data getLatestData(Uri uri) throws Exception {
        Data data = null;
        Cursor cursor = null;
        try {
            cursor = mContentResolver.query(uri, PROJECTION, null, null, SORT_ORDER);
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID));
                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                long dateAdded = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));

                if (fileName.contains(FILE_POSTFIX)) {
                    if (cursor.moveToNext()) {
                        id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID));
                        fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                        path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        dateAdded = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                    } else {
                        return null;
                    }
                }

                data = new Data();
                data.id = id;
                data.fileName = fileName;
                data.path = path;
                data.dateAdded = dateAdded;
                Log.e(TAG, "[Recent File] Name : " + fileName);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return data;
    }

    private boolean matchPath(String path) {
        return (path.toLowerCase().contains("screenshots/") && !path.contains(FILE_POSTFIX));
    }

    private boolean matchTime(long currentTime, long dateAdded) {
        return Math.abs(currentTime - dateAdded) <= DEFAULT_DETECT_WINDOW_SECONDS;
    }

    class Data {
        long id;
        String fileName;
        String path;
        long dateAdded;
    }
}
