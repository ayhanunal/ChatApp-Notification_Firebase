package com.ayhanunal.chatapplicationpushnotification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    EditText emailText, passwordText;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailText = findViewById(R.id.user_email_edit_text);
        passwordText = findViewById(R.id.user_password_edit_text);

        firebaseAuth = FirebaseAuth.getInstance();

		//current user 
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null){
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
        }

    }


    public void signIn(View view){

        firebaseAuth.signInWithEmailAndPassword(emailText.getText().toString(),passwordText.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){

                            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                            startActivity(intent);

                        }else{
                            Toast.makeText(MainActivity.this, "Failed/"+task.getException().toString(),Toast.LENGTH_LONG).show();
                        }

                    }
                });


    }

    public void signUp(View view){

        firebaseAuth.createUserWithEmailAndPassword(emailText.getText().toString(),passwordText.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //success

                            //FirebaseUser firebaseUser = firebaseAuth.getCurrentUser(); //guncel kullaniciyi al.
                            //String userEmail = firebaseUser.getEmail().toString();

                            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                            startActivity(intent);


                        }else{
                            Toast.makeText(MainActivity.this, "Failed/"+task.getException().toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }
}