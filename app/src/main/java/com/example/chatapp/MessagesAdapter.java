package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder> {

    private static final int TYPE_MY_MESSAGE=0;
    private static final int TYPE_MY_OTHERMESSAGE=1;

    private List<Message> messages;
    private Context context;

    public MessagesAdapter(Context context) {
        this.messages = new ArrayList<>();
        this.context=context;
    }

    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        if (i==TYPE_MY_MESSAGE){
            view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_item_mymessage,viewGroup,false);
        }else {
            view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_item_othermessage,viewGroup,false);
        }
        return new MessagesViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        Message message=messages.get(position);
        String author=message.getAuthor();
        if (author!=null && author.equals(PreferenceManager.getDefaultSharedPreferences(context).getString("author","Anonim"))){
            return TYPE_MY_MESSAGE;
        }else {
            return TYPE_MY_OTHERMESSAGE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesViewHolder messagesViewHolder, int i) {
        Message message=messages.get(i);
        String textOfMessage=message.getTextOfMessage();
        String urlToImage=message.getImageUrl();
        messagesViewHolder.author.setText(message.getAuthor());
        if (textOfMessage!=null && !textOfMessage.isEmpty()) {
            messagesViewHolder.textViewTextOfMessage.setText(textOfMessage);
            messagesViewHolder.imageViewImage.setVisibility(View.GONE);
        }
        if (urlToImage!=null && !urlToImage.isEmpty()){
            messagesViewHolder.imageViewImage.setVisibility(View.VISIBLE);
            Picasso.get().load(urlToImage).into(messagesViewHolder.imageViewImage);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class MessagesViewHolder extends RecyclerView.ViewHolder{
        private TextView author;
        private TextView textViewTextOfMessage;
        private ImageView imageViewImage;

        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);
            this.author=itemView.findViewById(R.id.textViewAuthor);
            this.textViewTextOfMessage =itemView.findViewById(R.id.textViewTextOfMessage);
            this.imageViewImage=itemView.findViewById(R.id.imageViewImage);
        }
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public List<Message> getMessages() {
        return messages;
    }
}
