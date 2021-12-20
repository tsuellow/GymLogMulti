package com.example.android.gymlogmulti.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import com.example.android.gymlogmulti.MainActivity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;


@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(tableName = "payment", foreignKeys = @ForeignKey(entity = ClientEntry.class,
        parentColumns = "id",
        childColumns = "clientId",
        onDelete = ForeignKey.NO_ACTION),
        indices = {@Index(value = {"clientId"})})
public class PaymentEntry {
    @PrimaryKey() @NonNull
    private String id= MainActivity.GYM_BRANCH+"_"+System.currentTimeMillis()/1000;
    private int clientId;
    private String product;
    private float amountUsd;
    private Date paidFrom;
    private Date paidUntil;
    private Date timestamp;
    private int isValid=1;
    private int syncStatus=0;
    private float exchangeRate;
    private String currency;
    private String comment;
    private String extra;
    private String dayOfWeek;
    private String branch;

    public PaymentEntry(int clientId, String product, float amountUsd, Date paidFrom, Date paidUntil, Date timestamp, float exchangeRate, String currency, String comment, String extra, String dayOfWeek, String branch) {
        this.clientId = clientId;
        this.product = product;
        this.amountUsd = amountUsd;
        this.paidFrom = paidFrom;
        this.paidUntil = paidUntil;
        this.timestamp = timestamp;
        this.exchangeRate = exchangeRate;
        this.currency = currency;
        this.comment = comment;
        this.extra = extra;
        this.dayOfWeek= dayOfWeek;
        this.branch = branch;
    }

    @Ignore
    public PaymentEntry(String id, int clientId, String product, float amountUsd, Date paidFrom, Date paidUntil, Date timestamp, int isValid, int syncStatus, float exchangeRate, String currency, String comment, String extra, String dayOfWeek, String branch) {
        this.id = id;
        this.clientId = clientId;
        this.product = product;
        this.amountUsd = amountUsd;
        this.paidFrom = paidFrom;
        this.paidUntil = paidUntil;
        this.timestamp = timestamp;
        this.isValid = isValid;
        this.syncStatus = syncStatus;
        this.exchangeRate = exchangeRate;
        this.currency = currency;
        this.comment = comment;
        this.extra = extra;
        this.dayOfWeek= dayOfWeek;
        this.branch = branch;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public float getAmountUsd() {
        return amountUsd;
    }

    public void setAmountUsd(float amountUsd) {
        this.amountUsd = amountUsd;
    }

    public Date getPaidFrom() {
        return paidFrom;
    }

    public void setPaidFrom(Date paidFrom) {
        this.paidFrom = paidFrom;
    }

    public Date getPaidUntil() {
        return paidUntil;
    }

    public void setPaidUntil(Date paidUntil) {
        this.paidUntil = paidUntil;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getIsValid() {
        return isValid;
    }

    public void setIsValid(int isValid) {
        this.isValid = isValid;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    public float getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(float exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}