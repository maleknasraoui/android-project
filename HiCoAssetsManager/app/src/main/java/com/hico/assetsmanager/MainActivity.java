package com.hico.assetsmanager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.button.MaterialButton;
import com.hico.assetsmanager.adapter.BienAdapter;
import com.hico.assetsmanager.db.DatabaseHelper;
import com.hico.assetsmanager.model.Bien;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private BienAdapter adapter;
    private EditText searchEditText;
    private Spinner typeSpinner;
    private TextView statsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean isAdmin = getIntent().getBooleanExtra("isAdmin", false);
        db = new DatabaseHelper(this);
        searchEditText = findViewById(R.id.searchEditText);
        typeSpinner = findViewById(R.id.typeSpinner);
        statsTextView = findViewById(R.id.statsTextView);
        MaterialButton manageUsers = findViewById(R.id.manageUsersButton);
        manageUsers.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        manageUsers.setOnClickListener(v -> startActivity(new Intent(this, UserRequestsActivity.class)));

        RecyclerView recyclerView = findViewById(R.id.biensRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BienAdapter(isAdmin, new BienAdapter.Listener() {
            @Override public void onEdit(Bien bien) { openForm(bien.getId()); }
            @Override public void onDelete(Bien bien) { confirmDelete(bien); }
            @Override public void onOpen(Bien bien) {
                db.markConsulted(bien.getId());
                Toast.makeText(MainActivity.this, bien.getNom() + " consulte", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);

        FloatingActionButton addFab = findViewById(R.id.addFab);
        addFab.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        addFab.setOnClickListener(v -> openForm(-1));

        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Tous", "Mobilier", "Immobilier"});
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(filterAdapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { loadBiens(); }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { loadBiens(); }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBiens();
    }

    private void loadBiens() {
        String query = searchEditText.getText().toString().trim();
        String filter = typeSpinner.getSelectedItem() == null ? "Tous" : typeSpinner.getSelectedItem().toString();
        List<Bien> source = query.isEmpty() ? db.getAllBiens() : db.getBienByName(query);
        List<Bien> filtered = new ArrayList<>();
        for (Bien bien : source) {
            if (filter.equals("Tous") || bien.getType().equals(filter)) filtered.add(bien);
        }
        adapter.setBiens(filtered);
        statsTextView.setText(filtered.size() + " bien(s) affiche(s)");
    }

    private void openForm(int id) {
        Intent intent = new Intent(this, AddEditBienActivity.class);
        intent.putExtra("bienId", id);
        startActivity(intent);
    }

    private void confirmDelete(Bien bien) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer")
                .setMessage("Supprimer " + bien.getNom() + " ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    db.deleteBien(bien.getId());
                    loadBiens();
                })
                .setNegativeButton("Non", null)
                .show();
    }
}
