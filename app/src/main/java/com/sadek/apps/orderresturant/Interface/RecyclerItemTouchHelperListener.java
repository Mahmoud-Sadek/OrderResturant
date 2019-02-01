package com.sadek.apps.orderresturant.Interface;

import android.support.v7.widget.RecyclerView;

/**
 * Created by Mahmoud Sadek on 12/4/2018.
 */

public interface RecyclerItemTouchHelperListener {
    void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
}
