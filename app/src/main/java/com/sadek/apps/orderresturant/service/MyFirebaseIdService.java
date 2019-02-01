package com.sadek.apps.orderresturant.service;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.sadek.apps.orderresturant.common.Common;
import com.sadek.apps.orderresturant.model.Token;

/**
 * Created by Mahmoud Sadek on 8/16/2018.
 */

public class MyFirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String tokenRefreshed = FirebaseInstanceId.getInstance().getToken();
        if (Common.currentUser != null)
            updateTokenToFirebase(tokenRefreshed);
    }

    private void updateTokenToFirebase(String tokenRefreshed) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref_tokens = db.getReference("Tokens");
        Token token = new Token(tokenRefreshed, false);
        ref_tokens.child(Common.currentUser.getPhone()).setValue(token);
    }
}
