package com.hico.assetsmanager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// =============================================================
// RECEIVER 1 — BOOT (shows latest dates as notification)
// =============================================================
class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context ctx, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        String[] d = DB.get(ctx).latestDates();
        String msg = "Création: "+d[0]+"\nModification: "+d[1]+
                     "\nConsultation: "+d[2]+"\nSuppression: "+d[3];
        notify(ctx, "🐝 HiCo — Résumé au démarrage", msg, 1001);
    }
}

// =============================================================
// RECEIVER 2 — TIME CHANGE (warns if date goes backwards)
// =============================================================
class TimeReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context ctx, Intent intent) {
        String action = intent.getAction();
        if (!Intent.ACTION_TIME_CHANGED.equals(action) &&
            !Intent.ACTION_DATE_CHANGED.equals(action)) return;

        String last    = DB.get(ctx).lastDate();
        String current = DB.now();
        if (last.isEmpty()) return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date dCurrent = sdf.parse(current);
            Date dLast    = sdf.parse(last);
            if (dCurrent != null && dLast != null && dCurrent.before(dLast)) {
                String warn = "⚠️ Date actuelle ("+current+") est antérieure à la dernière " +
                              "activité enregistrée ("+last+"). Intégrité des données à risque!";
                Toast.makeText(ctx, warn, Toast.LENGTH_LONG).show();
                notify(ctx, "⚠️ HiCo — Changement de date suspect", warn, 1002);
            }
        } catch (ParseException e) { e.printStackTrace(); }
    }
}

// =============================================================
// RECEIVER 3 — BIEN REQUEST (external app queries a bien)
// =============================================================
class RequestReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context ctx, Intent intent) {
        if (!"com.hico.assetsmanager.REQUEST_BIEN".equals(intent.getAction())) return;

        DB db = DB.get(ctx);
        Bien found = null;

        int searchId = intent.getIntExtra("search_id", -1);
        if (searchId != -1) found = db.getById(searchId);

        if (found == null) {
            String name = intent.getStringExtra("search_name");
            if (name != null && !name.isEmpty()) {
                List<Bien> results = db.search(name);
                if (!results.isEmpty()) found = results.get(0);
            }
        }

        if (found != null) {
            Toast.makeText(ctx,
                    "🐝 HiCo: "+found.nom+" | "+found.type+" | "+found.valeurStr(),
                    Toast.LENGTH_LONG).show();
            // Broadcast result back
            Intent res = new Intent("com.hico.assetsmanager.RESPONSE_BIEN");
            res.putExtra("found", true);
            res.putExtra("bien_id",   found.id);
            res.putExtra("bien_nom",  found.nom);
            res.putExtra("bien_type", found.type);
            res.putExtra("bien_val",  found.valeur);
            ctx.sendBroadcast(res);
        } else {
            Toast.makeText(ctx, "🐝 HiCo: Aucun bien trouvé.", Toast.LENGTH_SHORT).show();
            ctx.sendBroadcast(new Intent("com.hico.assetsmanager.RESPONSE_BIEN")
                    .putExtra("found", false));
        }
    }
}

// =============================================================
// CONTENT PROVIDER — Read-only, URI: content://com.hico.assetsprovider/biens
// =============================================================
class BienProvider extends ContentProvider {

    private static final String AUTH = "com.hico.assetsprovider";
    public  static final Uri CONTENT_URI = Uri.parse("content://"+AUTH+"/biens");
    private static final UriMatcher UM = new UriMatcher(UriMatcher.NO_MATCH);
    static { UM.addURI(AUTH,"biens",1); UM.addURI(AUTH,"biens/#",2); }

    private static final String[] COLS = {
        DB.C_ID,DB.C_NOM,DB.C_TYPE,DB.C_DESC,DB.C_VAL,
        DB.C_CREAT,DB.C_MODIF,DB.C_CONSULT,DB.C_SUPP
    };

    @Override public boolean onCreate() { return true; }

    @Override public Cursor query(@NonNull Uri uri, String[] proj,
                                  String sel, String[] selArgs, String sort) {
        DB db = DB.get(getContext());
        MatrixCursor mc = new MatrixCursor(COLS);
        if (UM.match(uri) == 1) {
            for (Bien b : db.getAll()) mc.addRow(row(b));
        } else if (UM.match(uri) == 2) {
            Bien b = db.getById(Integer.parseInt(uri.getLastPathSegment()));
            if (b!=null) mc.addRow(row(b));
        }
        return mc;
    }

    // Write operations DISABLED — read-only provider
    @Override public Uri insert(@NonNull Uri u,ContentValues v){
        throw new UnsupportedOperationException("Read-only"); }
    @Override public int update(@NonNull Uri u,ContentValues v,String s,String[] a){
        throw new UnsupportedOperationException("Read-only"); }
    @Override public int delete(@NonNull Uri u,String s,String[] a){
        throw new UnsupportedOperationException("Read-only"); }
    @Override public String getType(@NonNull Uri u){ return null; }

    private Object[] row(Bien b){
        return new Object[]{b.id,b.nom,b.type,b.description,b.valeur,
                b.dateCreation,b.dateModification,b.dateConsultation,b.dateSuppression};
    }
}

// =============================================================
// SHARED NOTIFICATION HELPER (used by BootReceiver & TimeReceiver)
// =============================================================
class NotifHelper {
    static void notify(Context ctx, String title, String msg, int id){
        NotificationManager nm =
                (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        String ch = "hico_ch";
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
            nm.createNotificationChannel(
                    new NotificationChannel(ch,"HiCo",NotificationManager.IMPORTANCE_DEFAULT));
        nm.notify(id, new NotificationCompat.Builder(ctx,ch)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setAutoCancel(true)
                .setColor(0xFFFFC107)
                .build());
    }
}

// Make notify accessible from inner classes
// (static helper call — avoids code duplication)