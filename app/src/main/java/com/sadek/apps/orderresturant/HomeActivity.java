package com.sadek.apps.orderresturant;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.facebook.accountkit.AccountKit;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.sadek.apps.orderresturant.Interface.ItemClickListener;
import com.sadek.apps.orderresturant.common.Common;
import com.sadek.apps.orderresturant.database.Database;
import com.sadek.apps.orderresturant.model.Banner;
import com.sadek.apps.orderresturant.model.Category;
import com.sadek.apps.orderresturant.model.Token;
import com.sadek.apps.orderresturant.viewholder.MenuViewHolder;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int MAX_WIDTH = 400;
    private static final int MAX_HEIGHT = 400;

    FirebaseDatabase database;
    DatabaseReference ref_category;
    TextView textFullName;
    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    CounterFab fab;

    SwipeRefreshLayout swipeRefreshLayout;
    PlaceAutocompleteFragment editAddres;
    HashMap homeAddress = new HashMap<String, String>();
    // slider
    HashMap<String, String> imageList;
    SliderLayout sliderLayout;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);

        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_bright);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    LoadMenu();
                } else {
                    Toast.makeText(HomeActivity.this, "Please check your connection", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                    return;
                }
            }
        });
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    LoadMenu();
                } else {
                    Toast.makeText(HomeActivity.this, "Please check your connection", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                    return;
                }
            }
        });

        //int Firebase
        database = FirebaseDatabase.getInstance();
        ref_category = database.getReference("Category");

        Paper.init(this);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent = new Intent(HomeActivity.this, CartActivity.class);
                startActivity(cartIntent);
            }
        });

        fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //set Name for user
        View headerView = navigationView.getHeaderView(0);
        textFullName = (TextView) headerView.findViewById(R.id.txtFullName);
        textFullName.setText(Common.currentUser.getName());

        //Load menu
        recyclerView = findViewById(R.id.recycler_menu);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recyclerView.getContext(),
                R.anim.layout_fall_down);
        recyclerView.setLayoutAnimation(controller);

        updateToken(FirebaseInstanceId.getInstance().getToken());
        // setup slider
        // call this after  init  firebase database

        setupSlider();


    }

    private void setupSlider() {

        // slider init
        sliderLayout = findViewById(R.id.slider);
        imageList = new HashMap<>();

        final DatabaseReference banner_db_ref = database.getReference("Banner");
        banner_db_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapShort : dataSnapshot.getChildren()) {
                    Banner banner = postSnapShort.getValue(Banner.class);
                    imageList.put(banner.getName() + "@@@" + banner.getId(), banner.getImage());

                }
                imageList.size();

                for (String key : imageList.keySet()) {
                    String[] keySplit = key.split("@@@");
                    String nameOfFood = keySplit[0];
                    String idOfFood = keySplit[1];


                    // create slider
                    final TextSliderView textSliderView = new TextSliderView(getBaseContext());

                    textSliderView.description(nameOfFood)
                            .image(imageList.get(key))
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {

                                    Intent intent = new Intent(HomeActivity.this, FoodDetailActivity.class);
                                    // we will send food id to food detail class
                                    intent.putExtras(textSliderView.getBundle());
                                    startActivity(intent);

                                }
                            });

                    // add extra bundle
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle().putString("FoodId", idOfFood);

                    sliderLayout.addSlider(textSliderView);

                    // remove event after finish
                    banner_db_ref.removeEventListener(this);


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sliderLayout.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
        sliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        sliderLayout.setCustomAnimation(new DescriptionAnimation());
        sliderLayout.setDuration(4000);

    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        LoadMenu();
    }

    private void updateToken(String token) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref_tokens = db.getReference("Tokens");
        Token data = new Token(token, false);
        ref_tokens.child(Common.currentUser.getPhone()).setValue(data);
    }

    private void LoadMenu() {
        fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(ref_category, Category.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.menu_item, parent, false);
                return new MenuViewHolder(itemView);
            }

            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder holder, int position, @NonNull Category model) {
                holder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).resize(MAX_WIDTH, MAX_HEIGHT).into(holder.imageView);
                final Category clickItem = model;
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Get Category and send to new Activity
                        Intent foodList = new Intent(HomeActivity.this, FoodListActivity.class);
                        //Because CategoryId is key , so we just get key of this item
                        foodList.putExtra("CategoryId", adapter.getRef(position).getKey());
                        startActivity(foodList);
                    }
                });
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);

        //Animation
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        sliderLayout.stopAutoCycle();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_search){
            startActivity(new Intent(HomeActivity.this, SearchActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {

        } else if (id == R.id.nav_cart) {
            Intent cartIntent = new Intent(HomeActivity.this, CartActivity.class);
            startActivity(cartIntent);
        } else if (id == R.id.nav_orders) {
            Intent orderIntent = new Intent(HomeActivity.this, OrderStatusActivity.class);
            startActivity(orderIntent);
        } else if (id == R.id.nav_fav) {
            Intent orderIntent = new Intent(HomeActivity.this, FavoriteActivity.class);
            startActivity(orderIntent);
        }  else if (id == R.id.nav_changeName) {
//            Intent orderIntent = new Intent(HomeActivity.this, OrderStatusActivity.class);
//            startActivity(orderIntent);

            //changePasswordDialog();
            changeUserNameDialog();

        } else if (id == R.id.nav_setting) {
            showSettingDialog();
        } else if (id == R.id.nav_log_out) {
            //Delete Remember
            AccountKit.logOut();

            //Logout
            Intent signIn = new Intent(HomeActivity.this, MainActivity.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);
        } /*else if (id == R.id.nav_home_adress) {

            showHomeAddressDialog();

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showSettingDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
        alertDialog.setTitle("SETTINGS");


        LayoutInflater inflater = LayoutInflater.from(this);

        View view = inflater.inflate(R.layout.setting_dialog, null);

        final CheckBox ckb_subscribe_new = view.findViewById(R.id.ckb_sub_new);
        Paper.init(this);
        String isSubscribe = Paper.book().read("sub_new");
        if (isSubscribe == null || TextUtils.isEmpty(isSubscribe) || isSubscribe.equals("false"))
            ckb_subscribe_new.setChecked(false);
        else
            ckb_subscribe_new.setChecked(true);


        alertDialog.setView(view);

        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {


                final android.app.AlertDialog waitingDialog = new SpotsDialog(HomeActivity.this);

                waitingDialog.show();
                if (ckb_subscribe_new.isChecked()) {
                    FirebaseMessaging.getInstance().subscribeToTopic(Common.TopicName);
                    Paper.book().write("sub_new", "true");
                }else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.TopicName);
                    Paper.book().write("sub_new", "false");
                }
                Common.currentUser.setHomeAddress(homeAddress);

                FirebaseDatabase.getInstance().getReference("User")
                        .child(Common.currentUser.getPhone())
                        .setValue(Common.currentUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                waitingDialog.dismiss();

                                Toast.makeText(HomeActivity.this, "Subscribed Successfully", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });

        alertDialog.show();

    }

    private void changeUserNameDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
        alertDialog.setTitle("Update Name");
        alertDialog.setMessage("Please fill all information");


        LayoutInflater inflater = LayoutInflater.from(this);

        final View view = inflater.inflate(R.layout.dialog_update_name, null);

        final MaterialEditText editTextName = view.findViewById(R.id.editName);

        alertDialog.setView(view);
        alertDialog.setIcon(R.drawable.ic_security);
        alertDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {


                // update name here

                Map<String, Object> update_name = new HashMap<>();
                update_name.put("name", editTextName.getText().toString());

                FirebaseDatabase.getInstance().getReference("User")
                        .child(Common.currentUser.getPhone())
                        .updateChildren(update_name)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialog.dismiss();

                                if (task.isSuccessful()) {

                                    // set name  for user
                                    // update the global user
                                    Common.currentUser.setName(editTextName.getText().toString());
                                    textFullName.setText(Common.currentUser.getName());


                                    Toast.makeText(HomeActivity.this, "Name updated!!!", Toast.LENGTH_SHORT).show();
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

    }

    private void showHomeAddressDialog() {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
        alertDialog.setTitle("Change Home Address");
        alertDialog.setMessage("Please fill all information");


        LayoutInflater inflater = LayoutInflater.from(this);

        View view = inflater.inflate(R.layout.home_address_dialog, null);

//        final MaterialEditText editTextHomeAddress = view.findViewById(R.id.editHomeAddress);
        editAddres = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        editAddres.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);

        ((EditText) editAddres.getView().findViewById(R.id.place_autocomplete_search_input)).setHint("enter your address");
        ((EditText) editAddres.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(25);


        // get address from place  auto complete

        editAddres.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {


                homeAddress.put("address", place.getAddress() + "");
                homeAddress.put("lat", place.getLatLng().latitude + "");
                homeAddress.put("lng", place.getLatLng().longitude + "");
                Log.e("position", homeAddress.get("lat") + "  " + homeAddress.get("lng"));
                Toast.makeText(HomeActivity.this, homeAddress.get("lat") + " long" + homeAddress.get("lng"), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Status status) {

            }
        });

        alertDialog.setView(view);


        alertDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {


                final android.app.AlertDialog waitingDialog = new SpotsDialog(HomeActivity.this);

                waitingDialog.show();

                Common.currentUser.setHomeAddress(homeAddress);

                FirebaseDatabase.getInstance().getReference("User")
                        .child(Common.currentUser.getPhone())
                        .setValue(Common.currentUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                waitingDialog.dismiss();

                                Toast.makeText(HomeActivity.this, "Update Address Successfull", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();


            }
        });

        alertDialog.show();

    }

}
