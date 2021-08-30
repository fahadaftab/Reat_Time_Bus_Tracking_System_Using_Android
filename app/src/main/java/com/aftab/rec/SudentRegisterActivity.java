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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import model.User;
import util.SessionManager;

public class SudentRegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    ProgressDialog dialog;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef=null;
    private Button regsiterStudent=null;
    private ImageView back_student=null;
    private EditText name, email,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudent_register);
        myRef = database.getReference("Users");
        mAuth = FirebaseAuth.getInstance();

        init();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

    }

    public void init(){
        regsiterStudent= (Button) findViewById(R.id.registerStudent);
        back_student= (ImageView) findViewById(R.id.back_student);
        back_student.setOnClickListener(this);
        name =(EditText) findViewById(R.id.name);
        email =(EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);

        regsiterStudent.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.back_student:
                onBackPressed();
                break;
            case R.id.registerStudent:
                if(TextUtils.isEmpty(name.getText())){
                    name.setError("Field is Empty");
                }else if (TextUtils.isEmpty(email.getText())){
                    email.setError("Field is Empty");
                }else if (TextUtils.isEmpty(password.getText())){
                    password.setError("Field is Empty");
                }else {


                  //  createStudent(""+name.getText(),""+email.getText(),""+password.getText());

                  userRegistration(""+name.getText(),""+email.getText(),""+password.getText());
                    //  startActivity(new Intent(SudentRegisterActivity.this,RouteActivity.class));
                    // Toast.makeText(getApplicationContext(),name.getText()+" "+email.getText()+" "+password.getText(),Toast.LENGTH_LONG).show();
                }

                break;
        }
    }



    public void userRegistration(final String name,final String email, final String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            createStudent(""+name,""+email,password);
                        } else {
                            // If sign in fails, display a message to the user.

                            Toast.makeText(SudentRegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }

    private void createStudent(final String name,final String email, final String password){
        myRef.child(mAuth.getUid()).setValue(new User(""+email,""+name,""+password))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            new SessionManager(getApplicationContext()).setLogin(""+name,""+email,""+password);
                            SudentRegisterActivity.this.finish();
                            Intent intent = new Intent(SudentRegisterActivity.this, RouteActivity.class);
                            startActivity(intent);
                            Toast.makeText(SudentRegisterActivity.this, "Account created successfully", Toast.LENGTH_LONG).show();

                        }else {
                            Toast.makeText(SudentRegisterActivity.this.getApplicationContext(), "Check Your Connection", Toast.LENGTH_LONG).show();

                        }
                    }
                });
    }

}
