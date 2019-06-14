package com.example.thoughts2binary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.commons.lang3.StringUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import in.codeshuffle.typewriterview.TypeWriterView;
import in.shadowfax.proswipebutton.ProSwipeButton;

public class MainActivity extends AppCompatActivity{

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference databaseReference;
    private EditText email,password;
    RelativeLayout relativeLayout;
    ProgressDialog progressDialog;
    String value,me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.made_with_love);
        String first = "Made with ";
        String second = "<font color = '#FF0000'>❤</font>️";
        String third = " for Thoughts2Binary";
        textView.setText(Html.fromHtml(first+second+third));

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.INTERNET},1);
        }

        setTypewriter();
        changeTextFont();
        modifyActionBar();
        login();
        setUpSnackBar();

        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        progressDialog = new ProgressDialog(this);
        mDatabase = FirebaseDatabase.getInstance();
        databaseReference = mDatabase.getReference("password");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                me = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        new android.os.Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        if (mAuth.getCurrentUser() != null){
                            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                            intent.putExtra("user",mAuth.getCurrentUser().getEmail());
                            intent.putExtra("password",me);
                            startActivity(intent);
                        }
                    }
                },
        4000);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setTitle("Loading ...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();
                if (email.getText().toString().trim().equals("") || email.getText().toString().trim() == null) {
                    Toast.makeText(MainActivity.this, "Email field can't be empty!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
                else if(password.getText().toString().trim().equals("") || password.getText().toString().trim() == "null") {
                    Toast.makeText(MainActivity.this, "Password field can't be empty!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
                else {
                    signUpUser();
                    databaseReference.setValue(password.getText().toString());
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            value = dataSnapshot.getValue(String.class);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.w("error_af", "Failed to read value.", databaseError.toException());
                        }
                    });
                }
            }
        });
    }


    @Override
    protected void onStart(){
        super.onStart();
        if (mAuth.getCurrentUser() != null){
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                            intent.putExtra("user",mAuth.getCurrentUser().getEmail());
                            intent.putExtra("password",me);
                            startActivity(intent);
                        }
                    },
            4000);
        }

    }

    @Override
    public void onBackPressed(){

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    void signUpUser(){
        if(mAuth.getCurrentUser() == null){

            mAuth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                progressDialog.dismiss();
                                Toast.makeText(MainActivity.this,"Successfully Signed Up!",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                                intent.putExtra("user",mAuth.getCurrentUser().getEmail());
                                intent.putExtra("password",password.getText().toString());
                                startActivity(intent);
                            }
                            else {
                                if (task.getException().getMessage().equals("The email address is already in use by another account.")) {
                                    signInUser();
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(MainActivity.this, "SignUp Failed :- " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });

        }

    }

    void signInUser(){
        mAuth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this,"Successfully Signed In!",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                            intent.putExtra("user",mAuth.getCurrentUser().getEmail());
                            intent.putExtra("password",password.getText().toString());
                            startActivity(intent);
                        }
                        else{
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this,"Login Failed :- " + task.getException().getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    void login(){
        final ProSwipeButton proSwipeButton = findViewById(R.id.awesome_btn);
        proSwipeButton.setOnSwipeListener(new ProSwipeButton.OnSwipeListener() {
            @Override
            public void onSwipeConfirm() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        proSwipeButton.showResultIcon(true);
                        proSwipeButton.setVisibility(View.INVISIBLE);
                        FloatingActionButton fab = findViewById(R.id.fab);
                        fab.setVisibility(View.VISIBLE);
                        email.setVisibility(View.VISIBLE);
                        password.setVisibility(View.VISIBLE);
                    }
                },0);
            }
        });
    }

    void modifyActionBar(){
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    void setTypewriter(){
        TypeWriterView typeWriterView = findViewById(R.id.typeWriterView);
        typeWriterView.setDelay(2000);
        typeWriterView.setWithMusic(false);
        typeWriterView.animateText("Welcome to the official app of T2B !");

    }

    void changeTextFont(){
        Typeface type = Typeface.createFromAsset(getAssets(),"fonts/Merriweather-Bold.ttf");
        TypeWriterView typeWriterView = findViewById(R.id.typeWriterView);
        typeWriterView.setTypeface(type);
    }

    void setUpSnackBar(){
        relativeLayout = findViewById(R.id.relative_layout);
        Snackbar snackbar = Snackbar.make(relativeLayout, Html.fromHtml("<font color = \"#FFFFFF\">Signup/Login with your T2B Email!</font>"),Snackbar.LENGTH_INDEFINITE);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimaryLogin));
        snackbar.show();
    }
}