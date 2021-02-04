package com.example.android.gymlogmulti.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

@Dao
public interface VisitDao {

    @Query("SELECT * FROM visit WHERE clientId=:clientId AND timestamp=(SELECT MAX(timestamp) FROM visit WHERE clientId=:clientId)")
    VisitEntry getLatestVisit(int clientId);

    @Query("SELECT * FROM visit WHERE clientId=:clientId ORDER BY timestamp DESC")
    LiveData<List<VisitEntry>> getVisitsByClient(int clientId);

    @Query("SELECT * FROM visit WHERE timestamp>=:lastHourMinus30")
    List<VisitEntry> getCurrentClass(Date lastHourMinus30);

    @Query("SELECT * FROM visit WHERE syncStatus!=1")
    List<VisitEntry> getVisitToBeSynced();

    @Query("UPDATE visit SET syncStatus=1 WHERE syncStatus!=1")
    void bulkUpdateVisitSyncStatus();

    @Query("UPDATE visit SET syncStatus=0 WHERE syncStatus!=0")
    void backupResetVisitSyncStatus();

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateVisit(VisitEntry visitEntry);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void restoreVisit(VisitEntry visitEntry);

    @Insert
    void insertVisit(VisitEntry visitEntry);

    @Delete
    void deleteVisit(VisitEntry visitEntry);

}
