package com.hico.assetsmanager;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.hico.assetsmanager.db.DatabaseHelper;
import com.hico.assetsmanager.model.Bien;

public class AddEditBienActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private int bienId;
    private EditText nomEditText, valeurEditText, descriptionEditText;
    private Spinner typeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_bien);

        db = new DatabaseHelper(this);
        bienId = getIntent().getIntExtra("bienId", -1);
        nomEditText = findViewById(R.id.nomEditText);
        valeurEditText = findViewById(R.id.valeurEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        typeSpinner = findViewById(R.id.typeSpinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Mobilier", "Immobilier"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);

        MaterialToolbar toolbar = findViewById(R.id.formToolbar);
        toolbar.setTitle(bienId == -1 ? "Ajouter un bien" : "Modifier un bien");
        toolbar.setNavigationOnClickListener(v -> finish());

        if (bienId != -1) fillForm();
        MaterialButton save = findViewById(R.id.saveButton);
        save.setOnClickListener(v -> saveBien());
    }

    private void fillForm() {
        Bien bien = db.getBienById(bienId);
        if (bien == null) return;
        nomEditText.setText(bien.getNom());
        valeurEditText.setText(String.valueOf(bien.getValeur()));
        descriptionEditText.setText(bien.getDescription());
        typeSpinner.setSelection(bien.getType().equals("Immobilier") ? 1 : 0);
    }

    private void saveBien() {
        String nom = nomEditText.getText().toString().trim();
        String valeurText = valeurEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        if (nom.isEmpty() || valeurText.isEmpty()) {
            Toast.makeText(this, "Nom et valeur obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        Bien bien = new Bien();
        bien.setId(bienId);
        bien.setNom(nom);
        bien.setType(typeSpinner.getSelectedItem().toString());
        bien.setDescription(description);
        bien.setValeur(Double.parseDouble(valeurText));

        if (bienId == -1) db.insertBien(bien);
        else db.updateBien(bien);
        Toast.makeText(this, "Bien enregistre", Toast.LENGTH_SHORT).show();
        finish();
    }
}
