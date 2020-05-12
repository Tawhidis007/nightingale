package com.hriportfolio.nightingale;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.hriportfolio.nightingale.Utilities.KeyString;
import com.hriportfolio.nightingale.Utilities.SharedPreferenceManager;
import com.hriportfolio.nightingale.Utilities.Utils;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.email_editText)
    EditText email_editText;

    @BindView(R.id.password_editText)
    EditText password_editText;

    @BindView(R.id.signUp_textView)
    TextView signup_textView;

    private String email;
    private String password;

    SharedPreferenceManager preferenceManager;

    private ProgressDialog pd;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        preferenceManager = new SharedPreferenceManager(this, KeyString.PREF_NAME);
        initPref();
        mAuth = FirebaseAuth.getInstance();

        signup_textView.setOnClickListener(view -> {
            Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(i);
        });
    }

    private void initPref(){
        preferenceManager = new SharedPreferenceManager(this, KeyString.PREF_NAME);
        if(preferenceManager.getValue(KeyString.SIGN_IN_FLAG,false)){
            Intent intent = new Intent(LoginActivity.this, Home.class);
            startActivity(intent);
        }
    }

    @OnClick(R.id.login_button)
    void login_button_Pressed() {
        email = email_editText.getText().toString();
        password = password_editText.getText().toString();
        signInUser(email, password);
    }

    private void signInUser(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(LoginActivity.this,
                    "Please fill out all the fields", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this,
                    "Please fill out all the fields", Toast.LENGTH_SHORT).show();
        } else {
            pd = Utils.createProgressDialog(this);
            pd.show();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    pd.dismiss();
                    preferenceManager.setValue(KeyString.SIGN_IN_FLAG, true);
                    Intent i = new Intent(LoginActivity.this, Home.class);
                    startActivity(i);
                } else {
                    Log.d("login_prob", task.getException().toString());
                    Toast.makeText(LoginActivity.this,
                            "Sign in Failed!", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        }
    }
}
