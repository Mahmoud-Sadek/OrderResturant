package com.sadek.apps.orderresturant.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sadek.apps.orderresturant.Interface.ItemClickListener;
import com.sadek.apps.orderresturant.R;

/**
 * Created by Mahmoud Sadek on 5/31/2018.
 */

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView foodName, foodPrice, shareImage, commentText;
    public ImageView foodImage,favImage ;
    public Button image_quick_cart;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public FoodViewHolder(View itemView) {
        super(itemView);
        foodName = itemView.findViewById(R.id.food_name);
        commentText = itemView.findViewById(R.id.comment_txt);
        foodPrice = itemView.findViewById(R.id.food_price);
        foodImage = itemView.findViewById(R.id.food_image);
        favImage = itemView.findViewById(R.id.fav);
        shareImage = itemView.findViewById(R.id.share);
        image_quick_cart=itemView.findViewById(R.id.btn_quick_cart);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), false);
    }
}
