package com.example.android.gymlogmulti.utils;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.example.android.gymlogmulti.Constants;
import com.example.android.gymlogmulti.MainActivity;
import com.example.android.gymlogmulti.R;
import com.example.android.gymlogmulti.data.ClientEntry;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;


public class QrCodeUtilities {

    public static Bitmap GenerateQrCode(Context context, String jsonText){
        try {
            //setting size of qr code
            int width =900;
            int height = 900;
            int smallestDimension = Math.min(width, height);


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
            Bitmap overlay = Bitmap.createScaledBitmap(overlayBig, Constants.qrDims, Constants.qrDims, false);

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

//    public static void saveQrCode(int clientId, File qrFile, Context context){
//        try {
//
//            String qrText="{\"obj\":\"l\",\""+ MainActivity.GYM_ID+"id\":"+clientId+"}";
//
//            if (qrFile.exists()){
//                qrFile.delete();
//            }
//            FileOutputStream qrOut = new FileOutputStream(qrFile);
//
//            Bitmap qrCode= QrCodeUtilities.GenerateQrCode(context,qrText);
//            qrCode.compress(Bitmap.CompressFormat.JPEG, 100, qrOut);
//            qrOut.flush();
//            qrOut.close();
//            Log.d("QR saved", "QR ok");
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            Log.d("QR saved", "QR IOException");
//        }
//    }

//    public static File createQrCodeFile(int clientId, Context context)  {
//        // Create an image file name
//        String imageFileName = "QR_CODE_" + clientId ;
//        //File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File qrCode = MiscellaneousUtils.getSharedImageFile(imageFileName,context);//new File(storageDir, imageFileName + ".jpg");
//        // Save a file: path for use with ACTION_VIEW intents
//        return qrCode;
//    }

    public static void saveQrCodeNew(int clientId, @NonNull Context context) {
        String qrText="{\"obj\":\"l\",\""+ MainActivity.GYM_ID+"id\":"+clientId+"}";
        Bitmap bitmap= QrCodeUtilities.GenerateQrCode(context,qrText);
        String name = "QR_CODE_" + clientId ;
        try {
            OutputStream fos;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = context.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name + ".jpg");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM+"/"+context.getString(R.string.app_label));
                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = resolver.openOutputStream(Objects.requireNonNull(imageUri),"rwt");
            } else {
                String imagesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
                File qrFile = new File(imagesDir, name + ".jpg");
                if (qrFile.exists()){
                    qrFile.delete();
                }
                fos = new FileOutputStream(qrFile);
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Objects.requireNonNull(fos).close();
        }catch (Exception e){
            Log.d("saveQR","could not save");
        }
    }

    public static Uri getQrUri(int clientId, Context context){
        String name = "QR_CODE_" + clientId +".jpg";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            String selection = MediaStore.MediaColumns.RELATIVE_PATH+" LIKE ?";

            String[] projection = new String[]{MediaStore.MediaColumns._ID,MediaStore.MediaColumns.DISPLAY_NAME};
            String[] selectionArgs = new String[]{Environment.DIRECTORY_DCIM+"/"+context.getString(R.string.app_label)+"%"};

            Cursor cursor = context.getContentResolver().query(contentUri, projection, selection, selectionArgs, null);

            Uri uri = null;

            if (cursor.getCount() == 0) {
                Log.d("saveQR", "cursor is 0");
            } else {
                while (cursor.moveToNext()) {
                    String fileName = cursor.getString(Math.max(0, cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)));
                    Log.d("saveQR", fileName);
                    if (fileName.equals(name)) {
                        Log.d("saveQR", "file found");
                        long id = cursor.getLong(Math.max(0, cursor.getColumnIndex(MediaStore.MediaColumns._ID)));

                        uri = ContentUris.withAppendedId(contentUri, id);

                        return uri;
                    }
                }
                Log.d("saveQR", "cursor is not 0 but was not found");
                cursor.close();
            }

            return null;
        }else{
            String imagesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
            File image = new File(imagesDir, name);
            return Uri.parse(image.getAbsolutePath());
        }
    }

//    public static void saveQrCode(int id, Context context){
//        try {
//            Uri uri=MiscellaneousUtils.getSharedImageUri("QR_CODE_" +id,context);
////            File qrFile=createQrCodeFile(id,context);
////            if (qrFile.exists()){
////                qrFile.delete();
////            }
//            FileOutputStream qrOut = new FileOutputStream(uri);
//            String qrText="{\"obj\":\"l\",\""+MainActivity.GYM_ID+"id\":"+id+"}";
//            Bitmap qrCode= QrCodeUtilities.GenerateQrCode(context,qrText);
//            qrCode.compress(Bitmap.CompressFormat.JPEG, 100, qrOut);
//            qrOut.flush();
//            qrOut.close();
//            Log.d("QR saved", "QR ok");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            Log.d("QR saved", "QR IOException");
//        }
//    }

    public static AlertDialog displayQrDialog(Context context,  ClientEntry client){
        AlertDialog.Builder mBuilder= new AlertDialog.Builder(context);
        Log.d("QR saved", "dialog ok");
        mBuilder.setTitle(R.string.sendqrtitle)
                .setMessage(context.getString(R.string.sendqr)+" "+client.getFirstName())
                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(context.getString(R.string.send), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        shareFileOnWhatsApp(client,context);
                        dialog.dismiss();
                    }
                });
        return mBuilder.create();
    }

    public static void shareFileOnWhatsApp(ClientEntry client, Context context) {
        if(client.getPhone()==null || client.getPhone().isEmpty())return;
        String toNumber= PhoneUtilities.depuratePhone(client.getPhone());
        toNumber=toNumber.replaceFirst("^0+(?!$)", "");

        Uri qrUri = getQrUri(client.getId(),context);
        Log.d("QR saved", "uri"+qrUri.getPath());

        Intent sendIntent = new Intent("android.intent.action.SEND");

        //sendIntent.setComponent(new ComponentName("com.whatsapp","com.whatsapp.ContactPicker"));
        sendIntent.setPackage("com.whatsapp");
        sendIntent.setType("image/jpeg");
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.putExtra(Intent.EXTRA_STREAM,qrUri);
        sendIntent.putExtra("jid", ""+toNumber+"@s.whatsapp.net");
        sendIntent.putExtra(Intent.EXTRA_TEXT,context.getString(R.string.hi)+" "+
                client.getFirstName()+", "+ PreferenceManager.getDefaultSharedPreferences(context).getString("gymname",context.getString(R.string.your_gym))+" "+context.getString(R.string.welcome_whatsapp_qr_code)+client.getId()+context.getString(R.string.asterisc_to_bolden_whatsapp));
        context.startActivity(sendIntent);
    }


}
