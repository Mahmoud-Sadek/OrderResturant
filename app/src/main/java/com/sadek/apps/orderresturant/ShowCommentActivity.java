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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.sadek.apps.orderresturant.Interface.ItemClickListener;
import com.sadek.apps.orderresturant.common.Common;
import com.sadek.apps.orderresturant.database.Database;
import com.sadek.apps.orderresturant.model.Food;
import com.sadek.apps.orderresturant.model.Order;
import com.sadek.apps.orderresturant.model.Rating;
import com.sadek.apps.orderresturant.viewholder.FoodViewHolder;
import com.sadek.apps.orderresturant.viewholder.ShowCommentViewHolder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ShowCommentActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference ref_commentList;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;


    FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder> adapter;
    SwipeRefreshLayout swipeRefreshLayout;

    String foodId = "";



    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_show_comment);


        //int Firebase
        database = FirebaseDatabase.getInstance();
        ref_commentList = database.getReference("Rating");


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
                    foodId = getIntent().getStringExtra(Common.INTENT_FOOD_ID);
                if (!foodId.isEmpty() && foodId != null) {
                    if (Common.isConnectedToInternet(getBaseContext())) {
                        LoadListRatings(foodId);
                    } else {
                        Toast.makeText(ShowCommentActivity.this, "Please check your connection", Toast.LENGTH_SHORT).show();
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
                    foodId = getIntent().getStringExtra(Common.INTENT_FOOD_ID);
                if (!foodId.isEmpty() && foodId != null) {
                    if (Common.isConnectedToInternet(getBaseContext())) {
                        LoadListRatings(foodId);
                    } else {
                        Toast.makeText(ShowCommentActivity.this, "Please check your connection", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                        return;
                    }
                }
            }
        });

        //Load Food
        recyclerView = findViewById(R.id.recycler_comment);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void LoadListRatings(String foodId) {
        Query searchByName = ref_commentList.orderByChild("foodId").equalTo(foodId);
        FirebaseRecyclerOptions<Rating> options = new FirebaseRecyclerOptions.Builder<Rating>()
                .setQuery(searchByName, Rating.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ShowCommentViewHolder viewHolder, final int position, @NonNull final Rating model) {
                viewHolder.txtComment.setText(model.getComment());
                viewHolder.txtUserPhone.setText(model.getUserPhone());
                viewHolder.ratingBar.setRating(Float.parseFloat(model.getRateValue()));

            }

            @NonNull
            @Override
            public ShowCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.comment_item, parent, false);
                return new ShowCommentViewHolder(itemView);
            }
        };
        //Set Adapter
        adapter.startListening();
        Log.d("TAG", "LoadListComment: " + adapter.getItemCount());
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}
