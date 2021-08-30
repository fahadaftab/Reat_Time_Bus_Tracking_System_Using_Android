package com.aftab.rec;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import util.SessionManager;

public class RoleActivity extends AppCompatActivity implements View.OnClickListener{


    private EditText username,password;
    private Button login,registerDriver,registerStudent;
    private FirebaseAuth auth;
    private ProgressDialog dialog;
    private Button about_us;
    String[] permissions = new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role);
        String keyUserEmail=new SessionManager(getApplicationContext()).getKeyUserEmail();
        if (keyUserEmail.equals("")){
            setContentView(R.layout.activity_role);
            this.auth = FirebaseAuth.getInstance();
            this.dialog = new ProgressDialog(this);
            checkPermissions();
            init();
        }else {
            finish();
            startActivity(new Intent(RoleActivity.this,RouteActivity.class));
            //  startActivity(new Intent(RoleActivity.this,MapsActivity.class));
        }

    }


    private boolean checkPermissions() {
        ArrayList arrayList = new ArrayList();
        for (String str : this.permissions) {
            if (ContextCompat.checkSelfPermission(this, str) != 0) {
                arrayList.add(str);
            }
        }
        if (arrayList.isEmpty()) {
            return true;
        }
        ActivityCompat.requestPermissions(this, (String[]) arrayList.toArray(new String[arrayList.size()]), 100);
        return false;
    }

    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == 100) {
            for (int i2 = 0; i2 < strArr.length; i2++) {
                if (iArr[i2] != 0 && Build.VERSION.SDK_INT >= 23) {
                    requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"}, 100);
                }
            }
        }
    }
    public void init(){
        username =  (EditText) findViewById(R.id.username);
        password =(EditText) findViewById(R.id.password);
        login=(Button) findViewById(R.id.login);
        registerDriver =(Button) findViewById(R.id.registerDriver);
        registerStudent = (Button) findViewById(R.id.registerStudent);


        login.setOnClickListener(this);
        registerDriver.setOnClickListener(this);
        registerStudent.setOnClickListener(this);


        about_us=(Button)findViewById(R.id.about_us);
        about_us.setOnClickListener(this);



    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.login:
                if (TextUtils.isEmpty(username.getText())){
                    username.setError("Field is Empty");
                }else if (TextUtils.isEmpty(password.getText())){
                    password.setError("Field is Empty");
                }else {
                    login(""+username.getText(),""+password.getText());
                    // startActivity(new Intent(RoleActivity.this,RouteActivity.class));
                    // Toast.makeText(getApplicationContext(),username.getText()+" "+password.getText(),Toast.LENGTH_LONG).show();
                }

                break;

            case R.id.registerDriver:

                startActivity(new Intent(RoleActivity.this,DriverRegisterActivity.class));
                break;

            case R.id.registerStudent:
                startActivity(new Intent(RoleActivity.this,SudentRegisterActivity.class));

                break;


            case R.id.about_us:

                startActivity(new Intent(RoleActivity.this,InformationActivity.class));
                break;

        }
    }

    public void login(final String email, final String password){
        this.dialog.setMessage("Logging in. Please wait.");
        this.dialog.show();


        this.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    new SessionManager(getApplicationContext()).setLogin("",""+email,""+password);
                    RoleActivity.this.dialog.dismiss();
                    Intent intent = new Intent(RoleActivity.this, RouteActivity.class);
                    RoleActivity.this.startActivity(intent);
                    RoleActivity.this.finish();
                    return;
                }
                Toast.makeText(RoleActivity.this.getApplicationContext(), "Wrong email/password combination. Try again.", Toast.LENGTH_LONG).show();
                RoleActivity.this.dialog.dismiss();
            }
        });
    }

}

