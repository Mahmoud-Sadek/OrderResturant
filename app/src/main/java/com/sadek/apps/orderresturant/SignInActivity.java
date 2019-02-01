package com.sadek.apps.orderresturant;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.CheckBox;
import com.sadek.apps.orderresturant.common.Common;
import com.sadek.apps.orderresturant.model.User;

import java.util.HashMap;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignInActivity extends AppCompatActivity {

    EditText edtPhone, edtPassword;
    Button btnSignIn;
    CheckBox ckbRemember;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        edtPhone = (MaterialEditText) findViewById(R.id.edtPhone);
        edtPassword = (MaterialEditText) findViewById(R.id.edtPassword);
        btnSignIn = (Button) findViewById(R.id.bnSignIn);
        ckbRemember = findViewById(R.id.ckbRemember);

        //init Paper
        Paper.init(this);

        //Inti Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref_user = database.getReference("User");
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Common.isConnectedToInternet(getBaseContext())) {

                    //save user& password
                    if (ckbRemember.isChecked()){
                        Paper.book().write(Common.USER_KEY, edtPhone.getText().toString());
                        Paper.book().write(Common.PWD_KEY, edtPassword.getText().toString());
                    }


                    final ProgressDialog mDialog = new ProgressDialog(SignInActivity.this);
                    mDialog.setMessage("Please Waiting ...");
                    mDialog.show();
                    ref_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mDialog.dismiss();
                            //check user exist in database
                            if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {
                                //get User data
//                                User user = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);
                                User user = new User();
                                user.setName(dataSnapshot.child(edtPhone.getText().toString()).child("name").getValue(String.class));
                                user.setIsStuff(dataSnapshot.child(edtPhone.getText().toString()).child("isStuff").getValue(String.class));
                                user.setPassword(dataSnapshot.child(edtPhone.getText().toString()).child("password").getValue(String.class));
                                user.setPhone(edtPhone.getText().toString()); //set Phone
                                user.setHomeAddress((HashMap<String, String>) dataSnapshot.child(edtPhone.getText().toString()).child("homeAddress").getValue());
                                if (user.getPassword().equals(edtPassword.getText().toString())) {
                                    Intent home = new Intent(SignInActivity.this, HomeActivity.class);
                                    Common.currentUser = user;
                                    startActivity(home);
                                    finish();
                                } else {
                                    Toast.makeText(SignInActivity.this, "Wrong Password !!", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(SignInActivity.this, "User Not Exist In Database !!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    Toast.makeText(SignInActivity.this, "Please check your connection", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
}
