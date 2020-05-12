package com.hriportfolio.nightingale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.hriportfolio.nightingale.Utilities.KeyString;
import com.hriportfolio.nightingale.Utilities.SharedPreferenceManager;
import com.hriportfolio.nightingale.Utilities.Utils;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    View view;

    @BindView(R.id.register_email_editText)
    EditText register_email_editText;

    @BindView(R.id.register_password_editText)
    EditText register_password_editText;

    private TextView register_login_textView;

    //second part registration
    private EditText register_name_editText;
    private EditText register_about_editText;
    private CircleImageView register_upload_pix;
    private Button submit_button;
    private ImageView reg_2_back_button;

    private String userId;
    private String email;
    private String password;
    private String name;
    private String aboutMe;
    private String propicUrl;

    private ProgressDialog pd;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private StorageReference storageProPicRef;
    private StorageTask uploadTask;
    Uri imageUri;

    SharedPreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        LayoutInflater inflater = LayoutInflater.from(this);

        preferenceManager = new SharedPreferenceManager(this, KeyString.PREF_NAME);
        mAuth = FirebaseAuth.getInstance();
        storageProPicRef = FirebaseStorage.getInstance().getReference().child("Profile Pictures");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");


        //second part of registration
        view = inflater.inflate(R.layout.activity_register_page_2, null);
        register_name_editText = view.findViewById(R.id.register_name_editText);
        register_about_editText = view.findViewById(R.id.register_about_editText);
        register_upload_pix = view.findViewById(R.id.register_upload_pix);
        submit_button = view.findViewById(R.id.register_submit_button);
        reg_2_back_button = view.findViewById(R.id.back_from_register_2_button);

        register_upload_pix.setOnClickListener(view -> {
            CropImage.activity().setAspectRatio(1, 1).start(RegisterActivity.this);
        });
        reg_2_back_button.setOnClickListener(view -> {
            Intent i = new Intent(RegisterActivity.this, RegisterActivity.class);
            startActivity(i);
        });

        //first part of registration
        register_login_textView = findViewById(R.id.register_login_textView);
        register_login_textView.setOnClickListener(view -> {
            Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(i);
        });

        submit_button.setOnClickListener(view -> {
            name = register_name_editText.getText().toString();
            aboutMe = register_about_editText.getText().toString();
            saveDataToDatabase();
        });

    }

    private void saveDataToDatabase() {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(RegisterActivity.this,
                    "Please fill out all the fields", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(aboutMe)) {
            Toast.makeText(RegisterActivity.this,
                    "Please fill out all the fields", Toast.LENGTH_SHORT).show();
        }else {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Setting Account Information");
            progressDialog.setMessage("Please wait while we update your information");
            progressDialog.show();

            if (imageUri != null) {
                final StorageReference fileRef = storageProPicRef.child(mAuth.getCurrentUser().getUid() + ".jpg");
                uploadTask = fileRef.putFile(imageUri);
                uploadTask.continueWithTask((Continuation) task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();
                            propicUrl = downloadUrl.toString();

                            HashMap<String, Object> userMap = new HashMap<>();
                            userMap.put("uid", mAuth.getCurrentUser().getUid());
                            userMap.put("name", name);
                            userMap.put("about", aboutMe);
                            userMap.put("image", propicUrl);

                            //sharedpref
                            preferenceManager.setValue(KeyString.USER_NAME, name);
                            preferenceManager.setValue(KeyString.ABOUT_USER, aboutMe);
                            preferenceManager.setValue(KeyString.PROFILE_PICTURE_URL, propicUrl);
                            preferenceManager.setValue(KeyString.FIREBASE_UID, userId);

                            databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);
                            progressDialog.dismiss();
                            Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(i);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Database update error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Image is not selected", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void registerUser(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(RegisterActivity.this,
                    "Please fill out all the fields", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(RegisterActivity.this,
                    "Please fill out all the fields", Toast.LENGTH_SHORT).show();
        }
        if (password.length() < 6) {
            Toast.makeText(RegisterActivity.this,
                    "Length should be at least 6!", Toast.LENGTH_SHORT).show();
        }
         else {
            pd = Utils.createProgressDialog(this);
            pd.show();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    userId = mAuth.getCurrentUser().getUid();
                    databaseReference.child(userId).setValue(true);
                    Toast.makeText(RegisterActivity.this,
                            "Registration complete!", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                    this.setContentView(view);
                } else {
                    Log.d("reg_prob", task.getException().toString());
                    Toast.makeText(RegisterActivity.this,
                            "Registration Failed!", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        }
    }

    @OnClick(R.id.back_from_register_button)
    void back_from_register_button_press() {
        Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(i);
    }

    @OnClick(R.id.register_next_button)
    void registerNextPage() {
        email = register_email_editText.getText().toString();
        password = register_password_editText.getText().toString();

        registerUser(email, password);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            register_upload_pix.setImageURI(imageUri);
        } else {
            this.setContentView(view);
            Toast.makeText(RegisterActivity.this, "Error! Try Again!", Toast.LENGTH_SHORT).show();
        }
    }

}
