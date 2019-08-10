package com.example.purenote;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginMenu extends AppCompatActivity {

    private Button loginButton;
    private TextInputLayout loginInput, passwordInput;
    private FirebaseAuth mAuth;
    private FirebaseUser user;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        mAuth=FirebaseAuth.getInstance();

        loginButton=findViewById(R.id.button3);
        loginInput=findViewById(R.id.textInputLayout);
        passwordInput=findViewById(R.id.textInputLayout2);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    String email = loginInput.getEditText().getText().toString();
                    String password = passwordInput.getEditText().getText().toString();

                    mAuth.createUserWithEmailAndPassword(email,password);
                    mAuth.signInWithEmailAndPassword(email,password);

                }catch (NullPointerException npe){
                    npe.printStackTrace();
                    Log.i("Login","Empty field");
                    Snackbar.make(findViewById(R.id.linearLayout2),"Please fill in the required fields!", BaseTransientBottomBar.LENGTH_SHORT).show();
                }

                if(mAuth.getCurrentUser()!=null){
                    Intent i=new Intent(LoginMenu.this,MainActivity.class);
                    startActivity(i);
                }


            }
        });



    }
}
