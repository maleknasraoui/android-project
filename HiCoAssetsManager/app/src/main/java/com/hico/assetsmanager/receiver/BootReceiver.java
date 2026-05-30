package com.hico.assetsmanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.hico.assetsmanager.db.DatabaseHelper;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            DatabaseHelper db = new DatabaseHelper(context);
            Toast.makeText(context, db.getLastDatesText(), Toast.LENGTH_LONG).show();
        }
    }
}
