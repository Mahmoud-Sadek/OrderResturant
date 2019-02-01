package com.sadek.apps.orderresturant.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import com.sadek.apps.orderresturant.model.Favorite;
import com.sadek.apps.orderresturant.model.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mahmoud Sadek on 6/17/2018.
 */

public class Database extends SQLiteAssetHelper {
    private static final String DB_NAME = "EatItDB.db";
    private static final int DB_VER = 1;

    public Database(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    public List<Order> getCarts(String userPhone) {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] sqlSelect = {"UserPhone", "ProductId", "ProductName", "Quantity", "Price", "Dicount", "Image"};
        String sqlTable = "OrderDetail";

        qb.setTables(sqlTable);
        Cursor c = qb.query(db, sqlSelect, "UserPhone=?", new String[]{userPhone}, null, null, null);

        final List<Order> result = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                result.add(new Order(c.getString(c.getColumnIndex("UserPhone")),
                        c.getString(c.getColumnIndex("ProductId")),
                        c.getString(c.getColumnIndex("ProductName")),
                        c.getString(c.getColumnIndex("Quantity")),
                        c.getString(c.getColumnIndex("Price")),
                        c.getString(c.getColumnIndex("Dicount")),
                        c.getString(c.getColumnIndex("Image")))
                );
            } while (c.moveToNext());
        }
        return result;
    }

    public boolean checkFoodExist(String foodId, String userPhone) {

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        String query = String.format("SELECT * FROM OrderDetail WHERE ProductId='%s'AND UserPhone='%s';", foodId, userPhone);
        cursor = db.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public void addToCart(Order order) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("INSERT OR REPLACE INTO OrderDetail(UserPhone,ProductId,ProductName,Quantity,Price,Dicount,Image) VALUES('%s','%s','%s','%s','%s','%s','%s');",
                order.getUserPhone(),
                order.getProductId(),
                order.getProductName(),
                order.getQuantity(),
                order.getPrice(),
                order.getDicount(),
                order.getImage());
        db.execSQL(query);

    }

    public void cleanCart(String userPhone) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("DELETE FROM OrderDetail WHERE UserPhone='%s'", userPhone);
        db.execSQL(query);

    }

    public int getCountCart(String userPhone) {
        int count = 0;
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT COUNT(*) FROM OrderDetail WHERE UserPhone='%s';", userPhone);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                count = cursor.getInt(0);
            } while (cursor.moveToNext());
        }
        return count;
    }

    public void updateCart(Order order) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("UPDATE OrderDetail SET Quantity='%s' WHERE UserPhone='%s' AND ProductId='%s';", order.getQuantity(), order.getUserPhone(), order.getProductId());
        db.execSQL(query);
    }

    public void increaseCart(String foodId, String userPhone) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("UPDATE OrderDetail SET Quantity=Quantity+1 WHERE UserPhone='%s' AND ProductId='%s';", userPhone, foodId);
        db.execSQL(query);
    }

    //Favorites
    public void addToFavorites(Favorite favorite) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("INSERT INTO Favorites(" +
                        "FoodId,UserPhone,FoodName,FoodPrice,FoodMenuId,FoodImage,FoodDiscount,FoodDescription)" +
                        " VALUES('%s','%s','%s','%s','%s','%s','%s','%s');",
                favorite.getFoodId(),
                favorite.getUserPhone(),
                favorite.getFoodName(),
                favorite.getFoodPrice(),
                favorite.getFoodMenuId(),
                favorite.getFoodImage(),
                favorite.getFoodDiscount(),
                favorite.getFoodDescription());
        db.execSQL(query);
    }

    public void removeFromFavorites(String foodId, String userPhone) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("DELETE FROM Favorites WHERE FoodId='%s' and UserPhone='%s';", foodId, userPhone);
        db.execSQL(query);
    }

    public boolean isFavorites(String foodId, String userPhone) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT * FROM Favorites WHERE FoodId='%s' and UserPhone='%s';", foodId, userPhone);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }


    public void removeFromCart(String productId, String userPhone) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("DELETE FROM OrderDetail WHERE ProductId='%s' and UserPhone='%s';", productId, userPhone);
        db.execSQL(query);
    }
    public List<Favorite> getAllFavorites(String userPhone) {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] sqlSelect = {"FoodId","UserPhone","FoodName","FoodPrice","FoodMenuId","FoodImage","FoodDiscount","FoodDescription"};
        String sqlTable = "Favorites";

        qb.setTables(sqlTable);
        Cursor c = qb.query(db, sqlSelect, "UserPhone=?", new String[]{userPhone}, null, null, null);

        final List<Favorite> result = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                result.add(new Favorite(c.getString(c.getColumnIndex("FoodId")),
                        c.getString(c.getColumnIndex("FoodName")),
                        c.getString(c.getColumnIndex("FoodImage")),
                        c.getString(c.getColumnIndex("FoodMenuId")),
                        c.getString(c.getColumnIndex("FoodDescription")),
                        c.getString(c.getColumnIndex("FoodPrice")),
                        c.getString(c.getColumnIndex("FoodDiscount")),
                        c.getString(c.getColumnIndex("UserPhone")))
                );
            } while (c.moveToNext());
        }
        return result;
    }

}
