package com.example.android.gymlogmulti.data;


import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import android.content.Context;
import android.util.Log;

@Database(entities = {ClientEntry.class, PaymentEntry.class, VisitEntry.class},version = 10,exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class GymDatabase extends RoomDatabase {




    private static final String LOG_TAG = GymDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DB_NAME = "GymData.db";
    private static GymDatabase sInstance;


    static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE payment " +
                    "ADD COLUMN exchangeRate REAL DEFAULT 32.5 NOT NULL");
            database.execSQL("ALTER TABLE payment " +
                    "ADD COLUMN currency TEXT DEFAULT 'USD'");
            database.execSQL("ALTER TABLE payment " +
                    "ADD COLUMN comment TEXT");
            database.execSQL("ALTER TABLE payment " +
                    "ADD COLUMN extra TEXT");
        }
    };

    static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE payment " +
                    "ADD COLUMN dayOfWeek TEXT ");
            database.execSQL("ALTER TABLE payment " +
                    "ADD COLUMN branch TEXT DEFAULT 'UNIQUE'");
            database.execSQL("ALTER TABLE visit " +
                    "ADD COLUMN branch TEXT DEFAULT 'UNIQUE'");
        }
    };

//    static final Migration MIGRATION_9_10 = new Migration(9, 10) {
//        @Override
//        public void migrate(SupportSQLiteDatabase database) {
//            database.execSQL("PRAGMA foreign_keys=off;");
//            database.execSQL("BEGIN TRANSACTION;");
//            database.execSQL("ALTER TABLE visit RENAME TO _visit_old;");
//            database.execSQL("ALTER TABLE payment RENAME TO _payment_old;");
//            database.execSQL("CREATE TABLE visit" +
//                    "( id TEXT PRIMARY KEY," +
//                    "  last_name VARCHAR NOT NULL," +
//                    "  first_name VARCHAR," +
//                    "  hire_date DATE);");
//        }
//    };

    public static GymDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                Log.d(LOG_TAG, "Creating new database instance");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        GymDatabase.class, GymDatabase.DB_NAME)
                        //.allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
//                        .addMigrations(MIGRATION_7_8)
//                        .addMigrations(MIGRATION_8_9)
                        .build();
            }
        }
        Log.d(LOG_TAG, "Getting the database instance");
        return sInstance;
    }

    public  abstract ClientDao clientDao();
    public abstract PaymentDao paymentDao();
    public abstract VisitDao visitDao();





}
