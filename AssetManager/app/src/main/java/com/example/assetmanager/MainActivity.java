package com.example.assetmanager;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    DatabaseHelper db;
    EditText editNom, editType, editValeur, editDesc, searchBar;
    Button btnAdd, btnUpdate, btnDelete;
    ImageButton btnLogout;
    ListView listView;
    View adminLayout;
    TextView txtRole;
    boolean isAdmin = false;
    int selectedId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        isAdmin = getIntent().getBooleanExtra("IS_ADMIN", false);

        txtRole = findViewById(R.id.txtRole);
        searchBar = findViewById(R.id.searchBar);
        adminLayout = findViewById(R.id.adminScroll);
        editNom = findViewById(R.id.editNom);
        editType = findViewById(R.id.editType);
        editValeur = findViewById(R.id.editValeur);
        editDesc = findViewById(R.id.editDesc);
        btnAdd = findViewById(R.id.btnAdd);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        btnLogout = findViewById(R.id.btnLogout);
        listView = findViewById(R.id.listView);

        if (isAdmin) {
            txtRole.setText("SESSION ADMINISTRATEUR");
            adminLayout.setVisibility(View.VISIBLE);
        } else {
            txtRole.setText("CONSULTATION PUBLIQUE");
            adminLayout.setVisibility(View.GONE);
        }

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { refreshList(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnAdd.setOnClickListener(v -> addAsset());
        btnUpdate.setOnClickListener(v -> updateAsset());
        btnDelete.setOnClickListener(v -> deleteAsset());

        btnLogout.setOnClickListener(v -> {
            finish(); // Retourne à LoginActivity
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Cursor cursor = (Cursor) parent.getItemAtPosition(position);
            selectedId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            if (isAdmin) {
                editNom.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOM)));
                editType.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TYPE)));
                editValeur.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_VALEUR)));
                editDesc.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESC)));
            }
            db.logAction("CONSULTATION");
        });

        refreshList("");
    }

    private void refreshList(String search) {
        String query = "SELECT id as _id, * FROM " + DatabaseHelper.TABLE_BIENS;
        String[] args = null;
        if (!search.isEmpty()) {
            query += " WHERE " + DatabaseHelper.COL_NOM + " LIKE ?";
            args = new String[]{"%" + search + "%"};
        }
        
        Cursor cursor = db.getReadableDatabase().rawQuery(query, args);
        String[] from = {DatabaseHelper.COL_NOM, DatabaseHelper.COL_TYPE, DatabaseHelper.COL_VALEUR};
        int[] to = {R.id.itemNom, R.id.itemType, R.id.itemValeur};
        
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.item_asset, cursor, from, to, 0);
        
        adapter.setViewBinder((view, cursor1, columnIndex) -> {
            if (view.getId() == R.id.itemValeur) {
                double val = cursor1.getDouble(columnIndex);
                ((TextView) view).setText(String.format("%.3f TND", val));
                return true;
            }
            return false;
        });

        listView.setAdapter(adapter);
    }

    private void addAsset() {
        if (editNom.getText().toString().isEmpty()) return;
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_NOM, editNom.getText().toString());
        cv.put(DatabaseHelper.COL_TYPE, editType.getText().toString());
        cv.put(DatabaseHelper.COL_VALEUR, Double.parseDouble(editValeur.getText().toString().isEmpty() ? "0" : editValeur.getText().toString()));
        cv.put(DatabaseHelper.COL_DESC, editDesc.getText().toString());
        db.getWritableDatabase().insert(DatabaseHelper.TABLE_BIENS, null, cv);
        db.logAction("CREATION");
        refreshList("");
        clearFields();
        Toast.makeText(this, "Bien enregistré", Toast.LENGTH_SHORT).show();
    }

    private void updateAsset() {
        if (selectedId == -1) return;
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_NOM, editNom.getText().toString());
        cv.put(DatabaseHelper.COL_TYPE, editType.getText().toString());
        cv.put(DatabaseHelper.COL_VALEUR, Double.parseDouble(editValeur.getText().toString()));
        cv.put(DatabaseHelper.COL_DESC, editDesc.getText().toString());
        db.getWritableDatabase().update(DatabaseHelper.TABLE_BIENS, cv, "id=?", new String[]{String.valueOf(selectedId)});
        db.logAction("MODIFICATION");
        refreshList("");
        clearFields();
    }

    private void deleteAsset() {
        if (selectedId == -1) return;
        db.getWritableDatabase().delete(DatabaseHelper.TABLE_BIENS, "id=?", new String[]{String.valueOf(selectedId)});
        db.logAction("SUPPRESSION");
        refreshList("");
        clearFields();
    }

    private void clearFields() {
        editNom.setText(""); editType.setText(""); editValeur.setText(""); editDesc.setText("");
        selectedId = -1;
    }
}
