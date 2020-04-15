package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    private static final int RC_GET_IMAGE = 101;
    // Access a Cloud Firestore instance from your Activity
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    FirebaseStorage storage;
    StorageReference storageRef;

    private RecyclerView recyclerView;
    private MessagesAdapter adapter;

    private EditText editTextMessage;
    private ImageView imageViewSendMessage;
    private ImageView imageViewAddImage;

    private SharedPreferences preferences;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.itemSignOut){
            mAuth.signOut();
            signOut();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        storage = FirebaseStorage.getInstance();
        preferences= PreferenceManager.getDefaultSharedPreferences(this);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        // Create a storage reference from our app
        storageRef = storage.getReference();
        //StorageReference referenceToImages=storageRef.child("images");
        recyclerView=findViewById(R.id.recyclerViewMessage);
        editTextMessage=findViewById(R.id.editTextMessage);
        imageViewSendMessage=findViewById(R.id.imageViewSendMessage);
        imageViewAddImage=findViewById(R.id.imageViewAddImage);
        adapter=new MessagesAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        imageViewSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(editTextMessage.getText().toString().trim(),null);
            }
        });
        imageViewAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToGetImage=new Intent(Intent.ACTION_GET_CONTENT);
                intentToGetImage.setType("image/jpeg");
                intentToGetImage.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(intentToGetImage,RC_GET_IMAGE);
            }
        });
        if (mAuth.getCurrentUser()!=null){
            preferences.edit().putString("author",mAuth.getCurrentUser().getEmail()).apply();
        }else {
            signOut();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        db.collection("messages").orderBy("date").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                List<Message> messages= null;
                if (queryDocumentSnapshots != null) {
                    messages = queryDocumentSnapshots.toObjects(Message.class);
                    adapter.setMessages(messages);
                    recyclerView.scrollToPosition(adapter.getItemCount()-1);
                }

            }
        });
    }

    private void sendMessage(String textOfMessage,String urlToImage){
        Message message=null;
        String author=preferences.getString("author","Anonim");
        if (textOfMessage!=null && !textOfMessage.isEmpty()){
            message=new Message(author,textOfMessage,System.currentTimeMillis(),null);
        }else if (urlToImage!=null && !urlToImage.isEmpty()){
            message=new Message(author,null,System.currentTimeMillis(),urlToImage);
        }
        recyclerView.scrollToPosition(adapter.getItemCount()-1);
        if (message != null) {
            db.collection("messages").add(message).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Toast.makeText(MainActivity.this, "Сообщение отправленно", Toast.LENGTH_SHORT).show();
                    editTextMessage.setText("");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Сообщение не отправленно", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void signOut(){
        AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    // Choose authentication providers
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build());

// Create and launch sign-in intent
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==RC_GET_IMAGE && resultCode==RESULT_OK){
            if (data!=null) {
                final Uri uri = data.getData();
                if (uri!=null){
                    final StorageReference referenceToImage=storageRef.child("images/"+uri.getLastPathSegment());
                    UploadTask uploadTask = referenceToImage.putFile(uri);
                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }

                            // Continue with the task to get the download URL
                            return referenceToImage.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                if (downloadUri != null) {
                                    sendMessage(null,downloadUri.toString());
                                }
                            } else {
                                // Handle failures
                                // ...
                            }
                        }
                    });
                }
            }
        }

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = mAuth.getCurrentUser();
                if (user!=null) {
                    Toast.makeText(this, "" + user.getEmail(), Toast.LENGTH_SHORT).show();
                    preferences.edit().putString("author",user.getEmail()).apply();
                }
                // ...
            } else {
                if (response != null) {
                    Toast.makeText(this, "Error: "+response.getError(), Toast.LENGTH_SHORT).show();
                }
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }
}
