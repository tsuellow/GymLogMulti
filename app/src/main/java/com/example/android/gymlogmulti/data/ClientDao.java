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
public interface ClientDao {

    //test
    @Query("SELECT count(*) FROM client")
    int getCount();
    //test
    @Query("SELECT case when num>=1 then 0 else 1 end test FROM (select count(*) num from client where id=:id) ")
    int isIdNew(int id);

    @Query("SELECT * FROM client ORDER BY firstName")
    LiveData<List<ClientEntry>> getAllClients();

    @Query("SELECT * FROM client WHERE id=:id")
    LiveData<ClientEntry> getClientById(int id);

    @Query("SELECT * FROM client WHERE id=:id")
    ClientEntry getClientByIdAsync(int id);

    @Query("SELECT * FROM client WHERE firstName||lastName LIKE :namePart " +
            "OR lastName LIKE :namePart OR CAST(id as TEXT) LIKE :namePart ORDER BY firstName ASC")
    LiveData<List<ClientEntry>> getClientByName(String namePart);

    @Query("SELECT * FROM client WHERE id>0 AND (firstName||lastName LIKE :namePart " +
            "OR lastName LIKE :namePart OR CAST(id as TEXT) LIKE :namePart) ORDER BY firstName ASC")
    LiveData<List<ClientEntry>> getRegularClientByName(String namePart);

    @Query("SELECT * FROM client WHERE syncStatus!=1")
    List<ClientEntry> getClientToBeSynced();

    @Query("SELECT C.* FROM (SELECT clientId, MAX(paidUntil) AS paidUntil " +
            "FROM payment WHERE isValid=1 AND clientId>0 and branch=:branch GROUP BY clientId) AS P " +
            "LEFT JOIN client AS C ON P.clientId=C.id WHERE substr(paidUntil,1,10)=substr(:date,1,10)  ORDER BY C.firstName ASC")
    LiveData<List<ClientEntry>> getPaymentDueClients(Date date, String branch);

    @Query("SELECT C.id, C.firstName, C.lastName, C.photo, V.timestamp from " +
            "(select * from (SELECT clientId, max(timestamp) AS timestamp FROM " +
            "visit WHERE access='G' AND timestamp>:queryDate AND clientId>0 AND branch=:branch GROUP BY clientId) " +
            "UNION " +
            "SELECT clientId, timestamp FROM visit where timestamp>:queryDate AND clientId IN(-1,-2) AND branch=:branch) AS V " +
            "LEFT JOIN client AS C ON V.clientId=C.id " +
            "WHERE C.firstName LIKE :namePart " +
            "OR C.lastName LIKE :namePart " +
            "OR CAST(C.id as TEXT) LIKE :namePart " +
            "ORDER BY timestamp DESC")
    LiveData<List<ClientVisitJoin>> getCurrentClass(Date queryDate, String namePart, String branch);

    @Query("select min(n) from (select 1+e+d*10+c*100+b*1000+a*10000 as n from\n" +
            "(select 0 as a union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9)," +
            "(select 0 as b union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9)," +
            "(select 0 as c union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9)," +
            "(select 0 as d union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9)," +
            "(select 0 as e union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9))" +
            "where n >5000 " +
            "and n not in (select id from client);")
    int autogenerateId();

    @Query("UPDATE client SET syncStatus=1 WHERE syncStatus!=1")
    void bulkUpdateClientSyncStatus();

    @Query("UPDATE client SET syncStatus=0 WHERE syncStatus!=0")
    void backupResetClientSyncStatus();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void restoreClient(ClientEntry clientEntry);

    @Insert
    void insertClient(ClientEntry clientEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateClient(ClientEntry clientEntry);

    @Delete
    void deleteClient(ClientEntry clientEntry);

}
