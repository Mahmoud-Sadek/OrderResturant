package com.sadek.apps.orderresturant;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.sadek.apps.orderresturant.Interface.RecyclerItemTouchHelperListener;
import com.sadek.apps.orderresturant.common.Common;
import com.sadek.apps.orderresturant.database.Database;
import com.sadek.apps.orderresturant.helper.RecyclerItemTouchHelper;
import com.sadek.apps.orderresturant.model.DataMessage;
import com.sadek.apps.orderresturant.model.MyResponse;
import com.sadek.apps.orderresturant.model.Order;
import com.sadek.apps.orderresturant.model.Request;
import com.sadek.apps.orderresturant.model.Token;
import com.sadek.apps.orderresturant.remote.APIService;
import com.sadek.apps.orderresturant.remote.IGoogleService;
import com.sadek.apps.orderresturant.viewholder.CartAdapter;
import com.sadek.apps.orderresturant.viewholder.CartViewHolder;


import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CartActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, RecyclerItemTouchHelperListener {


    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference ref_request;

    public TextView txtTotalPrice;
    Button btnPlace;

    PlaceAutocompleteFragment editAddres;

    List<Order> carts = new ArrayList<>();

    CartAdapter adapter;

    //// current Location of device
    private LocationRequest mLocationRequest;
    // public LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    public static final int UPDATE_INTERVAL = 5000;
    public static final int FASTEST_INTERVAL = 3000;
    public static final int DISPLACEMENT = 10;


    public static final int REQUEST_LOCATION_CODE = 9999;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9997;

    Place shippingAddress;

    // Google Map Api Client
    IGoogleService mGoogleMapService;
    APIService mService;

    String adress = "";
    String paymentMethod = "";


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

        setContentView(R.layout.activity_cart);

        // it uses Scalar Convertor
        mGoogleMapService = Common.getGoogleMapApi();

        // init service
        mService = Common.getFCMService();


        // Runtime permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Check permission
            ActivityCompat.requestPermissions(this, new String[]
                            {android.Manifest.permission.ACCESS_FINE_LOCATION}
                    , REQUEST_LOCATION_CODE);
        } else {

            if (checkPlayServices()) {
                buildingGoogleApiClient();
                createLocationRequest();
            }
        }

        //Firebase
        database = FirebaseDatabase.getInstance();
        ref_request = database.getReference("Requests");

        //Init
        recyclerView = findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        txtTotalPrice = findViewById(R.id.total);
        btnPlace = findViewById(R.id.btnPlaceOrder);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (carts.size() > 0)
                    showAllertDialog();
                else
                    Toast.makeText(CartActivity.this, "Your cart is empty!!", Toast.LENGTH_SHORT).show();
            }
        });

        loadListFood();
    }

    private boolean checkPlayServices() {

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Toast.makeText(this, "This device  is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private synchronized void buildingGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();


    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();

        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);


    }

    private void showAllertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(CartActivity.this);
        alertDialog.setTitle("One more step!");
        alertDialog.setMessage("Enter your address: ");

        LayoutInflater inflater = this.getLayoutInflater();
        final View order_address_comment = inflater.inflate(R.layout.order_address_comment, null);

        editAddres = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        editAddres.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);

        ((EditText) editAddres.getView().findViewById(R.id.place_autocomplete_search_input)).setHint("enter your address");
        ((EditText) editAddres.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(25);


        // get address from place  auto complete

        editAddres.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {


                shippingAddress = place;
                Log.e("position", shippingAddress.getLatLng().latitude + "  " + shippingAddress.getLatLng().longitude);
                Toast.makeText(CartActivity.this, shippingAddress.getLatLng().latitude + " long" + shippingAddress.getLatLng().longitude, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Status status) {

            }
        });
        final MaterialEditText edtComment = order_address_comment.findViewById(R.id.edtComment);

        //Radio buttons

        final RadioButton rdShipToAdress = order_address_comment.findViewById(R.id.rdShipToAdress);
        final RadioButton rdHomeAdress = order_address_comment.findViewById(R.id.rdHomeToAdress);
        final RadioButton rdCashOnDelivery = order_address_comment.findViewById(R.id.rdCOD);
        final RadioButton rdBalance = order_address_comment.findViewById(R.id.rdPayPal);

        final Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        rdShipToAdress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                final SpotsDialog dialog = new SpotsDialog(CartActivity.this);
                if (isChecked) {
                    dialog.show();
                    String strAdd = "";

                    try {
                        List<Address> addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                        if (addresses != null) {
                            Address returnedAddress = addresses.get(0);
                            StringBuilder strReturnedAddress = new StringBuilder("");

                            for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                                strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                            }
                            strAdd = strReturnedAddress.toString();
                            Log.w("Current loction", strReturnedAddress.toString());
                        } else {
                            Log.w("Current loction", "No Address returned!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.w("Current loction", "Canont get Address!");
                    }
                    adress = strAdd;
                    ((EditText) editAddres.getView().findViewById(R.id.place_autocomplete_search_input)).setText(adress);
                    dialog.dismiss();
                }
            }
        });

        /// home address

        rdHomeAdress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                if (isChecked) {
                    if (Common.currentUser.getHomeAddress() != null) {
                        adress = Common.currentUser.getHomeAddress().get("address").toString();
                        ((EditText) editAddres.getView().findViewById(R.id.place_autocomplete_search_input)).setText(adress);


                    } else {

                        Toast.makeText(CartActivity.this, "Please Update your Home Address", Toast.LENGTH_SHORT).show();
                    }

                }

            }
        });

        rdCashOnDelivery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    paymentMethod = "Cash On Delivery";
                }
            }
        });

        rdBalance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    paymentMethod = "By Balance";
                }
            }
        });
        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final android.app.AlertDialog waitingDialog = new SpotsDialog(CartActivity.this);
                waitingDialog.show();

                if (!rdShipToAdress.isChecked() && !rdHomeAdress.isChecked()) {

                    // if radio button is not activated
                    if (shippingAddress != null) {
                        adress = shippingAddress.getAddress().toString();
                    } else {
                        Toast.makeText(CartActivity.this, "Please enter address or select any option ", Toast.LENGTH_SHORT).show();
                        waitingDialog.cancel();
                        // fix crash fragment
                        getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment)).commit();

                        return;
                    }

                }

                if (TextUtils.isEmpty(adress)) {
                    Toast.makeText(CartActivity.this, "Please enter address or select any option ", Toast.LENGTH_SHORT).show();
                    waitingDialog.cancel();
                    // fix crash fragment
                    getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment)).commit();

                    return;
                }
                //Create new Request
                Request request = new Request();
                request.setName(Common.currentUser.getName());
                request.setPhone(Common.currentUser.getPhone());
                request.setAddress(adress);
                request.setTotal(txtTotalPrice.getText().toString());
                request.setStatus("0");
                request.setComment(edtComment.getText().toString());
                request.setFoods(carts);
                if (rdShipToAdress.isChecked()) {
                    request.setLatlng(String.format("%s,%s", mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                } else if (rdHomeAdress.isChecked()) {
                    request.setLatlng(String.format("%s,%s", Common.currentUser.getHomeAddress().get("lat"), Common.currentUser.getHomeAddress().get("lng")));
                } else {
                    request.setLatlng(String.format("%s,%s", shippingAddress.getLatLng().latitude, shippingAddress.getLatLng().longitude));

                }
                if (!rdCashOnDelivery.isChecked() && !rdBalance.isChecked()) {
                    waitingDialog.cancel();
                    Toast.makeText(CartActivity.this, "Please select ony one payment method", Toast.LENGTH_SHORT).show();
                    // fix crash fragment
                    getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment)).commit();

                    return;
                } else {
                    request.setPaymentMethod(paymentMethod);
                }
                if (paymentMethod.equalsIgnoreCase("Cash On Delivery"))
                    request.setPaymentStatus("Unpaid");
                else
                    request.setPaymentStatus("paid");

                //Submit to Firebase
                //we will using System.CurrentMill to key
                String order_number = String.valueOf(System.currentTimeMillis());
                ref_request.child(order_number).setValue(request);
                //Delete cart
                new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());
                waitingDialog.cancel();
                sendNotificationOrder(order_number);

            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //  we have to remove  fragment so it can prevent from crash,why  because in xml i use placeholder fragment
                getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment)).commit();

                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void sendNotificationOrder(final String order_number) {
        final android.app.AlertDialog waitingDialog = new SpotsDialog(CartActivity.this);
        waitingDialog.show();

        DatabaseReference ref_tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        final Query data = ref_tokens.orderByChild("serverToken").equalTo(true);
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Token severToken = postSnapshot.getValue(Token.class);

                    //Create raw payload to send
//                    Notification notification = new Notification("SADEK RESTAURANT", "You have new Order " + order_number);
//                    Sender content = new Sender(severToken.getToken(), notification);
                    Map<String, String> dataSender = new HashMap<>();
                    dataSender.put("title", "SADEK RESTAURANT");
                    dataSender.put("message", "You have new Order " + order_number);
                    DataMessage dataMessage = new DataMessage(severToken.getToken(), dataSender);

                    mService.sendNotification(dataMessage)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success == 1) {
                                            waitingDialog.cancel();
                                            Toast.makeText(CartActivity.this, "Thank You , Order Place", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            waitingDialog.cancel();
                                            Toast.makeText(CartActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    waitingDialog.cancel();
                                    Log.e("Error", "onFailure: " + t.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadListFood() {
        carts = new Database(this).getCarts(Common.currentUser.getPhone());
        adapter = new CartAdapter(carts, this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //Calculate Total Price
        int total = 0;
        for (Order order : carts)
            total += (Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuantity()));
        Locale locale = new Locale("en", "US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(fmt.format(total));

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int position) {
        carts.remove(position);
        new Database(this).cleanCart(Common.currentUser.getPhone());
        for (Order item : carts)
            new Database(this).addToCart(item);
        loadListFood();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_LOCATION_CODE: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        buildingGoogleApiClient();
                        createLocationRequest();
                    }

                }
            }

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            Log.e("Location", "Your Location" + mLastLocation.getLatitude() + ", " + mLastLocation.getLongitude());

        } else {
            Log.e("Location", "Coudn't get your location!!");
        }

    }


    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();

    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof CartViewHolder) {
            // get the removed item name to display it in snack bar
            String name = (carts).get(viewHolder.getAdapterPosition()).getProductName();

            // backup of removed item for undo purpose
            final Order deletedItem = carts.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            // remove the item from recycler view
            adapter.removeItem(viewHolder.getAdapterPosition());

            new Database(getBaseContext()).removeFromCart(deletedItem.getProductId(), Common.currentUser.getPhone());
            //Calculate Total Price
            int total = 0;
            for (Order item : carts)
                total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
            Locale locale = new Locale("en", "US");
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

            txtTotalPrice.setText(fmt.format(total));

            // showing snack bar with Undo option
            Snackbar snackbar = Snackbar
                    .make(recyclerView, name + " removed from cart!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // undo is selected, restore the deleted item
                    adapter.restoreItem(deletedItem, deletedIndex);
                    new Database(getBaseContext()).addToCart(deletedItem);
//Calculate Total Price
                    int total = 0;
                    for (Order item : carts)
                        total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
                    Locale locale = new Locale("en", "US");
                    NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

                    txtTotalPrice.setText(fmt.format(total));
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}
