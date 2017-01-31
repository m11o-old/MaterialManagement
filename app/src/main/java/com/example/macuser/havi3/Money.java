package com.example.macuser.havi3;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by macuser on 2016/08/30.
 */
public class Money {
    private final int money;
    private static final int month;

    public Money(Context context, boolean judge) {
        SharedPreferences budget = context.getSharedPreferences("Budget", Context.MODE_PRIVATE);
        //surplus = context.getSharedPreferences("Surplus", Context.MODE_PRIVATE);
        //use = context.getSharedPreferences("Use", Context.MODE_PRIVATE);

        money = budget.getInt(String.valueOf(month), 0);
        if (judge && money != 0) {
            allRecordRemainingMoney(context);
        }

    }

    public Money() {
        money = 0;
    }

    static {
        Calendar cl = Calendar.getInstance();
        month = cl.get(Calendar.MONTH) + 1;
    }

    //予算を登録
    public static void budgetRecord(Context context, int budgetRecord, int month) {
        SharedPreferences budget = context.getSharedPreferences("Budget", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = budget.edit();
        editor.putInt(String.valueOf(month), budgetRecord);
        int monthBeforeThree = month - 3;
        if (monthBeforeThree <= 0) {
            monthBeforeThree += 12;
        }
        editor.putInt(String.valueOf(monthBeforeThree), 0);
        editor.apply();
    }

    private int orderSumMoney(Context context) {
        Map<String, List<Integer>> orderList = getOrderList(context);
        int remainMoney = 0;
        for (String key : orderList.keySet()) {
            List<Integer> order = orderList.get(key);
            int price = order.get(0);
            int orderNumber = 0;
            for (int i = 1; i < 6; i++) {
                orderNumber += order.get(i);
            }
            remainMoney += price * orderNumber;
        }

        return remainMoney;
    }

    //今月の全ての発注品を発注した時の残金を計算、保存
    public void allRecordRemainingMoney(Context context) {
        int remainMoney = orderSumMoney(context);

        SharedPreferences surplus = context.getSharedPreferences("Surplus", Context.MODE_PRIVATE);
        SharedPreferences use = context.getSharedPreferences("Use", Context.MODE_PRIVATE);

        //発注した商品の合計金額が予算を超えた場合の処理がうまくいかない。
        if ((money - remainMoney) < 0) {
            Material.moveUsedNumber(context, this);
        }

        setSurplus(context, (money - remainMoney));

        setUse(context, remainMoney);
    }

    /**
     * 発注する品物の発注数と単価を得る
     *
     * @param context
     * @return Map<String, List<Integer>>
     *     String : 商品番号
     *     List : 0 : 単価
     *            1 : first
     *            2 : second
     *            3 : third
     *            4 : forth
     */
    public static Map<String, List<Integer>> getOrderList(Context context) {
        final DBOpenHelper myHelper = new DBOpenHelper(context);
        try {
            myHelper.createEmptyDatabase();
        } catch (IOException e) {
            throw new Error("Unable to Create Database");
        }

        SQLiteDatabase db = myHelper.getReadableDatabase();

        String sql = "SELECT Ordering.first, Ordering.second, Ordering.third, Ordering.forth, Ordering.other, Name.price, Name.name FROM Ordering INNER JOIN Name ON Ordering.number = Name.number WHERE Ordering.month = " + String.valueOf(month) + " AND (Ordering.first > 0 OR Ordering.second > 0 OR Ordering.third > 0 OR Ordering.forth > 0 OR Ordering.other > 0);";
        Cursor cursor = db.rawQuery(sql, null);

        Map<String, List<Integer>> orderList = new HashMap<>();

        try {
            while(cursor.moveToNext()) {
                List<Integer> order = new ArrayList<>(6);
                order.add(cursor.getInt(cursor.getColumnIndex("price")));
                order.add(cursor.getInt(cursor.getColumnIndex("first")));
                order.add(cursor.getInt(cursor.getColumnIndex("second")));
                order.add(cursor.getInt(cursor.getColumnIndex("third")));
                order.add(cursor.getInt(cursor.getColumnIndex("forth")));
                order.add(cursor.getInt(cursor.getColumnIndex("other")));

                orderList.put(cursor.getString(cursor.getColumnIndex("name")), order);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return orderList;
    }

    //今月の予算を得る
    public int getBudget() {
        return this.money;
    }

    //単体で発注した時の残金の計算と保存
    public void recordRemainingMoney(Context context, int price, int orderNumber) {
        int orderPrice = price * orderNumber;

        SharedPreferences surplus = context.getSharedPreferences("Surplus", Context.MODE_PRIVATE);
        SharedPreferences use = context.getSharedPreferences("Use", Context.MODE_PRIVATE);

        int useNumber = use.getInt(String.valueOf(month), 0);

        int calculation = useNumber + orderPrice;

        setSurplus(context, money - calculation);

        Log.d("Surplus", String.valueOf(surplus.getInt(String.valueOf(month), 0)));

        setUse(context, calculation);

        Log.d("Use", String.valueOf(use.getInt(String.valueOf(month), 0)));
    }

    public int getSurplus(Context context) {
        SharedPreferences surplus = context.getSharedPreferences("Surplus", Context.MODE_PRIVATE);
        int surplusInt = surplus.getInt(String.valueOf(month), 0);
        Log.d("surplusInt", String.valueOf(surplus.getInt(String.valueOf(month), 0)));
        return surplusInt;
    }

    public int getUse(Context context) {
        SharedPreferences use = context.getSharedPreferences("Use", Context.MODE_PRIVATE);
        int useInt = use.getInt(String.valueOf(month), 0);
        Log.d("useInt", String.valueOf(use.getInt(String.valueOf(month), 0)));
        return useInt;
    }

    private void setSurplus(Context context, int value) {
        SharedPreferences surplus = context.getSharedPreferences("Surplus", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = surplus.edit();
        editor.putInt(String.valueOf(month), value);
        editor.apply();
    }

    private void setUse(Context context, int value) {
        SharedPreferences use = context.getSharedPreferences("Use", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = use.edit();
        editor.putInt(String.valueOf(month), value);
        editor.apply();
    }


}
