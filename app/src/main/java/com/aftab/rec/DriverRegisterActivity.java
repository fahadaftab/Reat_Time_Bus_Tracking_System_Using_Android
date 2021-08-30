package com.aftab.rec;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import model.Driver;
import util.SessionManager;
import util.ToastMessage;

public class DriverRegisterActivity extends AppCompatActivity implements View.OnClickListener{

    FirebaseAuth auth;
    DatabaseReference databaseReference;
    ProgressDialog dialog;
    FirebaseUser user;

    private EditText name,email,password,bus_number;
    private Button register=null;
    private ImageView back_driver=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_register);
        this.auth = FirebaseAuth.getInstance();
        this.dialog = new ProgressDialog(this);
        init();
    }

    public void init(){
        back_driver =(ImageView) findViewById(R.id.back_driver);
        back_driver.setOnClickListener(this);
        name =(EditText) findViewById(R.id.name_driver);
        email = (EditText) findViewById(R.id.email_driver);
        password = (EditText) findViewById(R.id.password_driver);
        bus_number = (EditText) findViewById(R.id.busNumber_driver);
        register= (Button)findViewById(R.id.register_driver);
        register.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.back_driver:
                onBackPressed();
                break;
            case R.id.register_driver:
                if (TextUtils.isEmpty(name.getText())){
                    name.setError("Field is Empty");
                }else if (TextUtils.isEmpty(email.getText())){
                    email.setError("Field is Empty");
                }else if (TextUtils.isEmpty(password.getText())){
                    password.setError("Field is Empty");
                }else if (TextUtils.isEmpty(bus_number.getText())){
                    bus_number.setError("Field is Empty");
                }else {

                    this.dialog.setTitle("Creating account");
                    this.dialog.setMessage("Please wait");
                    this.dialog.show();
                    driverRegistration(""+name.getText(),""+bus_number.getText(),""+email.getText(),""+password.getText());
                    //   startActivity(new Intent(DriverRegisterActivity.this,RouteActivity.class));
                }

                break;
        }
    }


    public void driverRegistration(final String name, final String vehicle,final String email,final String password) {
        this.auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            public void onComplete(@NonNull Task<AuthResult> task) {
                DriverRegisterActivity driverRegistrationActivity;
                if (task.isSuccessful()) {
                    Driver driver = new Driver(""+name,""+email,""+password,""+vehicle,"33.652037", "73.156598");
                    driverRegistrationActivity = DriverRegisterActivity.this;
                    driverRegistrationActivity.user = driverRegistrationActivity.auth.getCurrentUser();
                    DriverRegisterActivity.this.databaseReference = FirebaseDatabase.getInstance().getReference().child("Drivers").child(DriverRegisterActivity.this.user.getUid());
                    DriverRegisterActivity.this.databaseReference.setValue(driver).addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                new SessionManager(getApplicationContext()).setLogin("",""+email,""+password);

                                DriverRegisterActivity.this.dialog.dismiss();
                                //Toast.makeText(DriverRegisterActivity.this, "Account created successfully", Toast.LENGTH_LONG).show();
                                new ToastMessage(DriverRegisterActivity.this).showSmallCustomToast("Account created successfully");
                                DriverRegisterActivity.this.finish();
                                Intent intent = new Intent(DriverRegisterActivity.this, RouteActivity.class);

                                DriverRegisterActivity.this.startActivity(intent);
                                return;
                            }
                            new ToastMessage(DriverRegisterActivity.this).showSmallCustomToast("Could not register driver");

                            //Toast.makeText(DriverRegisterActivity.this, "Could not register driver", Toast.LENGTH_LONG).show();
                            DriverRegisterActivity.this.dialog.dismiss();
                        }
                    });
                    return;
                }
                driverRegistrationActivity = DriverRegisterActivity.this;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Could not register. ");
                stringBuilder.append(task.getException().getMessage());
                // Toast.makeText(driverRegistrationActivity, stringBuilder.toString(), Toast.LENGTH_LONG).show();
                DriverRegisterActivity.this.dialog.dismiss();
            }
        });
    }

}
