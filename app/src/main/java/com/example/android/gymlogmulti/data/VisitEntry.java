package com.example.android.gymlogmulti.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.example.android.gymlogmulti.MainActivity;

import java.util.Date;

@Entity(tableName = "visit", foreignKeys = @ForeignKey(
        entity = ClientEntry.class,
        parentColumns = "id",
        childColumns ="clientId" ),
        indices = {@Index(value = {"clientId"})})
public class VisitEntry {


    @PrimaryKey() @NonNull
    private String id= MainActivity.GYM_BRANCH+"_"+System.currentTimeMillis()/1000;
    private int clientId;
    private Date timestamp;
    private String access;
    private int syncStatus=0;
    private String branch;

    public VisitEntry(int clientId, Date timestamp, String access, String branch) {
        this.clientId = clientId;
        this.timestamp = timestamp;
        this.access = access;
        this.branch = branch;
    }

    @Ignore
    public VisitEntry(String id, int clientId, Date timestamp, String access, int syncStatus, String branch) {
        this.id = id;
        this.clientId = clientId;
        this.timestamp = timestamp;
        this.access = access;
        this.syncStatus = syncStatus;
        this.branch = branch;
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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }
}
