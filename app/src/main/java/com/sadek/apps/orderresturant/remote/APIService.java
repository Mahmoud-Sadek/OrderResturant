package com.sadek.apps.orderresturant.remote;

import com.sadek.apps.orderresturant.model.DataMessage;
import com.sadek.apps.orderresturant.model.MyResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Mahmoud Sadek on 8/16/2018.
 */

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA8CniYig:APA91bGXD_yqpciN8mbrfHdwdRQetL6OneWo_TnNEQx_ddpewm3AD8VNruszz7UYgiNi7CIyIWNO7LgCGcUZ3GOEeJi4W8o1zYH1_QTGLxJ1IMI4dENc3LTgPUX_Pb-KJfHsCGSyrGMm"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body DataMessage body);
}
