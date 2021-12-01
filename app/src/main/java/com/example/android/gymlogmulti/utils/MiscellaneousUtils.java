package com.example.android.gymlogmulti.utils;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.android.gymlogmulti.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

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

    public static void sendImageAndTextOnWs(Context context, String phone, Uri uri, String text){

        String toNumber= PhoneUtilities.depuratePhone(phone);
        toNumber=toNumber.replaceFirst("^0+(?!$)", "");

        Intent sendIntent = new Intent("android.intent.action.SEND");
        //sendIntent.setComponent(new ComponentName("com.whatsapp","com.whatsapp.ContactPicker"));
        sendIntent.setPackage("com.whatsapp");
        sendIntent.setType("image/jpeg");
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sendIntent.putExtra(Intent.EXTRA_STREAM,uri);
        sendIntent.putExtra("jid", ""+toNumber+"@s.whatsapp.net");
        sendIntent.putExtra(Intent.EXTRA_TEXT,text);
        context.startActivity(sendIntent);

    }

    public static File saveStandardFile( Context context)  {
        // Create an image file name
        String imageFileName = "receiptImage"  ;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File standardFile = new File(storageDir, imageFileName + ".jpg");
        // Save a file: path for use with ACTION_VIEW intents
        if (!standardFile.exists()){
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.receipt_image);

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
}
