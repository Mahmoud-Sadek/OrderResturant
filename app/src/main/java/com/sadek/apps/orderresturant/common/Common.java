package com.sadek.apps.orderresturant.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.DateFormat;

import com.sadek.apps.orderresturant.model.User;
import com.sadek.apps.orderresturant.remote.APIService;
import com.sadek.apps.orderresturant.remote.GoogleRetrofitClient;
import com.sadek.apps.orderresturant.remote.IGoogleService;
import com.sadek.apps.orderresturant.remote.RetrofitClient;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Mahmoud Sadek on 5/3/2018.
 */

public class Common {
    public static User currentUser;
    public static String currentKey;
    public static final String INTENT_FOOD_ID = "FoodId";
    public static final String TopicName = "News";
    private static final String BASE_URL = "https://fcm.googleapis.com/";
    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/";

    public static APIService getFCMService(){
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }
    public static IGoogleService getGoogleMapApi(){
        return GoogleRetrofitClient.getGoogleClient(GOOGLE_API_URL).create(IGoogleService.class);
    }
    public static Bitmap scaleBitmap (Bitmap bitmap, int newWidth, int newhieght){
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newhieght, Bitmap.Config.ARGB_8888);

        float scaleX = newWidth/(float) bitmap.getWidth();
        float scaleY = newhieght/(float) bitmap.getHeight();
        float pivotX =0,pivotY = 0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0,0, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }
    public static String convertCodeToStatus(String status) {
        if (status.equals("0"))
            return "Placed";
        else if (status.equals("1"))
            return "On my may";
        else if (status.equals("2"))
            return "Shipping";
        else
            return "Shipped";
    }

    public static final String DELETE ="Delete";
    public static final String USER_KEY ="User";
    public static final String PWD_KEY ="Password";

    public static boolean isConnectedToInternet (Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null){
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null){
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }
    public static String getDate(long time){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        StringBuilder date = new StringBuilder(DateFormat.format("dd-MM-yyyy HH:mm", calendar).toString());
        return date.toString();
    }
}
