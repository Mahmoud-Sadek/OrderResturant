package com.sadek.apps.orderresturant;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.sadek.apps.orderresturant.Interface.ItemClickListener;
import com.sadek.apps.orderresturant.common.Common;
import com.sadek.apps.orderresturant.model.Food;
import com.sadek.apps.orderresturant.model.Request;
import com.sadek.apps.orderresturant.viewholder.FoodViewHolder;
import com.sadek.apps.orderresturant.viewholder.OrderViewHolder;

public class OrderStatusActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference requests_db_ref;

    RecyclerView recyclerView_orderStatus;
    RecyclerView.LayoutManager layoutManager;

    public  FirebaseRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);



        // init firebase
        database= FirebaseDatabase.getInstance();
        requests_db_ref= database.getReference("Requests");

        recyclerView_orderStatus=findViewById(R.id.listOrders);
        layoutManager=new LinearLayoutManager(this);
        recyclerView_orderStatus.setHasFixedSize(true);
        recyclerView_orderStatus.setLayoutManager(layoutManager);


        //Note

        // if we  start orderStatus from Home Activity then
        // we will not put any extra ,so we just load order from Common

        if (getIntent().getExtras()==null) {
            loadOrders(Common.currentUser.getPhone());
        }
        else
        {
            if (getIntent().getStringExtra("userPhone") == null )
                loadOrders(Common.currentUser.getPhone());
            else
                loadOrders(getIntent().getStringExtra("userPhone"));
        }
    }

    private void loadOrders(String phone) {


        // DatabaseReference ref=FirebaseDatabase.getInstance().getReference();
        Query query = requests_db_ref.orderByChild("phone").equalTo(phone);


        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(query, Request.class)
                .build();


        adapter=new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.order_item, parent, false);

                return new OrderViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder holder, final int position, @NonNull final Request model) {


                holder.txtOrderID.setText(adapter.getRef(position).getKey());

                // convert timeStamp to Actual date then set on textView

                holder.txtOrderPhone.setText(model.getPhone());
                holder.txtOrderAddress.setText(model.getAddress());
                holder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                holder.textOrderDate.setText(Common.getDate(Long.parseLong(adapter.getRef(position).getKey())));
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Common.currentKey = adapter.getRef(position).getKey();
                        startActivity(new Intent(OrderStatusActivity.this, TrackingOrderActivity.class));
                    }
                });

                holder.btn_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String st=model.getStatus();
                        Log.e("status",st);

                        if (model.getStatus().equalsIgnoreCase("0"))
                        {
                            new AlertDialog.Builder(OrderStatusActivity.this)
                                    .setMessage("Are you sure you want to delete?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // start code from here

                                            deleteOrder(adapter.getRef(position).getKey());

                                            // end code here


                                        }
                                    })
                                    .setNegativeButton("No", null)
                                    .show();


                        }
                        else {
                            Toast.makeText(OrderStatusActivity.this, "You can't cancel this order now", Toast.LENGTH_SHORT).show();
                        }

                    }
                });


            }
        };

        recyclerView_orderStatus.setAdapter(adapter);
    }

    private void deleteOrder(final String key) {

        requests_db_ref.child(key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                StringBuilder detail=new StringBuilder("Order");
                detail.append(key);
                detail.append("has been deleted!!");

                Toast.makeText(OrderStatusActivity.this,detail, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(OrderStatusActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
/*
public class OrderStatusActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference ref_request;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        //int Firebase
        database = FirebaseDatabase.getInstance();
        ref_request = database.getReference("Requests");
        //Load Food
        recyclerView = (RecyclerView) findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // for notification
        if (getIntent() == null)
            loadOrders(Common.currentUser.getPhone());
        else
            loadOrders(getIntent().getStringExtra("userPhone"));

    }

    private void loadOrders(String phone) {
        Query getOrderByUser = ref_request.orderByChild("phone").equalTo(phone);
        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(getOrderByUser, Request.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.order_item, parent, false);
                return new OrderViewHolder(itemView);
            }

            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder viewHolder, int position, @NonNull Request model) {
                viewHolder.txtOrderID.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
*/
