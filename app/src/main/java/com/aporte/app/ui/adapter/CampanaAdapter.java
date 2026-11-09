package com.aporte.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aporte.app.R;
import com.aporte.app.model.data.Campana;

import java.util.List;

public class CampanaAdapter extends RecyclerView.Adapter<CampanaAdapter.CampanaViewHolder> {

    private List<Campana> listaCampanas;

    public CampanaAdapter(List<Campana> listaCampanas) {
        this.listaCampanas = listaCampanas;
    }

    @NonNull
    @Override
    public CampanaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_campana, parent, false);
        return new CampanaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CampanaViewHolder holder, int position) {
        Campana campana = listaCampanas.get(position);
        holder.tvTitulo.setText(campana.getTitulo());
        holder.tvDescripcion.setText(campana.getDescripcion());
        holder.tvCategoria.setText(campana.getCategoria() != null ? campana.getCategoria().toUpperCase() : "GENERAL");
        holder.tvFechaLimite.setText("Límite: " + campana.getFechaLimite());

        String montosTexto = "$" + (int)campana.getRecaudado() + " de $" + (int)campana.getMontoObjetivo();
        holder.tvMontos.setText(montosTexto);
    }

    @Override
    public int getItemCount() {
        return listaCampanas != null ? listaCampanas.size() : 0;
    }

    public void actualizarLista(List<Campana> nuevasCampanas) {
        this.listaCampanas = nuevasCampanas;
        notifyDataSetChanged();
    }

    public static class CampanaViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescripcion, tvCategoria, tvFechaLimite, tvMontos;

        public CampanaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvFechaLimite = itemView.findViewById(R.id.tvFechaLimite);
            tvMontos = itemView.findViewById(R.id.tvMontos);
        }
    }
}