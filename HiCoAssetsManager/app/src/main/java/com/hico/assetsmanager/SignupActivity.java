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

public class SignupActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        DatabaseHelper db = new DatabaseHelper(this);
        EditText username = findViewById(R.id.signupUsernameEditText);
        EditText password = findViewById(R.id.signupPasswordEditText);
        Spinner role = findViewById(R.id.signupRoleSpinner);
        MaterialButton send = findViewById(R.id.sendRequestButton);

        MaterialToolbar toolbar = findViewById(R.id.signupToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"user", "admin"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        role.setAdapter(adapter);

        send.setOnClickListener(v -> {
            String u = username.getText().toString().trim();
            String p = password.getText().toString().trim();
            if (u.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Username et mot de passe obligatoires", Toast.LENGTH_SHORT).show();
                return;
            }

            long result = db.requestUser(u, p, role.getSelectedItem().toString());
            if (result == -1) {
                Toast.makeText(this, "Ce compte existe deja", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Demande envoyee a l'administrateur", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}
