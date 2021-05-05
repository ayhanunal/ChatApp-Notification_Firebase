package com.ayhanunal.chatapplicationpushnotification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;
    EditText messageText;

    private ArrayList<String> chatMessages = new ArrayList<>();


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.option_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.option_menu_sign_out){
            firebaseAuth.signOut();
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
        }else if(item.getItemId() == R.id.option_menu_profile){
            Intent intent = new Intent(getApplicationContext(),ProfileActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        firebaseAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recycler_view);
        recyclerViewAdapter = new RecyclerViewAdapter(chatMessages);
        messageText = findViewById(R.id.chat_activity_message_text);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator()); //basit animasyon.
        recyclerView.setAdapter(recyclerViewAdapter);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        getData();


        // OneSignal Initialization - Push Notification
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(final String userId, String registrationId) {
                System.out.println(userId);  //this device id -- not all id
 
                UUID uuid = UUID.randomUUID();
                final String uuidString = uuid.toString();

                final DatabaseReference newReferance = firebaseDatabase.getReference("PlayerIDs");
                newReferance.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        ArrayList<String> playerIDsFromServer = new ArrayList<>();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){

                            HashMap<String, String> hashMap = (HashMap<String, String>) snapshot.getValue();
                            String currentPlayerID = hashMap.get("playerID");

                            playerIDsFromServer.add(currentPlayerID);
                        }

                        if(!playerIDsFromServer.contains(userId)){

                            databaseReference.child("PlayerIDs").child(uuidString).child("playerID").setValue(userId);

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



            }
        });



    }


    public void sendMessage(View view){

        final String messageToSend = messageText.getText().toString();

        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String userEmail = firebaseUser.getEmail().toString();

        databaseReference.child("Chats").child(uuidString).child("usermessage").setValue(messageToSend);
        databaseReference.child("Chats").child(uuidString).child("useremail").setValue(userEmail);
        databaseReference.child("Chats").child(uuidString).child("usermessagetime").setValue(ServerValue.TIMESTAMP);

        getData();

        messageText.setText("");

        //OneSignal

        DatabaseReference newReferance = firebaseDatabase.getReference("PlayerIDs");
        newReferance.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                    HashMap<String,String> hashMap = (HashMap<String, String>) snapshot.getValue();

                    String playerID = hashMap.get("playerID");

                    try {
                        OneSignal.postNotification(new JSONObject("{'contents': {'en':'"+messageToSend+"'}, 'include_player_ids': ['" + playerID + "']}"), null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }


    public void getData(){

        try {

            DatabaseReference newReferance = firebaseDatabase.getReference("Chats");

            Query query = newReferance.orderByChild("usermessagetime");

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //veriler yenilendiginde, degistiginde.

                    chatMessages.clear();

                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        HashMap<String,String> hashMap = (HashMap<String, String>) snapshot.getValue();
                        String userEmail = hashMap.get("useremail");
                        String userMessage = hashMap.get("usermessage");

                        chatMessages.add(userEmail + " :" + userMessage);
                        recyclerViewAdapter.notifyDataSetChanged();

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    //db error 
                    Toast.makeText(getApplicationContext(),databaseError.getMessage().toString(),Toast.LENGTH_LONG).show();

                }
            });

        }catch (Exception e){

            e.printStackTrace();
        }




    }


}