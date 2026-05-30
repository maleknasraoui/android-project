package com.hico.assetsmanager;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.hico.assetsmanager.db.DatabaseHelper;

public class UserRequestsActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private LinearLayout container;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_requests);

        db = new DatabaseHelper(this);
        container = findViewById(R.id.requestsContainer);
        emptyText = findViewById(R.id.emptyRequestsTextView);

        MaterialToolbar toolbar = findViewById(R.id.requestsToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        loadRequests();
    }

    private void loadRequests() {
        container.removeAllViews();
        Cursor cursor = db.getPendingUsers();
        emptyText.setVisibility(cursor.getCount() == 0 ? View.VISIBLE : View.GONE);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_ID));
            String username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_USERNAME));
            String role = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_ROLE));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_DATE_REQUEST));
            addRequestView(id, username, role, date);
        }
        cursor.close();
    }

    private void addRequestView(int id, String username, String role, String date) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_user_request, container, false);
        TextView usernameText = view.findViewById(R.id.requestUsernameTextView);
        TextView roleText = view.findViewById(R.id.requestRoleTextView);
        TextView dateText = view.findViewById(R.id.requestDateTextView);
        Button approve = view.findViewById(R.id.approveButton);
        Button deny = view.findViewById(R.id.denyButton);

        usernameText.setText(username);
        roleText.setText("Role demande: " + role);
        dateText.setText("Date: " + date);

        approve.setOnClickListener(v -> update(id, "approved", "Compte accepte"));
        deny.setOnClickListener(v -> update(id, "denied", "Compte refuse"));
        container.addView(view);
    }

    private void update(int id, String status, String message) {
        db.updateUserStatus(id, status);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        loadRequests();
    }
}
