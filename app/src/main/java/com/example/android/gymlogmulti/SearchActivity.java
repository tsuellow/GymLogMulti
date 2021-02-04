package com.example.android.gymlogmulti;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.android.gymlogmulti.data.ClientEntry;
import com.example.android.gymlogmulti.data.GymDatabase;
import com.example.android.gymlogmulti.data.PaymentEntry;
import com.example.android.gymlogmulti.data.VisitEntry;
import com.example.android.gymlogmulti.utils.SqliteExporter;

import java.io.IOException;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    public static final String SEARCH_STRING = "SEARCH_STRING";
    RecyclerView rvClients;
    GymDatabase mDb;
    SearchAdapter mAdapter;
    Context mContext;
    SearchView searchView;
    Toolbar mToolbar;
    String searchString;
    SharedPreferences sharedPreferences;
    PackageManager packageManager;
    static Context appContext;

    final static int REQUEST_CODE_READ=99;
    final static int REQUEST_CODE_WRITE=98;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //normal variable instantiation
        setContentView(R.layout.activity_search);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);


        mContext = getApplicationContext();
        rvClients = (RecyclerView) findViewById(R.id.rv_client_search);
        mAdapter = new SearchAdapter(this);
        mDb = GymDatabase.getInstance(getApplicationContext());

        rvClients.setAdapter(mAdapter);
        rvClients.setLayoutManager(new LinearLayoutManager(this));
        if (savedInstanceState==null){
            searchString = "";
        }else{
            searchString=savedInstanceState.getString(SEARCH_STRING);
        }

        populateDataSource(searchString);

        //searchView.setQuery("a",false);


        //floating + button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), NewClientActivity.class);
                startActivity(i);
            }
        });
        appContext=getApplicationContext();

        packageManager = this.getPackageManager();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(SearchActivity.this, MainActivity.class);
        startActivity(i);;
    }

    public static Context getAppCont(){
        return appContext;
    }

    private void populateDataSource(String s) {
        String str = s.replace(" ","%") + "%";

        final LiveData<List<ClientEntry>> clients = mDb.clientDao().getClientByName(str);
        clients.observe(this, new Observer<List<ClientEntry>>() {
            @Override
            public void onChanged(@Nullable List<ClientEntry> clientEntries) {
                mAdapter.setClients(clientEntries);
                mToolbar.setSubtitle(mAdapter.getItemCount()+" "+getString(R.string.clients));

            }
        });
    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_plus, menu);

        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));

        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(onQueryTextListener);
        searchView.setQuery(searchString,true);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.opt_setting:{
                Intent intent=new Intent(getApplicationContext(),SettingsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.opt_reminder:{
                //Toast.makeText(getApplicationContext(),"open reminder list",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(getApplicationContext(),SendReminderActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.opt_backup:{
                Toast.makeText(getApplicationContext(), R.string.backup_in_process,Toast.LENGTH_SHORT).show();
                final DataBackup dataBackup=new DataBackup(SearchActivity.this,sharedPreferences);
                if (dataBackup.hasInternetConnectivity()){
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                                final List<ClientEntry> clients=mDb.clientDao().getClientToBeSynced();
                                final List<PaymentEntry> payments=mDb.paymentDao().getPaymentToBeSynced();
                                final List<VisitEntry> visits=mDb.visitDao().getVisitToBeSynced();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dataBackup.syncData(clients,payments,visits);
                                    }
                                });
                        }
                    });

                }else {
                    Toast.makeText(getApplicationContext(), R.string.no_internet,Toast.LENGTH_LONG).show();

                }
                break;
            }

            case R.id.opt_csv_export:{

                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        SQLiteDatabase mydatabase = SQLiteDatabase.openDatabase(mDb.getOpenHelper().getWritableDatabase().getPath(),null,0);
                        SqliteExporter sqliteExporter=new SqliteExporter(getApplicationContext());
                        try {
                            final String path = sqliteExporter.export(mydatabase);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    csvDialog(path);
                                }
                            });

                        }catch (IOException e){
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mContext, R.string.csv_error,Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });

                break;
            }

        }
        return super.onOptionsItemSelected(item);
    }

    private void csvDialog(final String path){
        androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog alertDialog=builder.create();

        alertDialog.setTitle(getString(R.string.csv_created));
        alertDialog.setMessage(getString(R.string.csv_created_msg));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.open), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(path);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, "resource/folder");
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(Intent.createChooser(intent, getString(R.string.open_folder)));
                } else {
                    Toast.makeText(mContext, R.string.no_app_available,Toast.LENGTH_LONG).show();
                }
                alertDialog.dismiss();

            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private SearchView.OnQueryTextListener onQueryTextListener =
            new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    populateDataSource(query);
                    searchView.clearFocus();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    populateDataSource(newText);
                    searchString=newText;

                    return true;
                }
            };

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putString(SEARCH_STRING, searchString);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }





}

