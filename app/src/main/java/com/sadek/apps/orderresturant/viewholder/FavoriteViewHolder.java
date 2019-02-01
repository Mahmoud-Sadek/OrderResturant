package com.sadek.apps.orderresturant.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sadek.apps.orderresturant.Interface.ItemClickListener;
import com.sadek.apps.orderresturant.R;

/**
 * Created by Mahmoud Sadek on 5/31/2018.
 */

public class FavoriteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView foodName, foodPrice;
    public ImageView foodImage, image_quick_cart ;

    public RelativeLayout viewBackground;
    public LinearLayout viewForeground;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public FavoriteViewHolder(View itemView) {
        super(itemView);
        foodName = itemView.findViewById(R.id.food_name);
        foodPrice = itemView.findViewById(R.id.food_price);
        foodImage = itemView.findViewById(R.id.food_image);
        image_quick_cart=itemView.findViewById(R.id.btn_quick_cart);
        viewBackground = itemView.findViewById(R.id.view_background);
        viewForeground = itemView.findViewById(R.id.view_foreground);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), false);
    }
}
