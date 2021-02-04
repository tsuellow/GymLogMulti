package com.example.android.gymlogmulti.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Environment;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Base64;
import android.widget.ImageView;

import com.example.android.gymlogmulti.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PhotoUtils {

    public static Bitmap getAppropriateBitmap(int clientId, Context context){
        String mediumFileName = "MEDIUM_" + clientId ;
        String thumbFileName = "THUMB_" + clientId ;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File medium = new File(storageDir, mediumFileName + ".jpg");
        File thumb = new File(storageDir, thumbFileName + ".jpg");
        String clientMedium=medium.getAbsolutePath();
        String clientThumb=thumb.getAbsolutePath();
        Bitmap bitmap;
        if (medium.exists()) {
            bitmap = BitmapFactory.decodeFile(clientMedium);
        }else if(thumb.exists()){
            bitmap = BitmapFactory.decodeFile(clientThumb);
            bitmap = Bitmap.createScaledBitmap(bitmap, 1000, 1000, true);
        }else{
            bitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.camera);
        }
        return bitmap;
    }

    public static void getAppropriateBitmapRounded(int clientId, Context context, ImageView imageView){
        String mediumFileName = "MEDIUM_" + clientId ;
        String thumbFileName = "THUMB_" + clientId ;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File medium = new File(storageDir, mediumFileName + ".jpg");
        File thumb = new File(storageDir, thumbFileName + ".jpg");
        String clientMedium=medium.getAbsolutePath();
        String clientThumb=thumb.getAbsolutePath();
        Bitmap bitmap;

        if (medium.exists()) {
            bitmap = BitmapFactory.decodeFile(clientMedium);
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(),bitmap);
            roundedBitmapDrawable.setCircular(true);
            imageView.setImageDrawable(roundedBitmapDrawable);
        } else if (thumb.exists()) {
            bitmap = BitmapFactory.decodeFile(clientThumb);
            bitmap = Bitmap.createScaledBitmap(bitmap, 1000, 1000, true);
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(),bitmap);
            roundedBitmapDrawable.setCircular(true);
            imageView.setImageDrawable(roundedBitmapDrawable);
        } else{
        imageView.setImageResource(R.drawable.camera);
        }

    }

    public static String base64Bitmap(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, 0);
    }

    public static Bitmap toGrayScale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public static void createGrayScaleThumb(int clientId, String base64, Context context){

        String mediumFileName = "MEDIUM_" + clientId ;
        String thumbFileName = "THUMB_" + clientId ;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File medium = new File(storageDir, mediumFileName + ".jpg");
        File thumb = new File(storageDir, thumbFileName + ".jpg");
        if (!medium.exists()) {
            try {
                FileOutputStream thumbOut = new FileOutputStream(thumb);
                byte[] biteOutput = Base64.decode(base64, 0);
                Bitmap bitmapDecoded = BitmapFactory.decodeByteArray(biteOutput, 0, biteOutput.length);
                bitmapDecoded.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
                thumbOut.flush();
                thumbOut.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
