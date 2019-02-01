package com.sadek.apps.orderresturant;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.sadek.apps.orderresturant.common.Common;
import com.sadek.apps.orderresturant.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 212;
    Button btnContinue;
    TextView txtSlogan;
    MaterialEditText editTextName;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AccountKit.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

//        printKeyHask();

        btnContinue = (Button) findViewById(R.id.btn_continue);
        txtSlogan = (TextView) findViewById(R.id.textSlogan);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/restaurant_font.otf");
        txtSlogan.setTypeface(face);


        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLoginSystem();
            }
        });



        // check  Session Facebook account kit

        if (AccountKit.getCurrentAccessToken()!=null)
        {
            // create dialog

            // show dialog
            final SpotsDialog  waitingDialog=new SpotsDialog(this);
            waitingDialog.show();
            waitingDialog.setMessage("Please wait..");
            waitingDialog.setCancelable(false);

            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(Account account) {
                    // copy code from exits user

                    FirebaseDatabase database= FirebaseDatabase.getInstance();
                    final DatabaseReference user_db_ref= database.getReference("User");

                    user_db_ref.child(account.getPhoneNumber().toString())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    User localUser=dataSnapshot.getValue(User.class);

                                    Common.currentUser = localUser;

                                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    waitingDialog.dismiss();
                                    finish();


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                }

                @Override
                public void onError(AccountKitError accountKitError) {

                }
            });

        }




    }

    private void startLoginSystem() {
        Intent intent=new Intent(this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder=
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                LoginType.PHONE, AccountKitActivity.ResponseType.TOKEN );

        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,configurationBuilder.build());
        startActivityForResult(intent,REQUEST_CODE);
    }

    //ctrl+0;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==REQUEST_CODE)
        {
            AccountKitLoginResult result= data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if (result.getError()!=null)
            {
                Toast.makeText(this, ""+result.getError().getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            else if (result.wasCancelled()){
                Toast.makeText(this, "cancel", Toast.LENGTH_SHORT).show();
                return;
            }
            else {
                if (result.getAccessToken()!=null)
                {
                    // show dialog
                    final android.app.AlertDialog waitingDialog=new SpotsDialog(this);
                    waitingDialog.show();
                    waitingDialog.setMessage("Please wait..");
                    waitingDialog.setCancelable(false);


                    // get current phone
                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(Account account) {
                            final String userPhone= account.getPhoneNumber().toString();

                            // check if exists on fireabse Users

                            Log.e("phone.....",userPhone);

                            FirebaseDatabase database= FirebaseDatabase.getInstance();
                            final DatabaseReference user_db_ref= database.getReference("User");

                            user_db_ref.orderByKey().equalTo(userPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.child(userPhone).exists())
                                    {
                                        Log.e("user doesnt exit.....","singup baby");
                                        // we will create new user and login

                                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                        alertDialog.setTitle("Please enter Name");
                                        alertDialog.setMessage("Please fill all information");
                                        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);

                                        View view = inflater.inflate(R.layout.dialog_update_name, null);

                                        editTextName = view.findViewById(R.id.editName);

                                        alertDialog.setView(view);
                                        alertDialog.setIcon(R.drawable.ic_security);
                                        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();

                                                User object=new User();
                                                object.setPhone(userPhone);
                                                object.setName(editTextName.getText().toString());
                                                object.setIsStuff("false");


                                                // now add to firebase

                                                user_db_ref.child(userPhone)
                                                        .setValue(object)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful())
                                                                {
                                                                    Toast.makeText(MainActivity.this, "User created successfully!!", Toast.LENGTH_SHORT).show();
                                                                    // Login
                                                                    user_db_ref.child(userPhone)
                                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot dataSnapshot) {

//                                                                                    User localUser=dataSnapshot.getValue(User.class);
                                                                                    User localUser = new User();
                                                                                    localUser.setName(dataSnapshot.child("name").getValue(String.class));
                                                                                    localUser.setIsStuff(dataSnapshot.child("isStuff").getValue(String.class));
                                                                                    localUser.setPassword(dataSnapshot.child("password").getValue(String.class));
                                                                                    localUser.setPhone(userPhone); //set Phone
                                                                                    localUser.setHomeAddress((HashMap<String, String>) dataSnapshot.child("homeAddress").getValue());

                                                                                    Common.currentUser = localUser;

                                                                                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                    startActivity(intent);
                                                                                    waitingDialog.dismiss();
                                                                                    finish();


                                                                                }

                                                                                @Override
                                                                                public void onCancelled(DatabaseError databaseError) {

                                                                                }
                                                                            });

                                                                }
                                                            }
                                                        });
                                            }
                                        });

                                        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();

                                            }
                                        });

                                        alertDialog.show();

//                                        userInformationDialog();

                                    }
                                    else // if exits
                                    {
                                        Log.e("user exits.....","yoo baby");
                                        // just login  to home activity
                                        user_db_ref.child(userPhone)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                        User localUser=dataSnapshot.getValue(User.class);

                                                        Common.currentUser = localUser;

                                                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                        waitingDialog.dismiss();
                                                        finish();


                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {
                            Toast.makeText(MainActivity.this, ""+accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });


                }
            }
        }



    }


    private void printKeyHask() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.sadek.apps.orderresturant",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    private void login(final String phone, final String pwd) {
        //Inti Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref_user = database.getReference("User");
        if (Common.isConnectedToInternet(getBaseContext())) {

            final ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
            mDialog.setMessage("Please Waiting ...");
            mDialog.show();
            ref_user.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mDialog.dismiss();
                    //check user exist in database
                    if (dataSnapshot.child(phone).exists()) {
                        //get User data
//                        User user = dataSnapshot.child(phone).getValue(User.class);
                        User user = new User();
                        user.setName(dataSnapshot.child(phone).child("name").getValue(String.class));
                        user.setIsStuff(dataSnapshot.child(phone).child("isStuff").getValue(String.class));
                        user.setPassword(dataSnapshot.child(phone).child("password").getValue(String.class));
                        user.setPhone(phone); //set Phone
                        user.setHomeAddress((HashMap<String, String>) dataSnapshot.child(phone).child("homeAddress").getValue());
                        if (user.getPassword().equals(pwd)) {
                            Intent home = new Intent(MainActivity.this, HomeActivity.class);
                            Common.currentUser = user;
                            startActivity(home);
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Wrong Password !!", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(MainActivity.this, "User Not Exist In Database !!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            Toast.makeText(MainActivity.this, "Please check your connection", Toast.LENGTH_SHORT).show();
            return;
        }
    }
}
