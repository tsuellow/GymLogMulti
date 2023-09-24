package com.example.android.gymlogmulti;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.gymlogmulti.data.Receptionist;
import com.example.android.gymlogmulti.utils.PhoneUtilities;

import java.util.ArrayList;
import java.util.Arrays;

public class ReceptionistAdapter extends RecyclerView.Adapter<ReceptionistAdapter.ViewHolder>{

    public ArrayList<Receptionist> mReceptionists=new ArrayList<>();
    Context mContext;
    SharedPreferences sharedPreferences;
    public Boolean isAuth=false;

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView mUserName;
        private Button mSend;
        private Button mDelete;

        public ViewHolder(View itemView){
            super(itemView);
            mUserName= (TextView) itemView.findViewById(R.id.login_name);
            mSend=(Button) itemView.findViewById(R.id.bt_send);
            mDelete=(Button) itemView.findViewById(R.id.bt_delete);
        }
    }

    public ReceptionistAdapter(Context context){
        mContext=context;
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        String superBlob=sharedPreferences.getString("receptionists","");
        assert superBlob != null;
        if (!superBlob.isEmpty())
        setList(decodeList(superBlob));
    }

    private ArrayList<Receptionist> decodeList(String superblob) {
        String[] recepBlobs=superblob.split("###");
        ArrayList<String> recepBlobList =new ArrayList<String>(Arrays.asList(recepBlobs));
        ArrayList<Receptionist> result=new ArrayList<>();
        for (String blob:recepBlobList){
            result.add(new Receptionist(blob));
        }
        return result;
    }

    public String encodeList(){
        StringBuilder result= new StringBuilder();
        for (Receptionist recep:mReceptionists){
            result.append(recep.toBlob());
            result.append("###");
        }
        return result.toString();
    }

    public void commitChanges(){
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("receptionists",encodeList());
        editor.apply();
    }

    public void addRecep(Receptionist recep){
        mReceptionists.add(recep);
        commitChanges();
        Log.d("kerson",recep.toBlob());
        notifyDataSetChanged();
    }

    public void setList(ArrayList<Receptionist> receptionists){
        mReceptionists=receptionists;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReceptionistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(mContext);

        //now inflate the view
        View receptionistsView =inflater.inflate(R.layout.receptionist_adapter,parent,false);

        ViewHolder viewHolder=new ViewHolder(receptionistsView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ReceptionistAdapter.ViewHolder holder, int position) {
        Receptionist recep=mReceptionists.get(position);

        Log.d("kerson",recep.toBlob()+position);
        holder.mUserName.setText(recep.userName);
        holder.mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReceptionists.remove(recep);
                commitChanges();
                notifyDataSetChanged();
            }
        });

        holder.mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWhatsAppContact(recep);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (mReceptionists==null){
            return  0;
        }else {
            return mReceptionists.size();
        }
    }

    public void openWhatsAppContact(Receptionist recep){
        try {
            String toNumber= PhoneUtilities.depuratePhone(recep.phone);
            toNumber=toNumber.replaceFirst("^0+(?!$)", "");
            String text = "Con este usuario podes acceder a la interfaz remota de pagos: https://gymlog-registration.netlify.app/login \n\nusuario: *"+recep.userName+"* \ncontraseña: *"+recep.password+"* " +
                    "\n\n_Recordá borrar este mensaje de la tablet una vez recibido_";// Replace with your message.
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+text));
            mContext.startActivity(intent);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
