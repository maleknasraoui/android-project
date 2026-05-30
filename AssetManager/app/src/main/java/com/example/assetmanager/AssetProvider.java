package com.example.assetmanager;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AssetProvider extends ContentProvider {
    private DatabaseHelper dbHelper;
    private static final String AUTHORITY = "com.example.assetmanager.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/biens");

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        dbHelper.logAction("CONSULTATION"); // Consigne : Loguer la consultation
        return dbHelper.getReadableDatabase().query(DatabaseHelper.TABLE_BIENS, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "vnd.android.cursor.dir/vnd.com.example.assetmanager.biens";
    }

    // Le ContentProvider est en mode consultation uniquement selon l'énoncé
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) { return null; }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) { return 0; }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) { return 0; }
}
