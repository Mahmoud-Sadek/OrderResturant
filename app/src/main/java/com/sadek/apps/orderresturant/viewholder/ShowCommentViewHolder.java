package com.sadek.apps.orderresturant.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.sadek.apps.orderresturant.R;

/**
 * Created by Mahmoud Sadek on 11/28/2018.
 */

public class ShowCommentViewHolder extends RecyclerView.ViewHolder{

    public TextView txtUserPhone, txtComment;
    public RatingBar ratingBar;

    public ShowCommentViewHolder(View itemView) {
        super(itemView);
        txtUserPhone = itemView.findViewById(R.id.txtUserPhone);
        txtComment = itemView.findViewById(R.id.txtComment);
        ratingBar = itemView.findViewById(R.id.ratingBar);
    }
}
