package com.example.android.gymlogmulti;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.android.gymlogmulti.utils.MiscellaneousUtils;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.gymlogmulti.data.ClientEntry;
import com.example.android.gymlogmulti.data.GymDatabase;
import com.example.android.gymlogmulti.utils.DateMethods;
import com.example.android.gymlogmulti.utils.PhoneUtilities;
import com.example.android.gymlogmulti.utils.PhotoUtils;
import com.example.android.gymlogmulti.utils.QrCodeUtilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class ModifyClientActivity extends AppCompatActivity {

    static final int REQUEST_TAKE_PHOTO = 1;
    public static final int REQUEST_CODE = 10;
    EditText  mFirstName,mLastName,mPhone, mDob;
    AutoCompleteTextView mOccupation;
    TextInputLayout ilId,ilFirstName,ilLastName,ilPhone, ilOccupation, ilDob;
    TextView mId;
    RadioGroup mGender;
    RadioButton mMaleRb, mFemaleRb;

    ImageView mPhoto;
    Button mTakePic;
    Button mSubmit;
    DatePickerDialog.OnDateSetListener onDateSetListener;

    Date dateOfBirth;

    int currentSyncStatus;

    int clientId;

    String mBase64;



    private GymDatabase mDb;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_client);




        mId=(TextView) findViewById(R.id.tv_id_mod);
        mFirstName =(EditText) findViewById(R.id.ev_first_name_mod);
        mLastName=(EditText) findViewById(R.id.ev_last_name_mod);
        mPhone=(EditText) findViewById(R.id.ev_phone_mod);

        mOccupation=(AutoCompleteTextView) findViewById(R.id.actv_occupation_mod);



        mDob=(EditText) findViewById(R.id.ev_dob_mod);
        mDob.setInputType(InputType.TYPE_NULL);

        ilId=(TextInputLayout) findViewById(R.id.lo_id_mod);
        ilFirstName=(TextInputLayout) findViewById(R.id.lo_first_name_mod);
        ilLastName=(TextInputLayout) findViewById(R.id.lo_last_name_mod);
        ilPhone=(TextInputLayout) findViewById(R.id.lo_phone_mod);
        ilOccupation=(TextInputLayout) findViewById(R.id.lo_occupation_mod);
        ilDob=(TextInputLayout) findViewById(R.id.lo_dob_mod);

        mGender=(RadioGroup) findViewById(R.id.rg_gender_mod);
        mMaleRb=(RadioButton) findViewById(R.id.rb_male_mod);
        mFemaleRb=(RadioButton) findViewById(R.id.rb_female_mod);

        mPhoto=(ImageView) findViewById(R.id.iv_photo_mod);
        mTakePic=(Button) findViewById(R.id.bt_take_photo_mod);
        mSubmit=(Button) findViewById(R.id.bt_submit_mod);

        mDb=GymDatabase.getInstance(getApplicationContext());
        //get client id that started the intent
        Intent i=getIntent();
        clientId = i.getExtras().getInt("CLIENT_ID");

        mDb=GymDatabase.getInstance(getApplicationContext());
        retrieveData(clientId);



        mPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher("NI"));

        //date picker clicklistener


        mDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dateOfBirth!=null) {
                    displayDatePickerDialog(dateOfBirth);
                }else{
                    Calendar cal=Calendar.getInstance();
                    cal.set(2000,0,1);
                    Date defaultDate=cal.getTime();
                    displayDatePickerDialog(defaultDate);
                }
            }
        });

        //take photo click listener
        mTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mTakePic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) {
                                    dispatchTakePictureIntent();

                                }else{
                                    String[] permissionRequested={Manifest.permission.CAMERA};
                                    requestPermissions(permissionRequested, REQUEST_CODE);
                                }

                            }else{
                                dispatchTakePictureIntent();
                            }


                    }
                });


            }
        });

        //submit click listener
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSubmit();
            }
        });

    }

    //to be evaluated jointly on submit
    private void onSubmit(){
        boolean firstNameCorrect=checkFirstName();
        boolean lastNameCorrect=checkLastName();
        boolean phoneCorrect=checkPhoneNumber();
        //boolean idCorrect=checkId();

        if(firstNameCorrect && lastNameCorrect && phoneCorrect){
            //preparing background tasks
            final Context context=getApplicationContext();
            final String qrText="{\"obj\":\"l\",\""+MainActivity.GYM_ID+"id\":"+clientId+"}";
            final File qrFile = createQrCodeFile();

            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    QrCodeUtilities.saveQrCodeNew(clientId,context);
                    Log.d("saveQR","was resaved");
                }

            });

            populateDb();

            getBackToProfile();
        }

    }

    private void getBackToProfile(){
        Intent i = new Intent(ModifyClientActivity.this,ClientProfileActivity.class);
        i.putExtra("CLIENT_ID",clientId);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        getBackToProfile();
    }

    //single conditions for evaluation
    private boolean checkFirstName(){
        if (mFirstName.getText().toString().trim().isEmpty()){
            ilFirstName.setErrorEnabled(true);
            ilFirstName.setError(getString(R.string.err_first_name));
            mFirstName.setError(getString(R.string.input_required));
            return false;
        }
        ilFirstName.setErrorEnabled(false);
        return true;
    }
    private boolean checkLastName(){
        if (mLastName.getText().toString().trim().isEmpty()){
            ilLastName.setErrorEnabled(true);
            ilLastName.setError(getString(R.string.err_last_name));
            mLastName.setError(getString(R.string.input_required));
            return false;
        }
        ilLastName.setErrorEnabled(false);
        return true;
    }
    private boolean checkPhoneNumber(){
        String phoneText= PhoneUtilities.depuratePhone(mPhone.getText().toString());
        if (mPhone.getText().toString().length()==0){
            ilPhone.setErrorEnabled(false);
            return true;
        }
        try{
            String checkText=phoneText.replaceFirst("^0+(?!$)", "");
            Long.parseLong(checkText);
            if (phoneText.length()>=8){
                ilPhone.setErrorEnabled(false);
                return true;
            }else{
                ilPhone.setErrorEnabled(true);
                ilPhone.setError(getString(R.string.err_phone));
                mPhone.setError(getString(R.string.valid_phone_required));
                return false;
            }
        }catch(Exception e){
            ilPhone.setErrorEnabled(true);
            ilPhone.setError(getString(R.string.err_phone));
            mPhone.setError(getString(R.string.valid_phone_required));
            return  false;
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==REQUEST_CODE){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent();
            }else{
                Toast.makeText(this,getString(R.string.camera_permission_request),Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            //setPic();
            takePic();
            setPicture();
        }
    }

    private void populateDb() {
        int genderId=mGender.getCheckedRadioButtonId();
        String gender;
        if (genderId==R.id.rb_male_mod){
            gender="m";
        }else{
            gender="f";
        }
        String firstName= mFirstName.getText().toString();
        String lastName=mLastName.getText().toString();
        String phone=mPhone.getText().toString();
        String occupation=mOccupation.getText().toString();
        //String photoDir=createImageFile().getAbsolutePath();
        Date date=new Date();

        final ClientEntry client=new ClientEntry(clientId,firstName,lastName,dateOfBirth,gender,occupation,phone, mBase64, null, date);
        if (currentSyncStatus==1){
            client.setSyncStatus(2);
        }
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.clientDao().updateClient(client);
            }
        });

    }

    //take a photo functionality

    private File createQrCodeFile()  {
        // Create an image file name
        String idPart = String.valueOf(clientId);//new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "QR_CODE_" + idPart ;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File qrCode = new File(storageDir, imageFileName + ".jpg");
        // Save a file: path for use with ACTION_VIEW intents
        return qrCode;
    }
    private File createImageFile()  {
        // Create an image file name
        String idPart = String.valueOf(clientId);//new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PHOTO_ID_" + idPart ;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, imageFileName + ".jpg");
        return image;
    }
    private File createMediumFile() {
        // Create an image file name
        String idPart = String.valueOf(clientId);//new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "MEDIUM_" + idPart ;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File medium = new File(storageDir, imageFileName + ".jpg");
        // Save a file: path for use with ACTION_VIEW intents
        return medium;
    }
    private File createThumbnailFile() {
        // Create an image file name
        String idPart = String.valueOf(clientId);
        String imageFileName = "THUMB_" + idPart ;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File image = new File(storageDir, imageFileName + ".jpg");
        File thumbnail = new File(storageDir, imageFileName + ".jpg");
        return thumbnail;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            if (Constants.USER_NAME=="CosmosGym"){
                createImageFile().delete();
            }

                photoFile = createImageFile();

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getResources().getString(R.string.file_provider),
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                Log.d("take photo",  photoURI.toString());

            }

        }

    }


    //code to frame and display pic
    private void takePic(){
        Bitmap bitmap = MiscellaneousUtils.rectifyImage(this, createImageFile());
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = (height > width) ? width : height;
        int newHeight = (height > width)? height - ( height - width) : height;
        int cropW = (width - height) / 2;
        cropW = (cropW < 0)? 0: cropW;
        int cropH = (height - width) / 2;
        cropH = (cropH < 0)? 0: cropH;
        Bitmap cropImg = Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);
//        if (Constants.USER_NAME=="CosmosGym"){
//            cropImg=rotateImage(cropImg,270);
//        }
        savePhotoThumbMed(cropImg);
        if (createImageFile().exists()){
            createImageFile().delete();
        }
    }
    private void setPicture(){
        PhotoUtils.getAppropriateBitmapRounded(clientId,this,mPhoto);
//        File medium=createMediumFile();
//        String clientMedium=medium.getAbsolutePath();
//        if (medium.exists()) {
//            Bitmap bitmap = BitmapFactory.decodeFile(clientMedium);
//            RoundedBitmapDrawable roundedBitmapDrawable=RoundedBitmapDrawableFactory.create(getResources(),bitmap);
//            roundedBitmapDrawable.setCircular(true);
//            mPhoto.setImageDrawable(roundedBitmapDrawable);
//        }else{
//            mPhoto.setImageResource(R.drawable.camera);
//        }

    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }



    //method to get payment and profile data related to the client
    private void retrieveData(int clientId){
        final LiveData<ClientEntry> client = mDb.clientDao().getClientById(clientId);
        client.observe(this, new Observer<ClientEntry>() {
            @Override
            public void onChanged(@Nullable ClientEntry clientEntry) {
                client.removeObserver(this);
                //now set all vars
                mId.setText("ID: "+clientEntry.getId());
                mFirstName.setText(clientEntry.getFirstName());
                mLastName.setText(clientEntry.getLastName());
                if (clientEntry.getOccupation()!=null) mOccupation.setText(clientEntry.getOccupation());
                ArrayAdapter<String> occupationAdapter=new ArrayAdapter<String>(ModifyClientActivity.this,
                        android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.occupation_array));
                mOccupation.setAdapter(occupationAdapter);
                mOccupation.setKeyListener(null);
                mOccupation.setOnTouchListener(new View.OnTouchListener(){
                    @Override
                    public boolean onTouch(View v, MotionEvent event){
                        //mOccupation.setText(null);
                        ((AutoCompleteTextView) v).showDropDown();
                        return false;
                    }
                });

                if (clientEntry.getPhone()!=null) mPhone.setText(clientEntry.getPhone());
                //dob setting
                dateOfBirth=clientEntry.getDob();
                if (dateOfBirth!=null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dateOfBirth);
                    mDob.setText(getDateString(cal));
                }
                //gender setting
                if (clientEntry.getGender().contentEquals("m")){
                    mMaleRb.setChecked(true);
                }else{
                    mFemaleRb.setChecked(true);
                }
                //set pick
                mBase64=clientEntry.getPhoto();
                setPicture();

                currentSyncStatus=clientEntry.getSyncStatus();
            }
        });

    }

    public String getDateString(Calendar cal){
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        month = 1 + month;
        return day + "/" + month + "/" + year;
    }

    private void savePhotoThumbMed(final Bitmap bitmap) {
        try {
            File thumbFile = createThumbnailFile();
            File mediumFile = createMediumFile();
            if (thumbFile.exists()){
                thumbFile.delete();
            }
            if (mediumFile.exists()){
                mediumFile.delete();
            }
            FileOutputStream thumbOut = new FileOutputStream(thumbFile);
            FileOutputStream mediumOut = new FileOutputStream(mediumFile);
            Bitmap thumb = Bitmap.createScaledBitmap(bitmap, 96, 96, false);
            Bitmap medium = Bitmap.createScaledBitmap(bitmap, 1000, 1000, false);
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
            medium.compress(Bitmap.CompressFormat.JPEG, 100, mediumOut);

            mBase64= PhotoUtils.base64Bitmap(PhotoUtils.toGrayScale(thumb));

            thumbOut.flush();
            mediumOut.flush();
            thumbOut.close();
            mediumOut.close();
            Log.d("ThumbMed saved", "Thumb and Medium ok");
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.d("ThumbMed saved", "Thumb Medium IOException");
        }
    }

//    private static void saveQrCode(String qrText, File qrFile, Context context){
//        try {
//
//            if (qrFile.exists()){
//                qrFile.delete();
//            }
//            FileOutputStream qrOut = new FileOutputStream(qrFile);
//            //String qrText="{\"obj\":\"l\",\""+MainActivity.GYM_ID+"id\":"+mId.getText().toString()+"}";
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

    private void displayDatePickerDialog(Date date){
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(ModifyClientActivity.this);
        View mView=getLayoutInflater().inflate(R.layout.dialog_date_picker,null);
        final DatePicker mDatePicker=(DatePicker) mView.findViewById(R.id.dp_date_picker);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        mDatePicker.init(year,month,day,null);

        Button mOk=(Button) mView.findViewById(R.id.btn_ok_dp);
        Button mCancel=(Button) mView.findViewById(R.id.btn_cancel_dp);
        mBuilder.setTitle(R.string.dob);
        mBuilder.setView(mView);

        final AlertDialog dialog=mBuilder.create();
        //button to dismiss dialog
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal=Calendar.getInstance();
                int year, month, day;
                year=mDatePicker.getYear();
                month=mDatePicker.getMonth();
                day=mDatePicker.getDayOfMonth();
                cal.set(year,month,day);
                mDob.setText(DateMethods.getDateString(cal));
                dateOfBirth=DateMethods.getRoundDate(cal.getTime());
                dialog.dismiss();
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


}

