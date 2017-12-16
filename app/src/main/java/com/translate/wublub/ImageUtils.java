package com.translate.wublub;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileInputStream;
import java.io.InputStream;


public class ImageUtils {

    public static Bitmap compressBitmap(Context context, Uri uri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        decodeBitmap(context, uri, options);
        options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap bitmap = null;

        try {
            bitmap = decodeBitmap(context, uri, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap decodeBitmap(Context context, Uri uri,
                                      BitmapFactory.Options options) {
        Bitmap bitmap = null;

        if (uri != null) {
            InputStream inputStream = null;
            try {
                /**
                 * 将图片的Uri地址转换成一个输入流
                 */
                if (uri.toString().startsWith("content://")) {
                    ContentResolver cr = context.getContentResolver();
                    inputStream = cr.openInputStream(uri);
                } else {
                    inputStream = new FileInputStream(uri.toString());
                }

                /**
                 * 将输入流转换成Bitmap
                 */
                bitmap = BitmapFactory.decodeStream(inputStream, null, options);

                assert inputStream != null;
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

}
