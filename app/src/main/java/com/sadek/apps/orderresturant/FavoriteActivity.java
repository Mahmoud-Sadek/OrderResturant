package com.sadek.apps.orderresturant;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sadek.apps.orderresturant.Interface.RecyclerItemTouchHelperListener;
import com.sadek.apps.orderresturant.common.Common;
import com.sadek.apps.orderresturant.database.Database;
import com.sadek.apps.orderresturant.helper.RecyclerItemTouchHelper;
import com.sadek.apps.orderresturant.model.Favorite;
import com.sadek.apps.orderresturant.model.Order;
import com.sadek.apps.orderresturant.viewholder.CartAdapter;
import com.sadek.apps.orderresturant.viewholder.CartViewHolder;
import com.sadek.apps.orderresturant.viewholder.FavoriteAdapter;
import com.sadek.apps.orderresturant.viewholder.FavoriteViewHolder;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FavoriteActivity extends AppCompatActivity implements RecyclerItemTouchHelperListener {

    FirebaseDatabase database;
    DatabaseReference ref_foodList;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FavoriteAdapter adapter;
    RelativeLayout rootLayout;
    List<Favorite> favorites = new ArrayList<>();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        rootLayout = findViewById(R.id.rootLayout);
        //Load Food
        recyclerView = findViewById(R.id.recycler_fav);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        loadListFavorites();
    }

    private void loadListFavorites() {
        favorites = new Database(this).getAllFavorites(Common.currentUser.getPhone());
        adapter = new FavoriteAdapter(favorites, this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof FavoriteViewHolder) {
            // get the removed item name to display it in snack bar
            String name = (favorites).get(viewHolder.getAdapterPosition()).getFoodName();

            // backup of removed item for undo purpose
            final Favorite deletedItem = favorites.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            // remove the item from recycler view
            adapter.removeItem(viewHolder.getAdapterPosition());

            new Database(getBaseContext()).removeFromFavorites(deletedItem.getFoodId(), Common.currentUser.getPhone());

            // showing snack bar with Undo option
            Snackbar snackbar = Snackbar
                    .make(recyclerView, name + " removed from cart!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // undo is selected, restore the deleted item
                    adapter.restoreItem(deletedItem, deletedIndex);
                    new Database(getBaseContext()).addToFavorites(deletedItem);

                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}
