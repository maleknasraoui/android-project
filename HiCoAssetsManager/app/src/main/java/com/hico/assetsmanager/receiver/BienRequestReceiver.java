package com.hico.assetsmanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.hico.assetsmanager.db.DatabaseHelper;
import com.hico.assetsmanager.model.Bien;

import java.util.List;

public class BienRequestReceiver extends BroadcastReceiver {
    public static final String ACTION_REQUEST = "com.hico.assetsmanager.REQUEST_BIEN";
    public static final String ACTION_RESPONSE = "com.hico.assetsmanager.RESPONSE_BIEN";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION_REQUEST.equals(intent.getAction())) return;

        DatabaseHelper db = new DatabaseHelper(context);
        Bien bien = null;
        int id = intent.getIntExtra("id", -1);
        String nom = intent.getStringExtra("nom");

        if (id != -1) {
            bien = db.getBienById(id);
        } else if (nom != null) {
            List<Bien> biens = db.getBienByName(nom);
            if (!biens.isEmpty()) bien = biens.get(0);
        }

        Intent response = new Intent(ACTION_RESPONSE);
        if (bien != null) {
            response.putExtra("found", true);
            response.putExtra("id", bien.getId());
            response.putExtra("nom", bien.getNom());
            response.putExtra("type", bien.getType());
            response.putExtra("description", bien.getDescription());
            response.putExtra("valeur", bien.getValeur());
            response.putExtra("date_creation", bien.getDateCreation());
            Toast.makeText(context, "Bien envoye: " + bien.getNom(), Toast.LENGTH_SHORT).show();
        } else {
            response.putExtra("found", false);
            Toast.makeText(context, "Bien introuvable", Toast.LENGTH_SHORT).show();
        }
        context.sendBroadcast(response);
    }
}
