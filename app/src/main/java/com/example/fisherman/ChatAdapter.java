package com.example.fisherman;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fisherman.Chats;
import com.example.fisherman.Mensaje;
import com.example.fisherman.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Chats> listaChats;

    public ChatAdapter(List<Chats> listaChats) {
        this.listaChats = listaChats;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chats chat = listaChats.get(position);


        holder.textViewNombreUsuario.setText("Usuario: " + chat.getUserId());
        //holder.textViewUltimoMensaje.setText(obtenerTextoUltimoMensaje(chat.getMensajes()));
        holder.textViewTimestamp.setText(obtenerTiempoFormateado(chat.getTimestamp()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = view.getContext();

                Intent intent = new Intent(context, Chat.class);

                intent.putExtra("receptorId", chat.getUserId());

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaChats.size();
    }

    public void actualizarChats(List<Chats> nuevaListaChats) {
        listaChats.clear();
        listaChats.addAll(nuevaListaChats);
        notifyDataSetChanged();
    }

    // ViewHolder para los elementos del chat
    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewNombreUsuario;
        public TextView textViewUltimoMensaje;
        public TextView textViewTimestamp;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNombreUsuario = itemView.findViewById(R.id.textViewNombreUsuario);
            textViewUltimoMensaje = itemView.findViewById(R.id.textViewUltimoMensaje);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
        }
    }

    // Método para obtener la marca de tiempo formateada
    private String obtenerTiempoFormateado(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    // Método para obtener el texto del último mensaje
    private String obtenerTextoUltimoMensaje(List<Mensaje> mensajes) {
        if (mensajes != null && mensajes.size() > 0) {
            Mensaje ultimoMensaje = mensajes.get(mensajes.size() - 1);
            return ultimoMensaje.getContenido();
        } else {
            return "Sin mensajes";
        }
    }
}
