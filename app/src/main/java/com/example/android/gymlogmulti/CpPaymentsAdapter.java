package com.example.android.gymlogmulti;

import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.gymlogmulti.data.DateConverter;
import com.example.android.gymlogmulti.data.GymDatabase;
import com.example.android.gymlogmulti.data.PaymentEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CpPaymentsAdapter extends RecyclerView.Adapter<CpPaymentsAdapter.ViewHolder> {


    //this defines the viewholder and finds and holds on to all its elements
    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView mFrom, mUntil, mProduct, mAmount;
        private Button mDelete;
        private LinearLayout mBackground;

        public ViewHolder(View itemView){
            super(itemView);

            mBackground= (LinearLayout) itemView.findViewById(R.id.ll_background_cp);
            mFrom= (TextView) itemView.findViewById(R.id.tv_from_cp_pay);
            mUntil= (TextView) itemView.findViewById(R.id.tv_to_cp_pay);
            mProduct = (TextView) itemView.findViewById(R.id.tv_product_cp_pay);
            mAmount=(TextView) itemView.findViewById(R.id.tv_amount_cp_pay);
            mDelete=(Button) itemView.findViewById(R.id.bt_delete_cp_pay);
        }
    }

    private List<PaymentEntry> mPayments;
    private Context mContext;
    private GymDatabase mDb;
    public CpPaymentsAdapter(Context context, GymDatabase database){
        mDb=database;
        mContext=context;
    }

    //define method to get and set current data source. usefull when clicking on adapter item
    public void setPayments(List<PaymentEntry> payments){
        mPayments=payments;
        notifyDataSetChanged();
    }

    public List<PaymentEntry> getPayments(){
        return mPayments;
    }


    @NonNull
    @Override
    public CpPaymentsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater=LayoutInflater.from(mContext);

        //now inflate the view
        View paymentView=inflater.inflate(R.layout.adapter_cp_payments_view,viewGroup,false);

        CpPaymentsAdapter.ViewHolder viewHolder=new CpPaymentsAdapter.ViewHolder(paymentView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CpPaymentsAdapter.ViewHolder viewHolder, int i) {
        final PaymentEntry payment=mPayments.get(i);
        final String paymentId=payment.getId();

        //now find the tvs in the viewholder and assign them the correct text
        final String from = new SimpleDateFormat("yyyy-MM-dd").format(payment.getPaidFrom());
        final String until = new SimpleDateFormat("yyyy-MM-dd").format(payment.getPaidUntil());
        viewHolder.mFrom.setText(from);
        viewHolder.mUntil.setText(until);
        viewHolder.mProduct.setText(payment.getProduct());
        float cordobas=payment.getAmountUsd()*payment.getExchangeRate();
        final String amount="USD "+Math.round(payment.getAmountUsd())+" /C$ "+Math.round(cordobas);
        viewHolder.mAmount.setText(amount);

        final String dialogText=mContext.getString(R.string.payment_id)+" "+payment.getId()+ " \n"+
                mContext.getString(R.string.paid_at)+" "+DateConverter.getDateString(payment.getTimestamp()).substring(0,16)+ " \n"+
                mContext.getString(R.string.branch)+" "+payment.getBranch()+ " \n"+
                mContext.getString(R.string.amount)+" "+amount+" \n"+
                mContext.getString(R.string.currency_cap)+" "+payment.getCurrency()+" \n"+
                mContext.getString(R.string.product_cap)+" "+payment.getProduct()+" \n"+
                mContext.getString(R.string.from_cap)+" "+DateConverter.getDateString(payment.getPaidFrom()).substring(0,16)+ " \n"+
                mContext.getString(R.string.to_cap)+" "+DateConverter.getDateString(payment.getPaidUntil()).substring(0,16)+ " \n"+
                mContext.getString(R.string.days_of_week)+" "+(payment.getDayOfWeek()!=null?payment.getDayOfWeek().replace("1",mContext.getString(R.string.sun))
                    .replace("2",mContext.getString(R.string.mon)).replace("3",mContext.getString(R.string.tue)).replace("4",mContext.getString(R.string.wed))
                    .replace("5",mContext.getString(R.string.thu)).replace("6",mContext.getString(R.string.fri)).replace("7",mContext.getString(R.string.sat)):"-")+ " \n"+
                mContext.getString(R.string.comment_cap)+" "+payment.getComment();


        viewHolder.mBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(mContext);
                final AlertDialog alertDialog=builder.create();
                alertDialog.setTitle(mContext.getString(R.string.payment_info));
                alertDialog.setMessage(dialogText);
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, mContext.getString(R.string.close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();

            }
        });


        viewHolder.mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(mContext);
                final AlertDialog alertDialog=builder.create();
                alertDialog.setTitle(mContext.getString(R.string.attention));
                alertDialog.setMessage(mContext.getString(R.string.are_you_sure)+" "+amount+" "+mContext.getString(R.string.payment_from_period)+
                        " "+from+" "+mContext.getString(R.string.until)+" "+until+" "+mContext.getString(R.string.question_marc));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String extra=payment.getExtra();
                        if(extra==null){extra="";}
                        extra=mContext.getString(R.string.invalidated_at)+" "+ DateConverter.getDateString(new Date())+", "+ extra;
                        payment.setExtra(extra);
                        invalidatePayment(payment);
                        alertDialog.dismiss();
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, mContext.getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();

            }
        });

    }

    @Override
    public int getItemCount() {
        if (mPayments==null){
            return  0;
        }else {
            return mPayments.size();
        }
    }

    private void invalidatePayment(final PaymentEntry payment){
        payment.setIsValid(0);
        if (payment.getSyncStatus()==1) {
            payment.setSyncStatus(2);
        }
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.paymentDao().updatePayment(payment);
            }
        });
    }
}
