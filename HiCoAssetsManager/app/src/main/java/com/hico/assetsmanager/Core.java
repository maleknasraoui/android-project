package com.hico.assetsmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// =============================================================
// SECTION 1 — MODEL
// =============================================================
class Bien implements Serializable {
    int id;
    String nom, type, description;
    double valeur;
    String dateCreation, dateModification, dateConsultation, dateSuppression;

    Bien() {}

    Bien(String nom, String type, String description, double valeur) {
        this.nom = nom; this.type = type;
        this.description = description; this.valeur = valeur;
        this.dateCreation = DB.now();
        this.dateModification = ""; this.dateConsultation = ""; this.dateSuppression = "";
    }

    // Format valeur nicely
    String valeurStr() { return String.format(Locale.getDefault(), "%.2f €", valeur); }
}

// =============================================================
// SECTION 2 — DATABASE HELPER
// =============================================================
class DB extends SQLiteOpenHelper {

    private static final String DB_NAME = "hico.db";
    private static final int DB_VER = 1;
    static final String T = "biens"; // table name
    // Column names
    static final String C_ID="id", C_NOM="nom", C_TYPE="type",
            C_DESC="description", C_VAL="valeur",
            C_CREAT="date_creation", C_MODIF="date_modification",
            C_CONSULT="date_consultation", C_SUPP="date_suppression";

    private static DB instance;

    static synchronized DB get(Context ctx) {
        if (instance == null) instance = new DB(ctx.getApplicationContext());
        return instance;
    }

    private DB(Context ctx) { super(ctx, DB_NAME, null, DB_VER); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T + "(" +
                C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                C_NOM + " TEXT," + C_TYPE + " TEXT," +
                C_DESC + " TEXT," + C_VAL + " REAL," +
                C_CREAT + " TEXT," + C_MODIF + " TEXT," +
                C_CONSULT + " TEXT," + C_SUPP + " TEXT)");
        // Sample data
        String n = now();
        for (Object[] d : new Object[][]{
                {"Siège Social Paris","Immobilier","Bâtiment principal HiCo",2500000.0},
                {"Flotte de Véhicules","Mobilier","10 véhicules de service",350000.0},
                {"Serveurs Informatiques","Mobilier","Infrastructure data center",180000.0},
                {"Entrepôt Lyon","Immobilier","Entrepôt logistique",850000.0}
        }) {
            ContentValues v = new ContentValues();
            v.put(C_NOM,(String)d[0]); v.put(C_TYPE,(String)d[1]);
            v.put(C_DESC,(String)d[2]); v.put(C_VAL,(Double)d[3]);
            v.put(C_CREAT,n); v.put(C_MODIF,""); v.put(C_CONSULT,""); v.put(C_SUPP,"");
            db.insert(T,null,v);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int o, int n) {
        db.execSQL("DROP TABLE IF EXISTS " + T); onCreate(db);
    }

    // ── INSERT ──────────────────────────────────────────────
    long insert(Bien b) {
        ContentValues v = new ContentValues();
        v.put(C_NOM,b.nom); v.put(C_TYPE,b.type);
        v.put(C_DESC,b.description); v.put(C_VAL,b.valeur);
        v.put(C_CREAT,now()); v.put(C_MODIF,""); v.put(C_CONSULT,""); v.put(C_SUPP,"");
        return getWritableDatabase().insert(T,null,v);
    }

    // ── UPDATE ──────────────────────────────────────────────
    int update(Bien b) {
        ContentValues v = new ContentValues();
        v.put(C_NOM,b.nom); v.put(C_TYPE,b.type);
        v.put(C_DESC,b.description); v.put(C_VAL,b.valeur);
        v.put(C_MODIF,now());
        return getWritableDatabase().update(T,v,C_ID+"=?",new String[]{String.valueOf(b.id)});
    }

    // ── DELETE ──────────────────────────────────────────────
    int delete(int id) {
        return getWritableDatabase().delete(T,C_ID+"=?",new String[]{String.valueOf(id)});
    }

    // ── UPDATE CONSULTATION DATE ─────────────────────────────
    void touch(int id) {
        ContentValues v = new ContentValues(); v.put(C_CONSULT,now());
        getWritableDatabase().update(T,v,C_ID+"=?",new String[]{String.valueOf(id)});
    }

    // ── GET ALL ──────────────────────────────────────────────
    List<Bien> getAll() {
        List<Bien> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM "+T+" ORDER BY "+C_ID+" DESC",null);
        while(c.moveToNext()) list.add(fromCursor(c));
        c.close(); return list;
    }

    // ── SEARCH BY NAME ──────────────────────────────────────
    List<Bien> search(String q) {
        List<Bien> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM "+T+" WHERE "+C_NOM+" LIKE ?",new String[]{"%"+q+"%"});
        while(c.moveToNext()) list.add(fromCursor(c));
        c.close(); return list;
    }

    // ── GET BY ID ────────────────────────────────────────────
    Bien getById(int id) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM "+T+" WHERE "+C_ID+"=?",new String[]{String.valueOf(id)});
        Bien b = null; if(c.moveToFirst()) b = fromCursor(c); c.close(); return b;
    }

    // ── GET BY TYPE ──────────────────────────────────────────
    List<Bien> getByType(String type) {
        List<Bien> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM "+T+" WHERE "+C_TYPE+"=?",new String[]{type});
        while(c.moveToNext()) list.add(fromCursor(c));
        c.close(); return list;
    }

    // ── STATS ────────────────────────────────────────────────
    int count() {
        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM "+T,null);
        int n = 0; if(c.moveToFirst()) n = c.getInt(0); c.close(); return n;
    }

    double totalValue() {
        Cursor c = getReadableDatabase().rawQuery("SELECT SUM("+C_VAL+") FROM "+T,null);
        double s = 0; if(c.moveToFirst()) s = c.getDouble(0); c.close(); return s;
    }

    // ── LAST RECORDED DATE (for TimeReceiver) ───────────────
    String lastDate() {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT MAX(d) FROM (SELECT MAX("+C_CREAT+") d FROM "+T+
                " UNION SELECT MAX("+C_MODIF+") FROM "+T+")",null);
        String d = ""; if(c.moveToFirst() && c.getString(0)!=null) d=c.getString(0);
        c.close(); return d;
    }

    // ── LATEST DATES (for BootReceiver) ─────────────────────
    String[] latestDates() {
        String[] result = new String[4];
        String[][] queries = {
                {"SELECT MAX("+C_CREAT+") FROM "+T},
                {"SELECT MAX("+C_MODIF+") FROM "+T+" WHERE "+C_MODIF+"!=''"},
                {"SELECT MAX("+C_CONSULT+") FROM "+T+" WHERE "+C_CONSULT+"!=''"},
                {"SELECT MAX("+C_SUPP+") FROM "+T+" WHERE "+C_SUPP+"!=''"}
        };
        for(int i=0;i<4;i++){
            Cursor c=getReadableDatabase().rawQuery(queries[i][0],null);
            result[i]=(c.moveToFirst()&&c.getString(0)!=null)?c.getString(0):"Aucune";
            c.close();
        }
        return result;
    }

    // ── HELPERS ──────────────────────────────────────────────
    private Bien fromCursor(Cursor c) {
        Bien b = new Bien();
        b.id          = c.getInt(c.getColumnIndexOrThrow(C_ID));
        b.nom         = c.getString(c.getColumnIndexOrThrow(C_NOM));
        b.type        = c.getString(c.getColumnIndexOrThrow(C_TYPE));
        b.description = c.getString(c.getColumnIndexOrThrow(C_DESC));
        b.valeur      = c.getDouble(c.getColumnIndexOrThrow(C_VAL));
        b.dateCreation    = c.getString(c.getColumnIndexOrThrow(C_CREAT));
        b.dateModification= c.getString(c.getColumnIndexOrThrow(C_MODIF));
        b.dateConsultation= c.getString(c.getColumnIndexOrThrow(C_CONSULT));
        b.dateSuppression = c.getString(c.getColumnIndexOrThrow(C_SUPP));
        return b;
    }

    static String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}

// =============================================================
// SECTION 3 — RECYCLER ADAPTER
// =============================================================
class BienAdapter extends RecyclerView.Adapter<BienAdapter.VH> {

    interface Actions {
        void onView(Bien b);
        void onEdit(Bien b);
        void onDelete(Bien b);
    }

    private List<Bien> list;
    private final boolean isAdmin;
    private final Actions actions;

    BienAdapter(List<Bien> list, boolean isAdmin, Actions actions) {
        this.list = list; this.isAdmin = isAdmin; this.actions = actions;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView nom, type, valeur, desc, badge;
        ImageButton btnEdit, btnDelete;
        View adminBar, indicator;
        VH(View v) {
            super(v);
            nom     = v.findViewById(R.id.tv_nom);
            type    = v.findViewById(R.id.tv_type);
            valeur  = v.findViewById(R.id.tv_valeur);
            desc    = v.findViewById(R.id.tv_desc);
            badge   = v.findViewById(R.id.tv_badge);
            btnEdit   = v.findViewById(R.id.btn_edit);
            btnDelete = v.findViewById(R.id.btn_delete);
            adminBar  = v.findViewById(R.id.admin_bar);
            indicator = v.findViewById(R.id.indicator);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_bien,p,false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Bien b = list.get(pos);
        h.nom.setText(b.nom);
        h.type.setText(b.type);
        h.valeur.setText(b.valeurStr());
        h.desc.setText(b.description.isEmpty() ? "—" : b.description);
        h.badge.setText(b.type);

        // Color indicator: yellow for Immobilier, black for Mobilier
        int color = b.type.equals("Immobilier") ? 0xFFFFC107 : 0xFF1A1A1A;
        h.indicator.setBackgroundColor(color);

        h.adminBar.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        h.itemView.setOnClickListener(v -> actions.onView(b));
        h.btnEdit.setOnClickListener(v -> actions.onEdit(b));
        h.btnDelete.setOnClickListener(v -> actions.onDelete(b));

        // Slide-in animation
        h.itemView.setAlpha(0f);
        h.itemView.setTranslationX(-30f);
        h.itemView.animate().alpha(1f).translationX(0).setDuration(250).setStartDelay(pos*40L).start();
    }

    @Override public int getItemCount() { return list == null ? 0 : list.size(); }

    void update(List<Bien> newList) { this.list = newList; notifyDataSetChanged(); }
}