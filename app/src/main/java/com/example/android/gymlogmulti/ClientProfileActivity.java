package com.example.android.gymlogmulti;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.gymlogmulti.data.ClientEntry;
import com.example.android.gymlogmulti.data.GymDatabase;
import com.example.android.gymlogmulti.data.PaymentEntry;
import com.example.android.gymlogmulti.data.VisitEntry;
import com.example.android.gymlogmulti.utils.DateMethods;
import com.example.android.gymlogmulti.utils.MiscellaneousUtils;
import com.example.android.gymlogmulti.utils.PhoneUtilities;
import com.example.android.gymlogmulti.utils.PhotoUtils;
import com.example.android.gymlogmulti.utils.QrCodeUtilities;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class ClientProfileActivity extends AppCompatActivity {

    //activity objects
    RecyclerView rvVisits;
    CpVisitsAdapter mVisitsAdapter;
    RecyclerView rvPayments;
    CpPaymentsAdapter mPaymentsAdapter;
    GymDatabase mDb;
    Context mContext;
    Toolbar mToolbar;
    SharedPreferences sharedPreferences;

    //visible objects
    ImageView mProfilePhoto;
    ImageView mQrCode;
    TextView mFirstName;
    TextView mLastName;
    TextView mPersonalInfo;
    TextView mPhone;
    TextView mId;
    Button mModify;
    TextView mPaymentsCount;
    Button mPay;
    TextView mVisitCounts;
    int clientId;
    String phoneNr=null;
    String firstName="";
    String contactName="";
    ClientEntry mClientEntry;
    //SearchAdapter mAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_profile);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_client_profile);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);

        mProfilePhoto=(ImageView) findViewById(R.id.iv_photo_profile);
        mQrCode=(ImageView) findViewById(R.id.iv_qr_icon);
        mFirstName=(TextView) findViewById(R.id.tv_first_name_profile);
        mLastName=(TextView) findViewById(R.id.tv_last_name_profile);
        mPersonalInfo=(TextView) findViewById(R.id.tv_info_profile);
        mPhone=(TextView) findViewById(R.id.tv_tel_profile);
        mId=(TextView) findViewById(R.id.tv_id_profile);
        mPaymentsCount=(TextView) findViewById(R.id.tv_payments_profile);
        mVisitCounts=(TextView) findViewById(R.id.tv_visits_profile);
        mModify=(Button) findViewById(R.id.bt_modify_profile);
        mPay=(Button) findViewById(R.id.bt_pay_profile);

        rvPayments=(RecyclerView) findViewById(R.id.rv_client_profile_payments);
        rvVisits=(RecyclerView) findViewById(R.id.rv_client_profile_visits);


        mContext=getApplicationContext();
        mDb = GymDatabase.getInstance(getApplicationContext());





        Intent i=getIntent();
        clientId=i.getExtras().getInt("CLIENT_ID");

        retrieveData(clientId);
        retrieveDataPayments(clientId);
        retrieveDataVisits(clientId);

        //mAdapter = new SearchAdapter(this);
        mPaymentsAdapter=new CpPaymentsAdapter(this,mDb);
        mVisitsAdapter= new CpVisitsAdapter(this);
        rvVisits.setAdapter(mVisitsAdapter);
        rvPayments.setAdapter(mPaymentsAdapter);
        rvPayments.setLayoutManager(new LinearLayoutManager(this));
        rvVisits.setLayoutManager(new LinearLayoutManager(this));

        mPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (phoneNr!=null){
                        openWhatsAppContact(phoneNr, firstName);
                    //openWhatsApp2("00505"+phoneNr);
                }else{
                    Toast.makeText(mContext,R.string.need_phone,Toast.LENGTH_LONG).show();
                }

            }
        });

//        final File qrFile= QrCodeUtilities.createQrCodeFile(clientId,mContext);
        if (QrCodeUtilities.getQrUri(clientId,mContext)==null) {
            mQrCode.setColorFilter(getResources().getColor(android.R.color.tab_indicator_text));
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    QrCodeUtilities.saveQrCodeNew(clientId,mContext);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mQrCode.setColorFilter(getResources().getColor(R.color.colorAccent));
                        }
                    });
                }
            });
        }



        mQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (QrCodeUtilities.getQrUri(clientId,mContext)!=null) {
                    if (phoneNr != null) {
                        QrCodeUtilities.shareFileOnWhatsApp(mClientEntry,mContext);

                        //openWhatsApp2("00505"+phoneNr);
                    } else {
                        Toast.makeText(mContext, R.string.need_phone, Toast.LENGTH_LONG).show();
                    }
                }else{
                    Log.d("quepaso", "file inexistent");
                }

            }
        });

        mQrCode.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String idPart = String.valueOf(clientId);
                String qrFileName = "QR_CODE_" + idPart ;
                //File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//                File qrCode = MiscellaneousUtils.getSharedImageFile(qrFileName,mContext);//new File(storageDir, qrFileName + ".jpg");
//                Log.d("quepaso",qrCode.getAbsolutePath());
                Uri qrUri = QrCodeUtilities.getQrUri(clientId,mContext);
                if (qrUri!=null){
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(qrUri, "image/*");
                    startActivity(intent);
                }else{
                    Toast.makeText(mContext,"uri for file not found",Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });

        mModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ClientProfileActivity.this,ModifyClientActivity.class);
                i.putExtra("CLIENT_ID",clientId);
                startActivity(i);
            }
        });

        mPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ClientProfileActivity.this,PayClientActivity.class);
                i.putExtra("CLIENT_ID",clientId);
                startActivity(i);
            }
        });



    }

    private void retrieveDataPayments(int clientId) {

        final LiveData<List<PaymentEntry>> payments = mDb.paymentDao().getPaymentsByClient(clientId);
        payments.observe(this, new Observer<List<PaymentEntry>>() {
            @Override
            public void onChanged(@Nullable List<PaymentEntry> paymentEntries) {
                mPaymentsAdapter.setPayments(paymentEntries);
                String paymentsText=mPaymentsAdapter.getItemCount()+" "+getString(R.string.payments);
                mPaymentsCount.setText(paymentsText);

            }
        });
    }

    private void retrieveDataVisits(int clientId) {

        final LiveData<List<VisitEntry>> visits = mDb.visitDao().getVisitsByClient(clientId);
        visits.observe(this, new Observer<List<VisitEntry>>() {
            @Override
            public void onChanged(@Nullable List<VisitEntry> visitEntries) {
                mVisitsAdapter.setVisits(visitEntries);
                String visitsText=mVisitsAdapter.getItemCount()+" "+getString(R.string.visits);
                mVisitCounts.setText(visitsText);

            }
        });
    }

    private void retrieveData(int clientId){
        final LiveData<ClientEntry> client = mDb.clientDao().getClientById(clientId);
        client.observe(this, new Observer<ClientEntry>() {
            @Override
            public void onChanged(@Nullable ClientEntry clientEntry) {
                client.removeObserver(this);
                //now set all vars
                mClientEntry=clientEntry;
                mId.setText("ID: "+clientEntry.getId());
                mFirstName.setText(clientEntry.getFirstName());
                mLastName.setText(clientEntry.getLastName());
                firstName=clientEntry.getFirstName();

                //dob string
                String str_occ="--";
                if (clientEntry.getOccupation()!=null && !clientEntry.getOccupation().isEmpty()) {
                    str_occ=clientEntry.getOccupation();
                }
                //dob string
                String str_dob="--";
                Date dateOfBirth=clientEntry.getDob();
                if (dateOfBirth!=null) {
                    int age= DateMethods.getDiffYears(dateOfBirth,new Date());
                    str_dob=""+age;
                }
                String str_info="("+str_dob+", "+clientEntry.getGender()+", "+str_occ+")";
                mPersonalInfo.setText(str_info);

                String str_phone="--";
                if (clientEntry.getPhone()!=null && !clientEntry.getPhone().isEmpty()){
                    str_phone=clientEntry.getPhone();
                    phoneNr=clientEntry.getPhone();
                }
                mPhone.setText(str_phone);

                //set photo
                PhotoUtils.getAppropriateBitmapRounded(clientEntry.getId(),mContext,mProfilePhoto);

                mPaymentsAdapter.setAdditionalData(clientEntry.getPhone(),clientEntry.getFirstName()+" "+clientEntry.getLastName());


                if (!MiscellaneousUtils.contactExists(mContext,phoneNr)){
                    boolean res=MiscellaneousUtils.addContact(clientEntry.getFirstName()+" "+clientEntry.getLastName().substring(0,1)+".",clientEntry.getPhone(),mContext);
                    if (res){
                        Toast.makeText(mContext,"client added to contacts",Toast.LENGTH_LONG).show();
                    }
                }

            }
        });

    }


    public void openWhatsAppContact(String phone, String name){
        try {
            String toNumber= PhoneUtilities.depuratePhone(phone);
            toNumber=toNumber.replaceFirst("^0+(?!$)", "");
            String text = getString(R.string.hi)+" "+name;// Replace with your message.
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+text));
            startActivity(intent);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

//    public void shareQrOnWhatsApp(String phone)  {
//        String toNumber= PhoneUtilities.depuratePhone(phone);
//        toNumber=toNumber.replaceFirst("^0+(?!$)", "");
//        String idPart = String.valueOf(clientId);
//        String qrFileName = "QR_CODE_" + idPart ;
////        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
////        Log.d("quepaso", storageDir.getAbsolutePath());
//        File qrCode = MiscellaneousUtils.getSharedImageFile(qrFileName,mContext);//new File(storageDir, qrFileName + ".jpg");
//        Log.d("quepaso", qrCode.getAbsolutePath());
//        Uri qrUri = Uri.parse(qrCode.getAbsolutePath());
//
//        Intent sendIntent = new Intent("android.intent.action.SEND");
//
//        //sendIntent.setComponent(new ComponentName("com.whatsapp","com.whatsapp.ContactPicker"));
//        sendIntent.setPackage("com.whatsapp");
//        sendIntent.setType("image/jpeg");
//        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        sendIntent.putExtra(Intent.EXTRA_STREAM,qrUri);
//        sendIntent.putExtra("jid", ""+toNumber+"@s.whatsapp.net");
//        sendIntent.putExtra(Intent.EXTRA_TEXT,getString(R.string.hi)+" "+
//                firstName+", "+sharedPreferences.getString("gymname",getString(R.string.your_gym))+" "+getString(R.string.welcome_whatsapp_qr_code)+clientId+getString(R.string.asterisc_to_bolden_whatsapp));
//        startActivity(sendIntent);
//    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(ClientProfileActivity.this,SearchActivity.class);
        startActivity(i);
    }
}
