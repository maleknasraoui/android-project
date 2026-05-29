package com.hico.assetsmanager;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddEditActivity extends AppCompatActivity {

    private TextInputEditText etNom, etDesc, etVal;
    private TextInputLayout tilNom, tilType, tilVal;
    private AutoCompleteTextView etType;
    private Bien editing;  // null = add mode, non-null = edit mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Get bien from intent (null = add mode)
        editing = (Bien) getIntent().getSerializableExtra("bien");

        Toolbar tb = findViewById(R.id.toolbar_edit);
        setSupportActionBar(tb);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(editing == null ? "Ajouter un Bien" : "Modifier le Bien");
        }

        tilNom  = findViewById(R.id.til_nom);
        tilType = findViewById(R.id.til_type);
        tilVal  = findViewById(R.id.til_val);
        etNom   = findViewById(R.id.et_nom);
        etType  = findViewById(R.id.et_type);
        etDesc  = findViewById(R.id.et_desc);
        etVal   = findViewById(R.id.et_val);
        Button btnSave   = findViewById(R.id.btn_save);
        Button btnCancel = findViewById(R.id.btn_cancel);
        TextView tvDate  = findViewById(R.id.tv_date);

        // Dropdown for type
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"Mobilier","Immobilier"});
        etType.setAdapter(typeAdapter);
        etType.setOnClickListener(v -> etType.showDropDown());

        // Pre-fill if editing
        if (editing != null) {
            etNom.setText(editing.nom);
            etType.setText(editing.type, false);
            etDesc.setText(editing.description);
            etVal.setText(String.valueOf(editing.valeur));
            tvDate.setText("Créé le : " + editing.dateCreation);
        } else {
            tvDate.setText("Date création : " + DB.now());
        }

        btnSave.setOnClickListener(v -> save());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void save() {
        String nom  = etNom.getText()!=null ? etNom.getText().toString().trim() : "";
        String type = etType.getText().toString().trim();
        String desc = etDesc.getText()!=null ? etDesc.getText().toString().trim() : "";
        String valS = etVal.getText()!=null  ? etVal.getText().toString().trim()  : "";

        // Validate
        boolean ok = true;
        if (nom.isEmpty())  { tilNom.setError("Obligatoire");  ok=false; } else tilNom.setError(null);
        if (type.isEmpty()) { tilType.setError("Sélectionnez un type"); ok=false; } else tilType.setError(null);
        if (valS.isEmpty()) { tilVal.setError("Obligatoire");  ok=false; } else {
            try { Double.parseDouble(valS); tilVal.setError(null); }
            catch(NumberFormatException e){ tilVal.setError("Nombre invalide"); ok=false; }
        }
        if (!ok) return;

        double val = Double.parseDouble(valS);
        DB db = DB.get(this);

        if (editing == null) {
            // ADD
            Bien b = new Bien(nom, type, desc, val);
            long id = db.insert(b);
            Toast.makeText(this, id!=-1 ? "✅ Ajouté !" : "❌ Erreur", Toast.LENGTH_SHORT).show();
        } else {
            // EDIT
            editing.nom=nom; editing.type=type; editing.description=desc; editing.valeur=val;
            int rows = db.update(editing);
            Toast.makeText(this, rows>0 ? "✅ Mis à jour !" : "❌ Erreur", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){ finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}