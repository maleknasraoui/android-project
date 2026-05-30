package com.example.assetmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "AssetManager.db";
    private static final int DATABASE_VERSION = 1;

    // Table Biens
    public static final String TABLE_BIENS = "Biens";
    public static final String COL_ID = "id";
    public static final String COL_NOM = "nom";
    public static final String COL_TYPE = "type";
    public static final String COL_VALEUR = "valeur";
    public static final String COL_DESC = "description";

    // Table Historique
    public static final String TABLE_HISTO = "Historique";
    public static final String COL_H_ID = "id";
    public static final String COL_H_ACTION = "action";
    public static final String COL_H_DATE = "date_action";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_BIENS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NOM + " TEXT, " +
                COL_TYPE + " TEXT, " +
                COL_VALEUR + " REAL, " +
                COL_DESC + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_HISTO + " (" +
                COL_H_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_H_ACTION + " TEXT, " +
                COL_H_DATE + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BIENS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTO);
        onCreate(db);
    }

    // --- Méthodes de log ---
    public void logAction(String action) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_H_ACTION, action);
        cv.put(COL_H_DATE, System.currentTimeMillis());
        db.insert(TABLE_HISTO, null, cv);
    }

    public long getLastActionTime() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(" + COL_H_DATE + ") FROM " + TABLE_HISTO, null);
        long time = 0;
        if (cursor.moveToFirst()) {
            time = cursor.getLong(0);
        }
        cursor.close();
        return time;
    }

    public String getFormattedLastActions() {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder sb = new StringBuilder();
        String[] actions = {"CREATION", "MODIFICATION", "CONSULTATION", "SUPPRESSION"};
        
        for (String act : actions) {
            Cursor c = db.rawQuery("SELECT MAX(" + COL_H_DATE + ") FROM " + TABLE_HISTO + " WHERE " + COL_H_ACTION + " = ?", new String[]{act});
            if (c.moveToFirst() && c.getLong(0) > 0) {
                sb.append(act).append(": ").append(new java.util.Date(c.getLong(0)).toString()).append("\n");
            } else {
                sb.append(act).append(": Jamais\n");
            }
            c.close();
        }
        return sb.toString();
    }
}
