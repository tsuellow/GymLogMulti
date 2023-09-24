package com.example.android.gymlogmulti;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.example.android.gymlogmulti.data.Receptionist;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceFragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.gymlogmulti.data.DateConverter;
import com.example.android.gymlogmulti.data.GymDatabase;
//import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import java.util.Date;

import static java.security.AccessController.getContext;

//import android.support.v7.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    EditTextPreference gymName, doorDuration;
    Preference changePin, changeOwnerPin, backupAll, restoreAll, backupTime, exchangeRate, singlePassMinus1, receptionists, singlePassMinus2, gymData, serverAddress;
    ListPreference preferredCurrency;
    SwitchPreference connectDoor, useProximitySensor;
    TimePickerDialog.OnTimeSetListener onTimeSetListenerBackup;
    GymDatabase mDb;

    SharedPreferences sharedPreferences;


    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.app_settings,rootKey);

        sharedPreferences=PreferenceManager.getDefaultSharedPreferences(getContext());
        mDb=GymDatabase.getInstance(getContext());

        gymName = (EditTextPreference) findPreference("gymname");
        gymData = (Preference) findPreference("gymdata");


        //alternative solution
        backupTime =(Preference) findPreference("timebackup");


        exchangeRate = (Preference) findPreference("usd2cs");
        exchangeRate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayValueDialog("usd2cs",exchangeRate.getTitle().toString(),getString(R.string.value_in_cs));
                return false;
            }
        });
        singlePassMinus1 = (Preference) findPreference("passminus1");
        singlePassMinus1.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayValueDialog("passminus1",singlePassMinus1.getTitle().toString(),getString(R.string.value_in_cs));
                return false;
            }
        });
        singlePassMinus2 = (Preference) findPreference("passminus2");
        singlePassMinus2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayValueDialog("passminus2",singlePassMinus2.getTitle().toString(),getString(R.string.value_in_cs));
                return false;
            }
        });
        backupAll=(Preference) findPreference("backupall");
        backupAll.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayConfirmDialog("backupall",backupAll.getTitle().toString());
                return false;
            }
        });
        restoreAll=(Preference) findPreference("recoverall");
        restoreAll.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayConfirmDialog("recoverall",restoreAll.getTitle().toString());
                return false;
            }
        });
        serverAddress=(Preference) findPreference("serveraddress");
        serverAddress.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayServerAddressDialog();
                return false;
            }
        });

        changePin=(Preference) findPreference("changepin");
        changePin.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayPinDialog("changepin",changePin.getTitle().toString());
                return false;
            }
        });

        changeOwnerPin=(Preference) findPreference("changeownerpin");
        changeOwnerPin.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayPinDialog("changeownerpin",changeOwnerPin.getTitle().toString());
                return false;
            }
        });

        receptionists=(Preference) findPreference("receptionists");
        receptionists.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayReceptionistValidationDialog();
                return false;
            }
        });

        preferredCurrency = (ListPreference) findPreference("preferredcurrency");

        connectDoor=(SwitchPreference) findPreference("doorconnect") ;
        useProximitySensor=(SwitchPreference) findPreference("doorproximity");
        doorDuration=(EditTextPreference) findPreference("doorduration");
        if (sharedPreferences.getBoolean("doorconnect",false)){
            useProximitySensor.setEnabled(true);
            doorDuration.setEnabled(true);
        }else{
            useProximitySensor.setEnabled(false);
            doorDuration.setEnabled(false);
        }
        connectDoor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((boolean) newValue){
                    ((SwitchPreference)preference).setChecked(true);
                    useProximitySensor.setEnabled(true);
                    doorDuration.setEnabled(true);
                }else{
                    useProximitySensor.setEnabled(false);
                    doorDuration.setEnabled(false);
                }

                return connectDoor.isChecked();
            }
        });



        setPrefSummary();
    }

    private void displayPinDialog(final String key, String title){
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(getContext());
        View mView=getLayoutInflater().inflate(R.layout.dialog_change_pin,null);
        final EditText mOldPin=(EditText) mView.findViewById(R.id.ev_old_pin);
        TextInputLayout loOldPin=(TextInputLayout) mView.findViewById(R.id.lo_old_pin);
        final EditText mNewPin1=(EditText) mView.findViewById(R.id.ev_new_pin1);
        TextInputLayout loNewPin1=(TextInputLayout) mView.findViewById(R.id.lo_new_pin1);
        final EditText mNewPin2=(EditText) mView.findViewById(R.id.ev_new_pin2);
        TextInputLayout loNewPin2=(TextInputLayout) mView.findViewById(R.id.lo_new_pin2);
        Button mOk=(Button) mView.findViewById(R.id.btn_ok_pin);
        Button mCancel=(Button) mView.findViewById(R.id.btn_cancel_pin);
        mBuilder.setTitle(title);
        mBuilder.setView(mView);

        final AlertDialog dialog=mBuilder.create();

        //button to dismiss dialog
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPin=mOldPin.getText().toString();
                String newPin1=mNewPin1.getText().toString();
                String newPin2=mNewPin2.getText().toString();
                String currPin=sharedPreferences.getString(key,"1234");
                String currOwnerPin=sharedPreferences.getString("changeownerpin","1234");
                if((currPin.contentEquals(oldPin)||currOwnerPin.contentEquals(oldPin)) && newPin1.contentEquals(newPin2)){
                    if (newPin1.length()==4){
                        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(getContext());
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putString(key,newPin1);
                        Date now= new Date();
                        if (key.contentEquals("changeownerpin")){
                            editor.putLong("pindate",now.getTime());
                        }
                        editor.apply();
                        dialog.dismiss();
                        Toast.makeText(getContext(), R.string.pin_change_success,Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getContext(), R.string.error_pin_too_long,Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getContext(),getString(R.string.error_pin_wrong),Toast.LENGTH_SHORT).show();
                }
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

    private void displayServerAddressDialog(){
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(getContext());
        View mView=getLayoutInflater().inflate(R.layout.dialog_change_server_address,null);
        final EditText mOwnerPin=(EditText) mView.findViewById(R.id.admin_pin);
        TextInputLayout loOwnerPin=(TextInputLayout) mView.findViewById(R.id.lo_admin_pin);
        final EditText mSettingValue=(EditText) mView.findViewById(R.id.server_address);
        TextInputLayout loSettingValue=(TextInputLayout) mView.findViewById(R.id.lo_server_address);
        String currAddress=sharedPreferences.getString("serveraddress","https://www.id-ex.de/GymLogMulti/php/");
        mSettingValue.setText(currAddress);


        Button mOk=(Button) mView.findViewById(R.id.btn_ok_pin);
        Button mCancel=(Button) mView.findViewById(R.id.btn_cancel_pin);
        mBuilder.setTitle(R.string.change_server);
        mBuilder.setView(mView);

        final AlertDialog dialog=mBuilder.create();

        //button to dismiss dialog
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ownerPin=mOwnerPin.getText().toString();
                String settingValue=mSettingValue.getText().toString();
                if(ownerPin.contentEquals("2987")){
                    try{
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putString("serveraddress",settingValue);
                        editor.apply();
                        dialog.dismiss();
                        Toast.makeText(getContext(), R.string.server_changed,Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        Toast.makeText(getContext(), R.string.error_server_change,Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getContext(), R.string.wrong_admin_pin,Toast.LENGTH_SHORT).show();
                }
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

    private void displayValueDialog(final String key, String title, String inputText){
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(getContext());
        View mView=getLayoutInflater().inflate(R.layout.dialog_change_owner_setting,null);
        final EditText mOwnerPin=(EditText) mView.findViewById(R.id.owner_pin);
        TextInputLayout loOwnerPin=(TextInputLayout) mView.findViewById(R.id.lo_owner_pin);
        final EditText mSettingValue=(EditText) mView.findViewById(R.id.setting_value);

        TextInputLayout loSettingValue=(TextInputLayout) mView.findViewById(R.id.lo_setting_value);
        loSettingValue.setHint(inputText);

        Button mOk=(Button) mView.findViewById(R.id.btn_ok_pin);
        Button mCancel=(Button) mView.findViewById(R.id.btn_cancel_pin);
        mBuilder.setTitle(title);
        mBuilder.setView(mView);

        final AlertDialog dialog=mBuilder.create();

        //button to dismiss dialog
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ownerPin=mOwnerPin.getText().toString();
                String settingValue=mSettingValue.getText().toString();
                String currPin=sharedPreferences.getString("changeownerpin","1234");
                if(currPin.contentEquals(ownerPin)){
                    try{
                        Float.valueOf(settingValue);
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putString(key,settingValue);
                        editor.apply();
                        dialog.dismiss();
                        Toast.makeText(getContext(), R.string.value_success,Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        Toast.makeText(getContext(), R.string.value_has_to_be_numeric,Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getContext(), R.string.wrong_owner_pin,Toast.LENGTH_SHORT).show();
                }
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

    private void displayReceptionistValidationDialog(){
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(getContext());
        View mView=getLayoutInflater().inflate(R.layout.dialog_validation,null);
        final EditText mOwnerPin=(EditText) mView.findViewById(R.id.admin_pin);
        String currPin=sharedPreferences.getString("changeownerpin","1234");
        Button mOk=(Button) mView.findViewById(R.id.btn_ok_pin);
        Button mCancel=(Button) mView.findViewById(R.id.btn_cancel_pin);
        mBuilder.setTitle(R.string.recep_title);
        mBuilder.setView(mView);

        final AlertDialog dialog=mBuilder.create();

        //button to dismiss dialog
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOwnerPin.getText().toString().contentEquals(currPin)){
                    displayReceptionistDialog();
                    dialog.dismiss();
                }else{
                    Toast.makeText(getContext(), R.string.wrong_owner_pin,Toast.LENGTH_SHORT).show();
                }

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

    private void displayReceptionistDialog(){
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(getContext());
        View mView=getLayoutInflater().inflate(R.layout.dialog_add_receptionist,null);

        RecyclerView rvReceptionists=(RecyclerView) mView.findViewById(R.id.rv_receptionists);
        ReceptionistAdapter adapter=new ReceptionistAdapter(getContext());
        rvReceptionists.setAdapter(adapter);
        rvReceptionists.setLayoutManager(new LinearLayoutManager(getContext()));

        EditText username=(EditText) mView.findViewById(R.id.username);
        EditText phone=(EditText) mView.findViewById(R.id.phone);

        Button mOk=(Button) mView.findViewById(R.id.btn_ok_pin);
        Button mCancel=(Button) mView.findViewById(R.id.btn_cancel_pin);
        mBuilder.setTitle(R.string.recep_title);
        mBuilder.setView(mView);

        final AlertDialog dialog=mBuilder.create();

        //button to dismiss dialog
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name=username.getText().toString();
                String tele=phone.getText().toString();
                    if (name.matches("[a-zA-Z]+")){
                        if(!tele.isEmpty()){
                            adapter.addRecep(new Receptionist(name, tele));
                            username.setText("");
                            phone.setText("");
                        }else{
                            Toast.makeText(getContext(), R.string.empty_phone,Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getContext(), R.string.wrong_user,Toast.LENGTH_SHORT).show();
                    }

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

    private void displayConfirmDialog(final String key, String title){
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(getContext());
        View mView=getLayoutInflater().inflate(R.layout.dialog_confirm_owner_setting,null);
        final EditText mOwnerPin=(EditText) mView.findViewById(R.id.owner_pin);
        TextInputLayout loOwnerPin=(TextInputLayout) mView.findViewById(R.id.lo_owner_pin);

        Button mOk=(Button) mView.findViewById(R.id.btn_confirm);
        Button mCancel=(Button) mView.findViewById(R.id.btn_cancel_pin);
        mBuilder.setTitle(title);
        mBuilder.setView(mView);

        final AlertDialog dialog=mBuilder.create();

        //button to dismiss dialog
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ownerPin=mOwnerPin.getText().toString();
                String currPin=sharedPreferences.getString("changeownerpin","1234");
                if(currPin.contentEquals(ownerPin)){
                    if (key.contentEquals("backupall")){
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                    mDb.clientDao().backupResetClientSyncStatus();
                                    mDb.paymentDao().backupResetPaymentSyncStatus();
                                    mDb.visitDao().backupResetVisitSyncStatus();
                            }
                        });
                        Toast.makeText(getContext(), R.string.toast_backup_all,Toast.LENGTH_SHORT).show();
                    }else if (key.contentEquals("recoverall")){
                        DataBackup dataBackup=new DataBackup(getContext(),sharedPreferences);
                        dataBackup.restoreAll();
                    }
                    dialog.dismiss();

                }else{
                    Toast.makeText(getContext(), R.string.wrong_owner_pin,Toast.LENGTH_SHORT).show();
                }
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


    private void setPrefSummary(){
        gymName.setSummary(gymName.getText());
        String gymDataText=getString(R.string.company_name)+" "+MainActivity.COMPANY_NAME+ " \n"+
                getString(R.string.company_owner)+" "+MainActivity.COMPANY_OWNER+ " \n"+
                getString(R.string.branch)+" "+MainActivity.GYM_BRANCH;
        gymData.setSummary(gymDataText);
        exchangeRate.setSummary("C$ "+sharedPreferences.getString("usd2cs","1"));
        singlePassMinus1.setSummary("C$ "+sharedPreferences.getString("passminus1","0"));
        singlePassMinus2.setSummary("C$ "+sharedPreferences.getString("passminus2","0"));
        backupTime.setSummary(getString(R.string.each_full_hour)+" "+ DateConverter.getDateString(new Date(sharedPreferences.getLong("timebackup",0))).substring(14,16)+" "+getString(R.string.minutes));
        //languagePref.setSummary(languagePref.getEntry());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        setPrefSummary();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
