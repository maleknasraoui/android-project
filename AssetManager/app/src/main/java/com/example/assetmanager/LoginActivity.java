package com.example.assetmanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText loginUser, loginPass;
    Button btnAdmin, btnUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginUser = findViewById(R.id.loginUser);
        loginPass = findViewById(R.id.loginPass);
        btnAdmin = findViewById(R.id.btnLoginAdmin);
        btnUser = findViewById(R.id.btnLoginUser);

        btnAdmin.setOnClickListener(v -> {
            String user = loginUser.getText().toString();
            String pass = loginPass.getText().toString();
            
            if (user.equals("admin") && pass.equals("admin")) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("IS_ADMIN", true);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Identifiants Admin incorrects", Toast.LENGTH_SHORT).show();
            }
        });

        btnUser.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("IS_ADMIN", false);
            startActivity(intent);
        });
    }
}
