package com.sadek.apps.orderresturant;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.sadek.apps.orderresturant.Interface.ItemClickListener;
import com.sadek.apps.orderresturant.common.Common;
import com.sadek.apps.orderresturant.database.Database;
import com.sadek.apps.orderresturant.model.Category;
import com.sadek.apps.orderresturant.model.Favorite;
import com.sadek.apps.orderresturant.model.Food;
import com.sadek.apps.orderresturant.model.Order;
import com.sadek.apps.orderresturant.model.Rating;
import com.sadek.apps.orderresturant.viewholder.FoodViewHolder;
import com.sadek.apps.orderresturant.viewholder.MenuViewHolder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FoodListActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference ref_foodList;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    String categoryId = "";

    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    SwipeRefreshLayout swipeRefreshLayout;

    //Search Functionality
    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    //Favorites
    Database localDB;

    //FacebookShare
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    //create Target from Picasso
    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            //create photo from Bitmap
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            if (ShareDialog.canShow(SharePhotoContent.class)) {
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo).build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        //init facebook
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);


        //int Firebase
        database = FirebaseDatabase.getInstance();
        ref_foodList = database.getReference("Foods");

        //Local DB
        localDB = new Database(this);

        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_bright);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //get Intent here
                if (getIntent() != null)
                    categoryId = getIntent().getStringExtra("CategoryId");
                if (!categoryId.isEmpty() && categoryId != null) {
                    if (Common.isConnectedToInternet(getBaseContext())) {
                        LoadListFood(categoryId);
                    } else {
                        Toast.makeText(FoodListActivity.this, "Please check your connection", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                        return;
                    }
                }
            }
        });
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                //get Intent here
                if (getIntent() != null)
                    categoryId = getIntent().getStringExtra("CategoryId");
                if (!categoryId.isEmpty() && categoryId != null) {
                    if (Common.isConnectedToInternet(getBaseContext())) {
                        LoadListFood(categoryId);
                    } else {
                        Toast.makeText(FoodListActivity.this, "Please check your connection", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                        return;
                    }
                }

                //Search
                materialSearchBar = findViewById(R.id.searchBar);
                materialSearchBar.setHint("Enter your food");
                loadSuggest();
                materialSearchBar.setLastSuggestions(suggestList);
                materialSearchBar.setCardViewElevation(10);
                materialSearchBar.addTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // when user type their text , we will change suggest list

                        List<String> suggest = new ArrayList<>();
                        for (String search : suggestList) {
                            if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                                suggest.add(search);
                        }
                        materialSearchBar.setLastSuggestions(suggest);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                    @Override
                    public void onSearchStateChanged(boolean enabled) {
                        // when search ar is close
                        // Restore original adapter
                        if (!enabled)
                            recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onSearchConfirmed(CharSequence text) {
                        // when search finish
                        // show result of search adapter
                        startSearch(text);
                    }

                    @Override
                    public void onButtonClicked(int buttonCode) {

                    }
                });
            }
        });

        //Load Food
        recyclerView = findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (getIntent() != null)
            categoryId = getIntent().getStringExtra("CategoryId");
        if (!categoryId.isEmpty() && categoryId != null) {
            if (Common.isConnectedToInternet(getBaseContext())) {
                LoadListFood(categoryId);
            } else {
                Toast.makeText(FoodListActivity.this, "Please check your connection", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
                return;
            }
        }
    }

    private void startSearch(CharSequence text) {
        Query searchByName = ref_foodList.orderByChild("name").equalTo(text.toString());
        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(searchByName, Food.class)
                .build();
        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FoodViewHolder viewHolder, final int position, @NonNull final Food model) {
                viewHolder.foodName.setText(model.getName());
                viewHolder.foodPrice.setText(String.format("$ %s", model.getPrice().toString()));
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.foodImage);

                //Add Favorites
                if (localDB.isFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone()))
                    viewHolder.favImage.setImageResource(R.drawable.ic_bookmark_black_24dp);

                //click to share
                viewHolder.shareImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Picasso.with(getApplicationContext())
                                .load(model.getImage())
                                .into(target);
                    }
                });

                //Click to change state of Favorites
                viewHolder.favImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Favorite favorite = new Favorite();
                        favorite.setFoodDescription(model.getDescription());
                        favorite.setFoodDiscount(model.getDiscount());
                        favorite.setFoodId(adapter.getRef(position).getKey());
                        favorite.setFoodImage(model.getImage());
                        favorite.setFoodMenuId(model.getMenuId());
                        favorite.setFoodName(model.getName());
                        favorite.setFoodPrice(model.getPrice());
                        favorite.setUserPhone(Common.currentUser.getPhone());
                        if (!localDB.isFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone())) {
                            localDB.addToFavorites(favorite);
                            viewHolder.favImage.setImageResource(R.drawable.ic_bookmark_black_24dp);
                            Toast.makeText(FoodListActivity.this, model.getName() + "was Added to Favorites", Toast.LENGTH_SHORT).show();
                        } else {
                            localDB.removeFromFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone());
                            viewHolder.favImage.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
                            Toast.makeText(FoodListActivity.this, model.getName() + "was removed to Favorites", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                Query foodRating = database.getReference("Rating").orderByChild("foodId").equalTo(adapter.getRef(position).getKey());
                foodRating.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                         viewHolder.commentText.setText(dataSnapshot.getChildrenCount()+" Comment");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                final Food local = model;
                viewHolder.foodImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //start new Activity
                        Intent foodDetail = new Intent(FoodListActivity.this, FoodDetailActivity.class);
                        foodDetail.putExtra("FoodId", searchAdapter.getRef(position).getKey()); //send Food Id to new activity
                        startActivity(foodDetail);
                    }
                });
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //start new Activity
                        Intent foodDetail = new Intent(FoodListActivity.this, FoodDetailActivity.class);
                        foodDetail.putExtra("FoodId", searchAdapter.getRef(position).getKey()); //send Food Id to new activity
                        startActivity(foodDetail);
                    }
                });
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item2, parent, false);
                return new FoodViewHolder(itemView);
            }
        };
        searchAdapter.startListening();

        recyclerView.setAdapter(searchAdapter);// Set adapter for recycler View is search result
    }

    private void loadSuggest() {
        ref_foodList.orderByChild("menuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Food item = postSnapshot.getValue(Food.class);
                            suggestList.add(item.getName()); // Add name of food to suggest

                        }
                        materialSearchBar.setLastSuggestions(suggestList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void LoadListFood(String categoryId) {
        Query searchByName = ref_foodList.orderByChild("menuId").equalTo(categoryId);
        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(searchByName, Food.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FoodViewHolder viewHolder, final int position, @NonNull final Food model) {
                viewHolder.foodName.setText(model.getName());
                viewHolder.foodPrice.setText(String.format("$ %s", model.getPrice().toString()));
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.foodImage);

                //Add Favorites
                if (localDB.isFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone()))
                    viewHolder.favImage.setImageResource(R.drawable.ic_bookmark_black_24dp);

                //click to share
                viewHolder.shareImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*Picasso.with(getApplicationContext())
                                .load(model.getImage())
                                .into(target);*/
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        String shareBody = "حمل التطبيق لتحصل على هذا الطلب " +"https://play.google.com/store/apps/details?id=com.makhtotat.ok.makhtotat";
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, model.getName());
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        startActivity(Intent.createChooser(sharingIntent, "Share via"));
                    }
                });
                //add to cart
                viewHolder.image_quick_cart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isExist = new Database(getBaseContext()).checkFoodExist(adapter.getRef(position).getKey(), Common.currentUser.getPhone());
                        if (!isExist) {
                            new Database(getBaseContext()).addToCart(new Order(
                                    Common.currentUser.getPhone(),
                                    adapter.getRef(position).getKey(),
                                    model.getName(),
                                    "1",
                                    model.getPrice(),
                                    model.getDiscount(),
                                    model.getImage()
                            ));
                        } else {
                            new Database(getBaseContext()).increaseCart(adapter.getRef(position).getKey(), Common.currentUser.getPhone());
                        }
                        Toast.makeText(FoodListActivity.this, "Added to Cart", Toast.LENGTH_SHORT).show();
                    }
                });

                //Click to change state of Favorites
                viewHolder.favImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Favorite favorite = new Favorite();
                        favorite.setFoodDescription(model.getDescription());
                        favorite.setFoodDiscount(model.getDiscount());
                        favorite.setFoodId(adapter.getRef(position).getKey());
                        favorite.setFoodImage(model.getImage());
                        favorite.setFoodMenuId(model.getMenuId());
                        favorite.setFoodName(model.getName());
                        favorite.setFoodPrice(model.getPrice());
                        favorite.setUserPhone(Common.currentUser.getPhone());

                        if (!localDB.isFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone())) {
                            localDB.addToFavorites(favorite);
                            viewHolder.favImage.setImageResource(R.drawable.ic_bookmark_black_24dp);
                            Toast.makeText(FoodListActivity.this, model.getName() + "was Added to Favorites", Toast.LENGTH_SHORT).show();
                        } else {
                            localDB.removeFromFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone());
                            viewHolder.favImage.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
                            Toast.makeText(FoodListActivity.this, model.getName() + "was removed from Favorites", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                Query foodRating = database.getReference("Rating").orderByChild("foodId").equalTo(adapter.getRef(position).getKey());
                foodRating.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        viewHolder.commentText.setText(dataSnapshot.getChildrenCount()+" Comment");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                final Food local = model;
                viewHolder.foodImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //start new Activity
                        Intent foodDetail = new Intent(FoodListActivity.this, FoodDetailActivity.class);
                        foodDetail.putExtra("FoodId", adapter.getRef(position).getKey()); //send Food Id to new activity
                        startActivity(foodDetail);
                    }
                });
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //start new Activity
                        Intent foodDetail = new Intent(FoodListActivity.this, FoodDetailActivity.class);
                        foodDetail.putExtra("FoodId", adapter.getRef(position).getKey()); //send Food Id to new activity
                        startActivity(foodDetail);
                    }
                });
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item2, parent, false);
                return new FoodViewHolder(itemView);
            }
        };
        //Set Adapter
        adapter.startListening();
        Log.d("TAG", "LoadListFood: " + adapter.getItemCount());
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        if (searchAdapter != null) {
            searchAdapter.stopListening();
        }
    }
}
