package com.hico.assetsmanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hico.assetsmanager.db.DatabaseHelper;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        DatabaseHelper db = new DatabaseHelper(this);
        EditText username = findViewById(R.id.usernameEditText);
        EditText password = findViewById(R.id.passwordEditText);
        Button login = findViewById(R.id.loginButton);
        Button requestAccount = findViewById(R.id.requestAccountButton);

        login.setOnClickListener(v -> {
            String u = username.getText().toString().trim();
            String p = password.getText().toString().trim();
            String role = db.loginUser(u, p);
            if ("admin".equals(role)) {
                openMain(true);
            } else if ("user".equals(role)) {
                openMain(false);
            } else {
                Toast.makeText(this, "Compte incorrect ou pas encore accepte", Toast.LENGTH_SHORT).show();
            }
        });

        requestAccount.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
    }

    private void openMain(boolean isAdmin) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("isAdmin", isAdmin);
        startActivity(intent);
        finish();
    }
}
