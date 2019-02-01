package com.sadek.apps.orderresturant.viewholder;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sadek.apps.orderresturant.FavoriteActivity;
import com.sadek.apps.orderresturant.FoodDetailActivity;
import com.sadek.apps.orderresturant.FoodListActivity;
import com.sadek.apps.orderresturant.Interface.ItemClickListener;
import com.sadek.apps.orderresturant.R;
import com.sadek.apps.orderresturant.common.Common;
import com.sadek.apps.orderresturant.database.Database;
import com.sadek.apps.orderresturant.model.Favorite;
import com.sadek.apps.orderresturant.model.Food;
import com.sadek.apps.orderresturant.model.Order;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mahmoud Sadek on 12/5/2018.
 */

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteViewHolder> {

    private List<Favorite> listData = new ArrayList<>();
    FavoriteActivity context;

    public FavoriteAdapter(List<Favorite> listData, FavoriteActivity context) {
        this.listData = listData;
        this.context = context;
    }

    @Override
    public FavoriteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.favorite_item, parent, false);
        return new FavoriteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FavoriteViewHolder viewHolder, final int position) {
        viewHolder.foodName.setText(listData.get(position).getFoodName());
        viewHolder.foodPrice.setText(String.format("$ %s", listData.get(position).getFoodPrice().toString()));
        Picasso.with(context).load(listData.get(position).getFoodImage()).into(viewHolder.foodImage);

        //add to cart
        viewHolder.image_quick_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isExist = new Database(context).checkFoodExist(listData.get(position).getFoodId(), Common.currentUser.getPhone());
                if (!isExist) {
                    new Database(context).addToCart(new Order(
                            Common.currentUser.getPhone(),
                            listData.get(position).getFoodId(),
                            listData.get(position).getFoodName(),
                            "1",
                            listData.get(position).getFoodPrice(),
                            listData.get(position).getFoodDiscount(),
                            listData.get(position).getFoodImage()
                    ));
                } else {
                    new Database(context).increaseCart(listData.get(position).getFoodId(), Common.currentUser.getPhone());
                }
                Toast.makeText(context, "Added to Cart", Toast.LENGTH_SHORT).show();
            }
        });


        final Favorite local = listData.get(position);
        viewHolder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                //start new Activity
                Intent foodDetail = new Intent(context, FoodDetailActivity.class);
                foodDetail.putExtra("FoodId", listData.get(position).getFoodId()); //send Food Id to new activity
                context.startActivity(foodDetail);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public void removeItem(int position) {
        listData.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
    }

    public void restoreItem(Favorite item, int position) {
        listData.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }
}