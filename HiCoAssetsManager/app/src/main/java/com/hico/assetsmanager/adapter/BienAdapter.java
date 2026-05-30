package com.hico.assetsmanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hico.assetsmanager.R;
import com.hico.assetsmanager.model.Bien;

import java.util.ArrayList;
import java.util.List;

public class BienAdapter extends RecyclerView.Adapter<BienAdapter.BienViewHolder> {
    public interface Listener {
        void onEdit(Bien bien);
        void onDelete(Bien bien);
        void onOpen(Bien bien);
    }

    private final boolean isAdmin;
    private final Listener listener;
    private final List<Bien> biens = new ArrayList<>();

    public BienAdapter(boolean isAdmin, Listener listener) {
        this.isAdmin = isAdmin;
        this.listener = listener;
    }

    public void setBiens(List<Bien> newBiens) {
        biens.clear();
        biens.addAll(newBiens);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BienViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bien, parent, false);
        return new BienViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BienViewHolder holder, int position) {
        Bien bien = biens.get(position);
        holder.nom.setText(bien.getNom());
        holder.type.setText(bien.getType());
        holder.valeur.setText(String.format("Valeur: %.2f DT", bien.getValeur()));
        holder.description.setText(bien.getDescription());
        holder.actions.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> listener.onOpen(bien));
        holder.edit.setOnClickListener(v -> listener.onEdit(bien));
        holder.delete.setOnClickListener(v -> listener.onDelete(bien));
    }

    @Override
    public int getItemCount() {
        return biens.size();
    }

    static class BienViewHolder extends RecyclerView.ViewHolder {
        TextView nom, type, valeur, description;
        Button edit, delete;
        LinearLayout actions;

        BienViewHolder(@NonNull View itemView) {
            super(itemView);
            nom = itemView.findViewById(R.id.nomTextView);
            type = itemView.findViewById(R.id.typeTextView);
            valeur = itemView.findViewById(R.id.valeurTextView);
            description = itemView.findViewById(R.id.descriptionTextView);
            edit = itemView.findViewById(R.id.editButton);
            delete = itemView.findViewById(R.id.deleteButton);
            actions = itemView.findViewById(R.id.adminActionsLayout);
        }
    }
}
