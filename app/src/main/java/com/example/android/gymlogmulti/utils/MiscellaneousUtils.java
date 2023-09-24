package com.example.android.gymlogmulti.utils;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.android.gymlogmulti.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Objects;

public class MiscellaneousUtils {



    public Uri getRawUri(String filename, Context context) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + File.pathSeparator + File.separator + context.getPackageName() + "/raw/" + filename);
    }
    public static Uri getDrawableUri(int res, Context context) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + File.pathSeparator + File.separator + File.separator
                + context.getPackageName()
                + File.separator
                + res);
    }
    public Uri getMipmapUri(String filename, Context context) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + File.pathSeparator + File.separator + context.getPackageName() + "/mipmap/" + filename);
    }

    static final String AB = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    public static String randomString(int len){
        StringBuilder sb = new StringBuilder(len);
        for(int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    public static void sendImageAndTextOnWs(Context context, String phone, Uri uri, String text){

        String toNumber= PhoneUtilities.depuratePhone(phone);
        if (toNumber!=null) {
            if (!toNumber.isEmpty()) {
                toNumber = toNumber.replaceFirst("^0+(?!$)", "");


                Intent sendIntent = new Intent("android.intent.action.SEND");
                //sendIntent.setComponent(new ComponentName("com.whatsapp","com.whatsapp.ContactPicker"));
                sendIntent.setPackage("com.whatsapp");
                sendIntent.setType("image/jpeg");
                sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                sendIntent.putExtra("jid", "" + toNumber + "@s.whatsapp.net");
                sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                context.startActivity(sendIntent);
            }
        }
    }

    public static void sendTextOnWs(Context context, String phone, String text){

        String toNumber= PhoneUtilities.depuratePhone(phone);
        toNumber=toNumber.replaceFirst("^0+(?!$)", "");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+text));
        context.startActivity(intent);

    }

    public static File saveStandardFile( Context context)  {
        // Create an image file name
        String imageFileName = "receiptLogo"  ;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File standardFile = new File(storageDir, imageFileName + ".jpg");
        // Save a file: path for use with ACTION_VIEW intents
        if (!standardFile.exists()){
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.logo_big);

            // Get the bitmap from drawable object
            Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
            try{
                OutputStream stream = null;
                stream = new FileOutputStream(standardFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
                stream.flush();
                stream.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return standardFile;
    }

    public static boolean contactExists(Context context, String number) {
        /// number is the phone number
        Uri lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        String[] mPhoneNumberProjection = { ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME };
        Cursor cur = context.getContentResolver().query(lookupUri,mPhoneNumberProjection, null, null, null);
        try {
            if (cur.moveToFirst()) {
                //Toast.makeText(context,"already exists",Toast.LENGTH_LONG).show();
                return true;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        //Toast.makeText(context,"does not exists",Toast.LENGTH_LONG).show();
        return false;
    }

    public static boolean addContact(String DisplayName,String MobileNumber, Context context)
    {
        MobileNumber = MobileNumber.replace(" ", "");
        ArrayList<ContentProviderOperation> ops = new ArrayList< ContentProviderOperation >();

        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        //------------------------------------------------------ Names
        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(
                        ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                        DisplayName).build());

        //------------------------------------------------------ Mobile Number
        if (MobileNumber != null && !MobileNumber.isEmpty()) {
            MobileNumber=PhoneUtilities.depuratePhone(MobileNumber);
            ops.add(ContentProviderOperation.
                    newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, MobileNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());
        }
        // Asking the Contact provider to create a new contact
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("contacts fail","Exception: " + e.getMessage());
            return  false;
        }
    }


    public static Bitmap rectifyImage(Context context,File imageFile){
        Bitmap originalBitmap= BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        try{
            Uri uri=Uri.fromFile(imageFile);
            InputStream input = context.getContentResolver().openInputStream(uri);
            ExifInterface ei;

            if (Build.VERSION.SDK_INT > 23)
                ei = new ExifInterface(input);
            else
                ei = new ExifInterface(uri.getPath());

            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(originalBitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(originalBitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(originalBitmap, 270);
                default:
                    return originalBitmap;
            }
        }catch (Exception e){
            return originalBitmap;
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public static Uri saveQrCode(@NonNull String name, @NonNull Context context) {
        //OutputStream fos;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name + ".jpg");
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            return imageUri;
            //fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
        } else {
            String imagesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
            File image = new File(imagesDir, name + ".jpg");
            return Uri.fromFile(image);
            //fos = new FileOutputStream(image);
        }
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//        Objects.requireNonNull(fos).close();
    }
}
