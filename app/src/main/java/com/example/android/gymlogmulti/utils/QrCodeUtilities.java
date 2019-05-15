package com.example.android.gymlogmulti.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

import com.example.android.gymlogmulti.MainActivity;
import com.example.android.gymlogmulti.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;


public class QrCodeUtilities {

    public static Bitmap GenerateQrCode(Context context, String jsonText){
        try {
            //setting size of qr code
            int width =900;
            int height = 900;
            int smallestDimension = width < height ? width : height;


            String qrCodeData = jsonText;
            //setting parameters for qr code
            String charset = "UTF-8";
            Map<EncodeHintType, ErrorCorrectionLevel> hintMap =new HashMap<EncodeHintType, ErrorCorrectionLevel>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            return CreateQRCode(qrCodeData, charset, hintMap, smallestDimension, smallestDimension, context);

        } catch (Exception ex) {
            Log.e("QrGenerate",ex.getMessage());
            return null;
        }
    }

    private static  Bitmap CreateQRCode(String qrCodeData, String charset, Map hintMap, int qrCodeheight, int qrCodewidth, Context context){


        try {
            //generating qr code in bitmatrix type
            BitMatrix matrix = new MultiFormatWriter().encode(new String(qrCodeData.getBytes(charset), charset),
                    BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap);
            //converting bitmatrix to bitmap

            int width = matrix.getWidth();
            int height = matrix.getHeight();
            int[] pixels = new int[width * height];
            // All are 0, or black, by default
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = matrix.get(x, y) ? BLACK : WHITE;
                    //pixels[offset + x] = matrix.get(x, y) ? ResourcesCompat.getColor(getResources(),R.color.colorB,null) :WHITE;
                }
            }


            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            //setting bitmap to image view

            Bitmap overlayBig = BitmapFactory.decodeResource(context.getResources(),R.drawable.qr_image_2);
            Bitmap overlay = Bitmap.createScaledBitmap(overlayBig, 168, 168, false);

            return mergeBitmaps(overlay,bitmap);


        }catch (Exception er){
            Log.e("QrGenerate",er.getMessage());
            return null;
        }
    }



    private static Bitmap mergeBitmaps(Bitmap overlay, Bitmap bitmap) {

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        Bitmap combined = Bitmap.createBitmap(width, height, bitmap.getConfig());
        Canvas canvas = new Canvas(combined);
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        canvas.drawBitmap(bitmap, new Matrix(), null);

        int centreX = (canvasWidth  - overlay.getWidth()) /2;
        int centreY = (canvasHeight - overlay.getHeight()) /2 ;
        canvas.drawBitmap(overlay, centreX, centreY, null);

        return combined;
    }

    public static void saveQrCode(int clientId, File qrFile, Context context){
        try {

            String qrText="{\"obj\":\"l\",\""+ MainActivity.GYM_ID+"id\":"+clientId+"}";

            if (qrFile.exists()){
                qrFile.delete();
            }
            FileOutputStream qrOut = new FileOutputStream(qrFile);

            Bitmap qrCode= QrCodeUtilities.GenerateQrCode(context,qrText);
            qrCode.compress(Bitmap.CompressFormat.JPEG, 100, qrOut);
            qrOut.flush();
            qrOut.close();
            Log.d("QR saved", "QR ok");
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.d("QR saved", "QR IOException");
        }
    }

    public static File createQrCodeFile(int clientId, Context context)  {
        // Create an image file name
        String imageFileName = "QR_CODE_" + clientId ;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File qrCode = new File(storageDir, imageFileName + ".jpg");
        // Save a file: path for use with ACTION_VIEW intents
        return qrCode;
    }



}
