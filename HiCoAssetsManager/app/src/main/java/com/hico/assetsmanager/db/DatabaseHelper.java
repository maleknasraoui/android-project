package com.hico.assetsmanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hico.assetsmanager.model.Bien;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "hico_assets.db";
    public static final int DB_VERSION = 2;
    public static final String TABLE_BIENS = "biens";
    public static final String TABLE_USERS = "users";

    public static final String COL_ID = "id";
    public static final String COL_NOM = "nom";
    public static final String COL_TYPE = "type";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_VALEUR = "valeur";
    public static final String COL_DATE_CREATION = "date_creation";
    public static final String COL_DATE_MODIFICATION = "date_modification";
    public static final String COL_DATE_CONSULTATION = "date_consultation";
    public static final String COL_DATE_SUPPRESSION = "date_suppression";
    public static final String USER_ID = "id";
    public static final String USER_USERNAME = "username";
    public static final String USER_PASSWORD = "password";
    public static final String USER_ROLE = "role";
    public static final String USER_STATUS = "status";
    public static final String USER_DATE_REQUEST = "date_request";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_BIENS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NOM + " TEXT, " +
                COL_TYPE + " TEXT, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_VALEUR + " REAL, " +
                COL_DATE_CREATION + " TEXT, " +
                COL_DATE_MODIFICATION + " TEXT, " +
                COL_DATE_CONSULTATION + " TEXT, " +
                COL_DATE_SUPPRESSION + " TEXT)");
        createUsersTable(db);

        insertDemo(db, "Ordinateur Dell", "Mobilier", "Poste informatique de bureau", 1800);
        insertDemo(db, "Bureau Tunis", "Immobilier", "Local administratif principal", 95000);
        insertDefaultUsers(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            createUsersTable(db);
            insertDefaultUsers(db);
        }
    }

    public long insertBien(Bien bien) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = valuesFor(bien);
        String now = now();
        values.put(COL_DATE_CREATION, now);
        values.put(COL_DATE_MODIFICATION, now);
        return db.insert(TABLE_BIENS, null, values);
    }

    public int updateBien(Bien bien) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = valuesFor(bien);
        values.put(COL_DATE_MODIFICATION, now());
        return db.update(TABLE_BIENS, values, COL_ID + "=?", new String[]{String.valueOf(bien.getId())});
    }

    public int deleteBien(int id) {
        ContentValues values = new ContentValues();
        values.put(COL_DATE_SUPPRESSION, now());
        return getWritableDatabase().update(TABLE_BIENS, values, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public List<Bien> getAllBiens() {
        return getBiens(null, null);
    }

    public List<Bien> getBienByName(String name) {
        return getBiens(COL_NOM + " LIKE ? AND " + COL_DATE_SUPPRESSION + " IS NULL",
                new String[]{"%" + name + "%"});
    }

    public Bien getBienById(int id) {
        List<Bien> biens = getBiens(COL_ID + "=?", new String[]{String.valueOf(id)});
        if (biens.isEmpty()) return null;
        markConsulted(id);
        return biens.get(0);
    }

    public void markConsulted(int id) {
        ContentValues values = new ContentValues();
        values.put(COL_DATE_CONSULTATION, now());
        getWritableDatabase().update(TABLE_BIENS, values, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public Cursor queryActiveBiens() {
        return getReadableDatabase().query(TABLE_BIENS, null,
                COL_DATE_SUPPRESSION + " IS NULL", null, null, null, COL_ID + " DESC");
    }

    public String getLastDatesText() {
        SQLiteDatabase db = getReadableDatabase();
        StringBuilder text = new StringBuilder("HiCo dates:\n");
        text.append("Creation: ").append(max(db, COL_DATE_CREATION)).append("\n");
        text.append("Modification: ").append(max(db, COL_DATE_MODIFICATION)).append("\n");
        text.append("Consultation: ").append(max(db, COL_DATE_CONSULTATION)).append("\n");
        text.append("Suppression: ").append(max(db, COL_DATE_SUPPRESSION));
        return text.toString();
    }

    public String getMaxSavedDate() {
        SQLiteDatabase db = getReadableDatabase();
        String latest = "";
        for (String col : new String[]{COL_DATE_CREATION, COL_DATE_MODIFICATION, COL_DATE_CONSULTATION, COL_DATE_SUPPRESSION}) {
            String value = max(db, col);
            if (value.equals("Aucune")) continue;
            if (value.compareTo(latest) > 0) latest = value;
        }
        return latest.isEmpty() ? "Aucune" : latest;
    }

    public long requestUser(String username, String password, String role) {
        if (getUserCount(username) > 0) return -1;
        ContentValues values = new ContentValues();
        values.put(USER_USERNAME, username);
        values.put(USER_PASSWORD, password);
        values.put(USER_ROLE, role);
        values.put(USER_STATUS, "pending");
        values.put(USER_DATE_REQUEST, now());
        return getWritableDatabase().insert(TABLE_USERS, null, values);
    }

    public String loginUser(String username, String password) {
        Cursor cursor = getReadableDatabase().query(TABLE_USERS, null,
                USER_USERNAME + "=? AND " + USER_PASSWORD + "=? AND " + USER_STATUS + "='approved'",
                new String[]{username, password}, null, null, null);
        String role = null;
        if (cursor.moveToFirst()) role = cursor.getString(cursor.getColumnIndexOrThrow(USER_ROLE));
        cursor.close();
        return role;
    }

    public Cursor getPendingUsers() {
        return getReadableDatabase().query(TABLE_USERS, null, USER_STATUS + "='pending'",
                null, null, null, USER_ID + " DESC");
    }

    public int updateUserStatus(int userId, String status) {
        ContentValues values = new ContentValues();
        values.put(USER_STATUS, status);
        return getWritableDatabase().update(TABLE_USERS, values, USER_ID + "=?",
                new String[]{String.valueOf(userId)});
    }

    private List<Bien> getBiens(String selection, String[] args) {
        List<Bien> biens = new ArrayList<>();
        String finalSelection = selection;
        if (finalSelection == null) finalSelection = COL_DATE_SUPPRESSION + " IS NULL";
        Cursor cursor = getReadableDatabase().query(TABLE_BIENS, null, finalSelection, args, null, null, COL_ID + " DESC");
        while (cursor.moveToNext()) {
            biens.add(fromCursor(cursor));
        }
        cursor.close();
        return biens;
    }

    private Bien fromCursor(Cursor c) {
        return new Bien(
                c.getInt(c.getColumnIndexOrThrow(COL_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_NOM)),
                c.getString(c.getColumnIndexOrThrow(COL_TYPE)),
                c.getString(c.getColumnIndexOrThrow(COL_DESCRIPTION)),
                c.getDouble(c.getColumnIndexOrThrow(COL_VALEUR)),
                c.getString(c.getColumnIndexOrThrow(COL_DATE_CREATION)),
                c.getString(c.getColumnIndexOrThrow(COL_DATE_MODIFICATION)),
                c.getString(c.getColumnIndexOrThrow(COL_DATE_CONSULTATION)),
                c.getString(c.getColumnIndexOrThrow(COL_DATE_SUPPRESSION))
        );
    }

    private ContentValues valuesFor(Bien bien) {
        ContentValues values = new ContentValues();
        values.put(COL_NOM, bien.getNom());
        values.put(COL_TYPE, bien.getType());
        values.put(COL_DESCRIPTION, bien.getDescription());
        values.put(COL_VALEUR, bien.getValeur());
        return values;
    }

    private void insertDemo(SQLiteDatabase db, String nom, String type, String description, double valeur) {
        ContentValues values = new ContentValues();
        values.put(COL_NOM, nom);
        values.put(COL_TYPE, type);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_VALEUR, valeur);
        values.put(COL_DATE_CREATION, now());
        values.put(COL_DATE_MODIFICATION, now());
        db.insert(TABLE_BIENS, null, values);
    }

    private void createUsersTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USER_USERNAME + " TEXT UNIQUE, " +
                USER_PASSWORD + " TEXT, " +
                USER_ROLE + " TEXT, " +
                USER_STATUS + " TEXT, " +
                USER_DATE_REQUEST + " TEXT)");
    }

    private void insertDefaultUsers(SQLiteDatabase db) {
        insertUserIfMissing(db, "admin", "admin123", "admin", "approved");
        insertUserIfMissing(db, "user", "user123", "user", "approved");
    }

    private void insertUserIfMissing(SQLiteDatabase db, String username, String password, String role, String status) {
        Cursor cursor = db.query(TABLE_USERS, null, USER_USERNAME + "=?",
                new String[]{username}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        if (exists) return;

        ContentValues values = new ContentValues();
        values.put(USER_USERNAME, username);
        values.put(USER_PASSWORD, password);
        values.put(USER_ROLE, role);
        values.put(USER_STATUS, status);
        values.put(USER_DATE_REQUEST, now());
        db.insert(TABLE_USERS, null, values);
    }

    private int getUserCount(String username) {
        Cursor cursor = getReadableDatabase().query(TABLE_USERS, null, USER_USERNAME + "=?",
                new String[]{username}, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    private String max(SQLiteDatabase db, String column) {
        Cursor c = db.rawQuery("SELECT MAX(" + column + ") FROM " + TABLE_BIENS, null);
        String value = "Aucune";
        if (c.moveToFirst() && c.getString(0) != null) value = c.getString(0);
        c.close();
        return value;
    }

    public static String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}
