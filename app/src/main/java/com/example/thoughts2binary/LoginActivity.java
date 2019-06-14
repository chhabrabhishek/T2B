package com.example.thoughts2binary;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.drm.DrmStore;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.victor.loading.newton.NewtonCradleLoading;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    WebView webView;
    String user_email,password;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Loading PayBooks ...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                Toast.makeText(LoginActivity.this,"No user found!",Toast.LENGTH_LONG).show();
            } else {
                user_email = extras.getString("user");
                password = extras.getString("password");
                if(password == null)
                    Log.d("lllll","fghj");
                else
                    Log.d("lllll",password);
            }
        } else {
            user_email = (String) savedInstanceState.getSerializable("user");
            password = (String) savedInstanceState.getSerializable("password");
        }

        mAuth = FirebaseAuth.getInstance();
        webView = findViewById(R.id.PayBooks);

        setWebView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.action_settings){
            mAuth.signOut();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        else{
            return super.onOptionsItemSelected(item);
        }
    }

    void setWebView(){
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadUrl("https://apps.paybooks.in/mylogin.aspx?domain=Thoughts");
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webView.loadUrl(
                        "javascript:(function() { " +
                                "var element = document.getElementById('txtUserName');"
                                + "element.setAttribute('value','"+user_email+"');"
                                + "var ele = document.getElementById('txtPassword');"
                                + "ele.setAttribute('value','"+password+"');"
                                + "var btn = document.getElementById('btnLogin');"
                                + "btn.click();" +
                                "})()");
            }
        });
        progressDialog.dismiss();
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
