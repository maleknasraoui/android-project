package com.example.assetmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Toast;
import android.util.Log;

public class AssetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseHelper db = new DatabaseHelper(context);
        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            // 1. Afficher les dates les plus récentes au démarrage
            String report = db.getFormattedLastActions();
            Toast.makeText(context, "États de la base au démarrage :\n" + report, Toast.LENGTH_LONG).show();
            Log.d("AssetReceiver", "Boot completed: " + report);
        } 
        else if (Intent.ACTION_TIME_CHANGED.equals(action) || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            // 2. Avertissement si changement de date vers le passé
            long lastDbTime = db.getLastActionTime();
            if (System.currentTimeMillis() < lastDbTime) {
                Toast.makeText(context, "ATTENTION : La date système est inférieure à la date de la dernière opération en base !", Toast.LENGTH_LONG).show();
            }
        } 
        else if ("com.example.assetmanager.SEARCH_ASSET".equals(action)) {
            // 3. Afficher les données d'un bien demandé par une autre application
            String query = intent.getStringExtra("query"); // ID ou Nom
            if (query != null) {
                Cursor c = db.getReadableDatabase().rawQuery(
                        "SELECT * FROM " + DatabaseHelper.TABLE_BIENS + " WHERE " + DatabaseHelper.COL_NOM + "=? OR " + DatabaseHelper.COL_ID + "=?",
                        new String[]{query, query});
                
                if (c.moveToFirst()) {
                    String info = "Bien trouvé : " + c.getString(1) + " (" + c.getString(2) + ") - " + c.getDouble(3) + "€";
                    Toast.makeText(context, info, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Aucun bien trouvé pour : " + query, Toast.LENGTH_SHORT).show();
                }
                c.close();
            }
        }
    }
}
