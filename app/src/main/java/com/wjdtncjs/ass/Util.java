package com.wjdtncjs.ass;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;

import com.wjdtncjs.ass.service.ScreenShotContentObserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by soochun on 2017-01-16.
 */

public class Util {
    public static int dpToPx(int dp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics()));
    }

    public static Bitmap getOverlayBitmap(Context context, Bitmap bitmap, String text) {
        Bitmap result = bitmap.copy(bitmap.getConfig(), true);
        float scale = context.getResources().getDisplayMetrics().density;
        Canvas canvas = new Canvas(result);
        TextPaint mTextPaint = new TextPaint();
        mTextPaint.setTextSize((int) (16 * scale));
        mTextPaint.setColor(Color.RED);
        mTextPaint.setAlpha(38);
        StaticLayout mTextLayout = new StaticLayout(text, mTextPaint, canvas.getWidth() + 700, Layout.Alignment.ALIGN_CENTER, 1.1f, 0.3f, true);
        canvas.save();

        float textX = -200;
        float textY = -10;

        canvas.translate(textX, textY);
        mTextLayout.draw(canvas);
        canvas.restore();
        return result;
    }

    public static Bitmap getOverlayBitmap2(Context context, Bitmap bitmap, String text) {
        Bitmap result = bitmap.copy(bitmap.getConfig(), true);
        float scale = context.getResources().getDisplayMetrics().density;
        Canvas canvas = new Canvas(result);

        TextPaint mTextPaint = new TextPaint();
        mTextPaint.setTextSize((int) (12 * scale));
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setAlpha(204);
        mTextPaint.setShadowLayer(5f, 0f, 1f, Color.DKGRAY);

        StaticLayout mTextLayout = new StaticLayout(text, mTextPaint, canvas.getWidth() - Util.dpToPx(87), Layout.Alignment.ALIGN_CENTER, 1.0f, 0.3f, true);
        canvas.save();

        float textX = (canvas.getWidth() / 2) - (mTextLayout.getWidth() / 2);
        float textY = result.getHeight() - Util.dpToPx(72);

        canvas.translate(textX, textY);
        mTextLayout.draw(canvas);
        canvas.restore();
        return result;
    }

    public static void saveImage(Context context, Bitmap bitmap, String title) throws Exception {
        OutputStream fOut = null;
        title = title.replaceAll(" ", "+");
        int index = title.lastIndexOf(".png");
        if (index == -1) {
            index = title.lastIndexOf(".jpg");
        }
        String fileName = title.substring(0, index) + ScreenShotContentObserver.FILE_POSTFIX + ".png";
        final String appDirectoryName = "Screenshots";
        final File imageRoot = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), appDirectoryName);
        imageRoot.mkdirs();
        final File file = new File(imageRoot, fileName);
        fOut = new FileOutputStream(file);

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        fOut.flush();
        fOut.close();

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "XXXXX");
        values.put(MediaStore.Images.Media.DESCRIPTION, "description here");
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.ImageColumns.BUCKET_ID, file.hashCode());
        values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, file.getName());
        values.put("_data", file.getAbsolutePath());
        ContentResolver cr = context.getContentResolver();
        Uri newUri = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri));
    }
}
