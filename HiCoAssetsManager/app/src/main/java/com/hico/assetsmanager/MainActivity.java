package com.hico.assetsmanager;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BienAdapter.Actions {

    // ── Prefs keys ───────────────────────────────────────────
    private static final String PREFS   = "hico_prefs";
    private static final String K_ROLE  = "role";
    private static final String K_USER  = "username";
    private static final String K_IN    = "logged_in";
    static final String ADMIN = "admin", USER = "user";

    // ── Credentials ──────────────────────────────────────────
    private static final String[][] CREDS = {
            {"admin","admin123",ADMIN},
            {"user","user123",USER}
    };

    // ── State ────────────────────────────────────────────────
    private SharedPreferences prefs;
    private DB db;
    private BienAdapter adapter;
    private List<Bien> allBiens;
    private String role;
    private String filter = "All";

    // ── Views — LOGIN (flipper child 0) ──────────────────────
    private EditText etUser, etPass;
    private Button btnLogin;

    // ── Views — MAIN (flipper child 1) ───────────────────────
    private ViewFlipper flipper;
    private RecyclerView rv;
    private EditText etSearch;
    private TextView tvCount, tvValue, tvRole, tvEmpty;
    private FloatingActionButton fab;
    private ChipGroup chips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        db    = DB.get(this);

        bindViews();

        // If already logged in → go straight to main screen
        if (prefs.getBoolean(K_IN, false)) {
            role = prefs.getString(K_ROLE, USER);
            showMain();
        } else {
            showLogin();
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (prefs.getBoolean(K_IN,false)) refresh();
    }

    // ── Bind all views ───────────────────────────────────────
    private void bindViews() {
        flipper  = findViewById(R.id.flipper);

        // Login views
        etUser   = findViewById(R.id.et_username);
        etPass   = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);

        // Main views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rv       = findViewById(R.id.rv);
        etSearch = findViewById(R.id.et_search);
        tvCount  = findViewById(R.id.tv_count);
        tvValue  = findViewById(R.id.tv_value);
        tvRole   = findViewById(R.id.tv_role);
        tvEmpty  = findViewById(R.id.tv_empty);
        fab      = findViewById(R.id.fab);
        chips    = findViewById(R.id.chips);

        rv.setLayoutManager(new LinearLayoutManager(this));

        // Login button
        btnLogin.setOnClickListener(v -> attemptLogin());

        // FAB → add new bien
        fab.setOnClickListener(v -> {
            startActivity(new Intent(this, AddEditActivity.class));
        });

        // Search
        etSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            public void afterTextChanged(Editable s){}
            public void onTextChanged(CharSequence s,int a,int b,int c){ applyFilter(); }
        });

        // Chips
        chips.setOnCheckedStateChangeListener((g, ids) -> {
            if (ids.isEmpty()) { filter="All"; ((Chip)findViewById(R.id.chip_all)).setChecked(true); }
            else {
                int id = ids.get(0);
                if      (id==R.id.chip_all)  filter="All";
                else if (id==R.id.chip_mob)  filter="Mobilier";
                else if (id==R.id.chip_immo) filter="Immobilier";
            }
            applyFilter();
        });
    }

    // ── LOGIN ────────────────────────────────────────────────
    private void attemptLogin() {
        String u = etUser.getText().toString().trim();
        String p = etPass.getText().toString().trim();
        if (u.isEmpty()){ etUser.setError("Requis"); return; }
        if (p.isEmpty()){ etPass.setError("Requis"); return; }

        for (String[] cred : CREDS) {
            if (cred[0].equals(u) && cred[1].equals(p)) {
                role = cred[2];
                prefs.edit().putBoolean(K_IN,true).putString(K_ROLE,role)
                        .putString(K_USER,u).apply();
                Toast.makeText(this,"Bienvenue "+u+" 🐝",Toast.LENGTH_SHORT).show();
                showMain();
                return;
            }
        }
        etPass.setText("");
        etPass.setError("Identifiants incorrects");
        // Shake animation
        etPass.animate().translationX(16).setDuration(50)
                .withEndAction(()->etPass.animate().translationX(-16).setDuration(50)
                        .withEndAction(()->etPass.animate().translationX(0).setDuration(50).start())
                        .start()).start();
    }

    // ── SHOW LOGIN SCREEN ────────────────────────────────────
    private void showLogin() {
        flipper.setDisplayedChild(0);           // child 0 = login
        if(getSupportActionBar()!=null) getSupportActionBar().hide();
    }

    // ── SHOW MAIN SCREEN ─────────────────────────────────────
    private void showMain() {
        flipper.setDisplayedChild(1);           // child 1 = main
        if(getSupportActionBar()!=null) getSupportActionBar().show();
        fab.setVisibility(ADMIN.equals(role) ? View.VISIBLE : View.GONE);
        tvRole.setText(ADMIN.equals(role) ? "👑 Admin" : "👤 Utilisateur");
        refresh();
    }

    // ── REFRESH DATA ─────────────────────────────────────────
    private void refresh() {
        allBiens = db.getAll();
        if (adapter == null) {
            adapter = new BienAdapter(allBiens, ADMIN.equals(role), this);
            rv.setAdapter(adapter);
        } else {
            adapter.update(allBiens);
        }
        tvCount.setText(String.valueOf(allBiens.size()));
        tvValue.setText(String.format("%.0f €", db.totalValue()));
        applyFilter();
    }

    // ── FILTER ───────────────────────────────────────────────
    private void applyFilter() {
        if (allBiens == null) return;
        String q = etSearch.getText().toString().toLowerCase();
        List<Bien> out = new java.util.ArrayList<>();
        for (Bien b : allBiens) {
            boolean matchQ = q.isEmpty() || b.nom.toLowerCase().contains(q)
                    || b.description.toLowerCase().contains(q);
            boolean matchT = filter.equals("All") || b.type.equals(filter);
            if (matchQ && matchT) out.add(b);
        }
        adapter.update(out);
        tvEmpty.setVisibility(out.isEmpty() ? View.VISIBLE : View.GONE);
        rv.setVisibility(out.isEmpty() ? View.GONE : View.VISIBLE);
    }

    // ── ADAPTER CALLBACKS ────────────────────────────────────
    @Override public void onView(Bien b) {
        db.touch(b.id);
        new AlertDialog.Builder(this)
                .setTitle("🐝 " + b.nom)
                .setMessage(
                        "Type : " + b.type + "\n" +
                        "Valeur : " + b.valeurStr() + "\n" +
                        "Description : " + b.description + "\n\n" +
                        "Créé le : " + b.dateCreation + "\n" +
                        "Modifié : " + (b.dateModification.isEmpty()?"—":b.dateModification) + "\n" +
                        "Consulté : " + (b.dateConsultation.isEmpty()?"—":b.dateConsultation))
                .setPositiveButton("OK", null)
                .show();
    }

    @Override public void onEdit(Bien b) {
        Intent i = new Intent(this, AddEditActivity.class);
        i.putExtra("bien", b);
        startActivity(i);
    }

    @Override public void onDelete(Bien b) {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Supprimer ?")
                .setMessage("Voulez-vous supprimer \"" + b.nom + "\" ?")
                .setPositiveButton("Supprimer", (d,w) -> {
                    db.delete(b.id);
                    Toast.makeText(this,"Supprimé ✓",Toast.LENGTH_SHORT).show();
                    refresh();
                })
                .setNegativeButton("Annuler",null)
                .show();
    }

    // ── MENU ─────────────────────────────────────────────────
    @Override public boolean onCreateOptionsMenu(Menu m) {
        m.add(0,1,0,"Actualiser").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        m.add(0,2,0,"Déconnexion");
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) { refresh(); return true; }
        if (item.getItemId() == 2) {
            prefs.edit().clear().apply();
            etUser.setText(""); etPass.setText("");
            showLogin();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}