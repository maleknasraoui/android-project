package com.hico.assetsmanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.hico.assetsmanager.db.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction())) {
            DatabaseHelper db = new DatabaseHelper(context);
            String current = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            String lastSaved = db.getMaxSavedDate();
            if (!lastSaved.equals("Aucune") && current.compareTo(lastSaved) < 0) {
                Toast.makeText(context, "Attention: date systeme anterieure aux donnees HiCo", Toast.LENGTH_LONG).show();
            }
        }
    }
}
