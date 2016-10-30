package com.example.macuser.havi3;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLData;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by macuser on 2016/08/27.
 */
public class Material {
    private final String materialNumber;
    private final String materialName;
    private static final int month;
    public GetMaterial getMaterial;

    public Material(String number, String name) {
        this.materialNumber = number;
        this.materialName = name;
        getMaterial = GetMaterial.newInstance(number, name);
    }

    static {
        Calendar cl = Calendar.getInstance();
        month = cl.get(Calendar.MONTH) + 1;
    }

    //消耗品リストを作成(HashMap)
    public static List<List<String>> showMaterialList(Context context, String expendable, String place) {
        List<List<String>> allMaterialList = new ArrayList<>();

        final DBOpenHelper myHelper = new DBOpenHelper(context);
        try {
            myHelper.createEmptyDatabase();
        } catch (IOException e) {
            throw new Error ("Unable to Create Database");
        }

        SQLiteDatabase db = myHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(makeSQL(expendable, place), null);
        try {
            while (cursor.moveToNext()) {
                List<String> materialList = new ArrayList<>(2);

                String number = cursor.getString(cursor.getColumnIndex("number"));
                String name = cursor.getString(cursor.getColumnIndex("name"));

                materialList.add(number);
                materialList.add(name);
                allMaterialList.add(materialList);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        myHelper.close();
        return allMaterialList;
    }

    //条件(消耗品、場所)にあったsql文を作成
    private static String makeSQL(String expendable, String place) {
        String sql = null;
        if (expendable.equals("") || place.equals("")) {
            if (expendable.equals("") && place.equals("")) {
                //どちらもnullの場合
                sql = "SELECT \"number\", \"name\" FROM Name;";
            } else if (expendable.equals("") && !(place.equals(""))) {
                //消耗品がnullの場合
                sql = "SELECT \"number\", \"name\" FROM Name WHERE \"place1\" = \"" + place + "\" OR \"place2\" = \"" + place + "\";";
            } else if (!(expendable.equals("")) && place.equals("")) {
                //場所がnullの場合
                sql = "SELECT \"number\", \"name\" FROM Name WHERE \"expendable\" = \"" + expendable + "\";";
            }
        } else {
            //どちらもnullでない場合
            sql = "SELECT \"number\", \"name\" FROM Name WHERE \"expendable\" = \"" + expendable + "\" AND (\"place1\" = \"" + place + "\" OR \"place2\" = \"" + place + "\");";
        }

        return sql;
    }

    //資材の詳細情報を表示
    public List<String> showContent(Context context) {
        List<String> materialContent = new ArrayList<>();
        final DBOpenHelper myHelper = new DBOpenHelper(context);
        try {
            myHelper.createEmptyDatabase();
        } catch (IOException e) {
            throw new Error ("Unable to Create Database");
        }

        List<String> nameList = getMaterial.getName(context);

        for (String name : nameList) {
            materialContent.add(name);
        }

        List<Integer> inNumberList = getMaterial.getInNumber(context);

        for (int inNumber : inNumberList) {
            materialContent.add(String.valueOf(inNumber));
        }

        SQLiteDatabase dbMonth = myHelper.getReadableDatabase();

        String[] columnUsedNumber = {"this", "last", "theMonthBeforeLast"};
        String monthSQL = getMaterial.makeSelectSQL(columnUsedNumber, "UsedNumber");
        Cursor cursorMonth = dbMonth.rawQuery(monthSQL, null);

        try {
            while (cursorMonth.moveToNext()) {
                String thisMonth = String.valueOf(cursorMonth.getDouble(cursorMonth.getColumnIndex("this")));
                String lastMonth = String.valueOf(cursorMonth.getDouble(cursorMonth.getColumnIndex("last")));
                String theMonthBeforeLast = String.valueOf(cursorMonth.getDouble(cursorMonth.getColumnIndex("theMonthBeforeLast")));

                materialContent.add(thisMonth);
                materialContent.add(lastMonth);
                materialContent.add(theMonthBeforeLast);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            dbMonth.close();
        }

        SQLiteDatabase dbNumber = myHelper.getReadableDatabase();

        String[] columnNumber = {"thisMonth"};
        String numberSQL = getMaterial.makeSelectSQL(columnNumber, "Number");
        Cursor cursorNumber = dbNumber.rawQuery(numberSQL, null);

        try {
            while (cursorNumber.moveToNext()) {
                String thisMonth = String.valueOf(cursorNumber.getDouble(cursorNumber.getColumnIndex("thisMonth")));

                materialContent.add(thisMonth);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            dbNumber.close();
        }

        myHelper.close();

        /**
         * materialContent indexと内容
         * 0.商品番号
         * 1.商品名
         * 2.単価
         * 3.消耗品の種類
         * 4.場所1
         * 5.場所2
         * 6.内包量:ケース
         * 7.内包量:中包
         * 8.内包量:個数
         * 9.使用数:今月
         * 10.使用数:先月
         * 11.使用数:先々月
         * 12.在庫
         */
        return materialContent;
    }

    /**
     * 使用数を計算する
     *
     * @param context
     * @param judge   //true : 先月、先々月の使用数も登録する / false : 先月、先々月の使用数は登録しない
     */
    public void calculationUsedNumber(final Context context, boolean judge) {
        final DBOpenHelper myHelper = new DBOpenHelper(context);
        try {
            myHelper.createEmptyDatabase();
        } catch (IOException e) {
            throw new Error ("Unable to Create Database");
        }

        List<Integer> orderingList = getMaterial.getOrdering(context);
        int orderSum = 0;
        for (int order : orderingList) {
            orderSum += order;
        }

        List<Double> numberList = getMaterial.getNumber(context);
        double usedNumber = numberList.get(1) + orderSum - numberList.get(0);

        BigDecimal bdUsedNumber = new BigDecimal(usedNumber);
        BigDecimal bdUsedNumber1 = bdUsedNumber.setScale(2, BigDecimal.ROUND_HALF_UP);

        usedNumber = bdUsedNumber1.doubleValue();

        List<Double> usedNumberList = getMaterial.getUsedNumber(context);
        double average = (usedNumberList.get(0) + usedNumberList.get(1) + usedNumberList.get(2)) / 3;

        BigDecimal bdAverage = new BigDecimal(average);
        BigDecimal bdAverage1 = bdAverage.setScale(2, BigDecimal.ROUND_HALF_UP);

        average = bdAverage1.doubleValue();

        SQLiteDatabase dbUpdate = myHelper.getWritableDatabase();

        try {
            ContentValues cv = new ContentValues();
            if (judge) {
                List<String> column = new ArrayList<>(5);
                column.add("this");
                column.add("last");
                column.add("theMonthBeforeLast");
                column.add("average");
                column.add("month");

                String[] columnsParam = {String.valueOf(usedNumber), String.valueOf(usedNumberList.get(0)), String.valueOf(usedNumberList.get(1)), String.valueOf(average), String.valueOf(month)};

                for (int i = 0; i < 5; i++) {
                    cv.put(column.get(i), columnsParam[i]);
                }
            } else {
                List<String> column = new ArrayList<>(3);
                column.add("this");
                column.add("average");
                column.add("month");

                String[] columnsParam = {String.valueOf(usedNumber), String.valueOf(average), String.valueOf(month)};

                for (int i = 0; i < 3; i++) {
                    cv.put(column.get(i), columnsParam[i]);
                }
            }

            try {
                dbUpdate.beginTransaction();
                dbUpdate.update("UsedNumber", cv, "number = ?", new String[] {this.materialNumber});
                dbUpdate.setTransactionSuccessful();
            } finally {
                dbUpdate.endTransaction();
            }
        } finally {
            dbUpdate.close();
            myHelper.close();
        }




    }

    //発注数を計算
    private void calculationOrder(Context context) {
        final DBOpenHelper myHelper = new DBOpenHelper(context);
        try {
            myHelper.createEmptyDatabase();
        } catch (IOException e) {
            throw new Error ("Unable to Create Database");
        }

        List<Double> numberList = getMaterial.getNumber(context);
        List<Double> usedNumberList = getMaterial.getUsedNumber(context);
        double thisMonth = numberList.get(0);
        double average_aWeek = usedNumberList.get(3) / 4;

        int[] order = new int[4];

        for (int i = 0; i < 4; i++) {
            thisMonth -= average_aWeek;
            if (thisMonth <= 0.0) {
                order[i] = 1;
                thisMonth++;
            } else {
                order[i] = 0;
            }
        }

        SQLiteDatabase dbUpdate = myHelper.getWritableDatabase();

        try {
            List<String> columns = new ArrayList<>(5);
            columns.add("first");
            columns.add("second");
            columns.add("third");
            columns.add("forth");
            columns.add("month");

            String[] columnsParam = {String.valueOf(order[0]), String.valueOf(order[1]), String.valueOf(order[2]), String.valueOf(order[3]), String.valueOf(month)};

            ContentValues cv = new ContentValues();
            for (int i = 0; i < 5; i++) {
                cv.put(columns.get(i), columnsParam[i]);
            }

            try {
                dbUpdate.beginTransaction();
                dbUpdate.update("Ordering", cv, "number = ?", new String[] {this.materialNumber});
                dbUpdate.setTransactionSuccessful();
            } finally {
                dbUpdate.endTransaction();
            }
        } finally {
            dbUpdate.close();
            myHelper.close();
        }
    }

    //資材の今月の在庫を得る
    private double getNumber(Context context) {
        final DBOpenHelper myHelper = new DBOpenHelper(context);
        try {
            myHelper.createEmptyDatabase();
        } catch (IOException e) {
            throw new Error ("Unable to Create Database");
        }

        double thisMonth = 0.0;

        SQLiteDatabase db = myHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(getMaterial.makeSelectSQL(new String[] {"thisMonth"}, "Number"), null);
        try {
            while (cursor.moveToNext()) {
                thisMonth = cursor.getDouble(cursor.getColumnIndex("thisMonth"));
            }
        } finally {
            db.close();
        }

        return thisMonth;
    }

    private List<Integer> getDate(Context context) {
        final DBOpenHelper myHelper = new DBOpenHelper(context);
        try {
            myHelper.createEmptyDatabase();
        } catch (IOException e) {
            throw new Error ("Unable to Create Database");
        }

        String[] columns = {"date", "month"};

        SQLiteDatabase db = myHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(getMaterial.makeSelectSQL(columns, "Number"), null);

        List<Integer> list = new ArrayList<>(2);

        try {
            while (cursor.moveToNext()) {
                for (String column : columns) {
                    list.add(cursor.getInt(cursor.getColumnIndex(column)));
                }
            }
        } finally {
            db.close();
        }

        return list;
    }

    //資材の在庫の入力
    public void setNumber(Context context, int caseNumber, int bagNumber, double substanceNumber) {
        final DBOpenHelper myHelper = new DBOpenHelper(context);
        try {
            myHelper.createEmptyDatabase();
        } catch (IOException e) {
            throw new Error ("Unable to Create Database");
        }

        Calendar cl = Calendar.getInstance();
        int date = cl.get(Calendar.DATE);

        List<Integer> dateList = getDate(context);

        List<Integer> listInNumber = getMaterial.getInNumber(context);
        double thisMonthNumber = ((double)caseNumber + ((double)bagNumber + (substanceNumber / listInNumber.get(2))) / listInNumber.get(1)) / listInNumber.get(0);

        BigDecimal bd = new BigDecimal(thisMonthNumber);
        BigDecimal bdCut = bd.setScale(2, BigDecimal.ROUND_HALF_UP);

        thisMonthNumber = bdCut.doubleValue();

        double thisMonth = getNumber(context);

        if (date == dateList.get(0) && month == dateList.get(1)) {
            thisMonthNumber += thisMonth;

            SQLiteDatabase db = myHelper.getWritableDatabase();

            try {
                List<String> column = new ArrayList<>();
                column.add("thisMonth");
                column.add("month");
                column.add("date");

                String[] columnsParams = {String.valueOf(thisMonthNumber), String.valueOf(month), String.valueOf(date)};

                ContentValues cv = new ContentValues();
                for (int i = 0; i < 3; i++) {
                    cv.put(column.get(i), columnsParams[i]);
                }

                try {
                    db.beginTransaction();
                    db.update("Number", cv, "number = ?", new String[] {this.materialNumber});
                    db.setTransactionSuccessful();
                    Toast.makeText(context, "保存しました。", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    db.endTransaction();
                }
            } finally {
                db.close();
                myHelper.close();
            }

            calculationUsedNumber(context, false);
        } else {
            SQLiteDatabase db = myHelper.getWritableDatabase();

            try {
                List<String> column = new ArrayList<>();
                column.add("thisMonth");
                column.add("lastMonth");
                column.add("month");
                column.add("date");

                String[] columnsParams = {String.valueOf(thisMonthNumber), String.valueOf(thisMonth), String.valueOf(month), String.valueOf(date)};

                ContentValues cv = new ContentValues();
                for (int i = 0; i < 4; i++) {
                    cv.put(column.get(i), columnsParams[i]);
                }

                try {
                    db.beginTransaction();
                    db.update("Number", cv, "number = ?", new String[] {this.materialNumber});
                    db.setTransactionSuccessful();
                    Toast.makeText(context, "保存しました。", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    db.endTransaction();
                }
            } finally {
                db.close();
                myHelper.close();
            }

            calculationUsedNumber(context, true);
        }

        calculationOrder(context);
    }

    private static Map<String, Integer> getNumberAndForth(Context context, int orderNumber) {
        final DBOpenHelper myHelper = new DBOpenHelper(context);
        try {
            myHelper.createEmptyDatabase();
        } catch (IOException e) {
            throw new Error("Unable to Create Database");
        }

        SQLiteDatabase dbSelect = myHelper.getReadableDatabase();

        String selectSQL = null;

        Map<String, Integer> number = new HashMap<>();

        //forth=1のときにnumberの数が0だった場合、third,second...となるように実装する
        //発注用テーブルを月ごとに作るのはどうだろうか
        switch (orderNumber) {
            case 4:
                selectSQL = "SELECT number, forth FROM Ordering WHERE forth > 0 AND first = 0 AND second = 0 AND third = 0 AND month = " + String.valueOf(month) + ";";
                Cursor cursor1 = dbSelect.rawQuery(selectSQL, null);

                try {
                    while (cursor1.moveToNext()) {
                        number.put(cursor1.getString(cursor1.getColumnIndex("number")), cursor1.getInt(cursor1.getColumnIndex("forth")));
                    }
                } catch (SQLiteException e) {
                    e.printStackTrace();
                } finally {
                    dbSelect.close();
                }
                break;
            case 3:
                selectSQL = "SELECT number, third FROM Ordering WHERE third > 0 AND first = 0 AND second = 0 AND  forth = 0 AND month = " + String.valueOf(month) + ";";
                Cursor cursor2 = dbSelect.rawQuery(selectSQL, null);

                try {
                    while (cursor2.moveToNext()) {
                        number.put(cursor2.getString(cursor2.getColumnIndex("number")), cursor2.getInt(cursor2.getColumnIndex("third")));
                    }
                } catch (SQLiteException e) {
                    e.printStackTrace();
                } finally {
                    dbSelect.close();
                }
                break;
            case 2:
                selectSQL = "SELECT number, second FROM Ordering WHERE second > 0 AND first = 0 AND forth = 0 AND third = 0 AND month = " + String.valueOf(month) + ";";
                Cursor cursor3 = dbSelect.rawQuery(selectSQL, null);

                try {
                    while (cursor3.moveToNext()) {
                        number.put(cursor3.getString(cursor3.getColumnIndex("number")), cursor3.getInt(cursor3.getColumnIndex("second")));
                    }
                } catch (SQLiteException e) {
                    e.printStackTrace();
                } finally {
                    dbSelect.close();
                }
                break;
            case 1:
                selectSQL = "SELECT number, first FROM Ordering WHERE first > 0 AND forth = 0 AND second = 0 AND third = 0 AND month = " + String.valueOf(month) + ";";
                Cursor cursor4 = dbSelect.rawQuery(selectSQL, null);

                try {
                    while (cursor4.moveToNext()) {
                        number.put(cursor4.getString(cursor4.getColumnIndex("number")), cursor4.getInt(cursor4.getColumnIndex("first")));
                    }
                } catch (SQLiteException e) {
                    e.printStackTrace();
                } finally {
                    dbSelect.close();
                }

                break;
        }

        return number;


    }

    private static Map<Map<String, Double>, List<Double>> getMapAndList(Context context, Set<String> set) {
        final DBOpenHelper myHelper = new DBOpenHelper(context);
        try {
            myHelper.createEmptyDatabase();
        } catch (IOException e) {
            throw new Error("Unable to Create Database");
        }

        Map<Map<String, Double>, List<Double>> map = new HashMap<>();

        for (String s : set) {
            SQLiteDatabase dbUsedNumber = myHelper.getReadableDatabase();

            String usedNumberSQL = "SELECT \"average\" FROM \"UsedNumber\" WHERE \"number\" = \"" + s + "\";";
            Cursor cursorUsedNumber = dbUsedNumber.rawQuery(usedNumberSQL, null);

            try {
                while (cursorUsedNumber.moveToNext()) {
                    Map<String, Double> usedNumber = new HashMap<>();
                    List<Double> arrayUsedNumber = new ArrayList<>();

                    usedNumber.put(s, cursorUsedNumber.getDouble(cursorUsedNumber.getColumnIndex("average")));
                    arrayUsedNumber.add(cursorUsedNumber.getDouble(cursorUsedNumber.getColumnIndex("average")));

                    map.put(usedNumber, arrayUsedNumber);
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
            } finally {
                dbUsedNumber.close();
            }
        }

        return map;
    }

    //使用数が低いものの発注を来月にまわす
    public static void moveUsedNumber(Context context, Money money) {
        final DBOpenHelper myHelper = new DBOpenHelper(context);
        try {
            myHelper.createEmptyDatabase();
        } catch (IOException e) {
            throw new Error("Unable to Create Database");
        }

        Map<String, Integer> number = new HashMap<>();

        for (int i = 4; i > 0; i--) {
            if (number.size() == 0) {
                number = getNumberAndForth(context, i);
            } else {
                break;
            }
        }

        Map<Map<String, Double>, List<Double>> mapList = getMapAndList(context, number.keySet());

        Map<String, Double> usedNumber = new HashMap<>();
        List<Double> arrayUsedNumber = new ArrayList<>();

        for (Map<String, Double> map : mapList.keySet()) {
            usedNumber = map;
            arrayUsedNumber = mapList.get(map);
        }

            //使用数が一番小さい商品番号を検索
        String smallestNumber = smallestNumber(arrayUsedNumber, usedNumber);

        String[] columns = {"month", "first"};

        List<String> columnsParam = new ArrayList<>(2);
        columnsParam.add(String.valueOf(month + 1));
        columnsParam.add(String.valueOf(number.get(smallestNumber)));

        ContentValues cv = new ContentValues();
        for (int i = 0; i < 2; i++) {
            cv.put(columns[i], columnsParam.get(i));
        }

        SQLiteDatabase dbUpdate = myHelper.getWritableDatabase();

        try {
            if (smallestNumber != null) {
                try {
                    dbUpdate.beginTransaction();
                    dbUpdate.update("Ordering", cv, "number = ?", new String[] {smallestNumber});
                    dbUpdate.setTransactionSuccessful();
                } finally {
                    dbUpdate.endTransaction();
                }
            } else {
                throw new NullPointerException();
            }
        } finally {
            dbUpdate.close();
            myHelper.close();
        }

        money.allRecordRemainingMoney(context);
    }


    private static Double sortUsedNumber(List<Double> usedNumber) {
        int smallestNumber = 0;
        for (int j = 0; j < usedNumber.size(); j++) {
            if (usedNumber.get(j) < usedNumber.get(smallestNumber)) {
                smallestNumber = j;
            }
        }

        return usedNumber.get(smallestNumber);
    }

    private static String smallestNumber(List<Double> arrayUsedNumber, Map<String, Double> usedNumber) {
        double smallestNumber = sortUsedNumber(arrayUsedNumber);
        String result = null;
        for (String key : usedNumber.keySet()) {
            if (smallestNumber == usedNumber.get(key)) {
                result = key;
            }
        }
        return result;
    }

    //新しい商品の登録
    public static void recordMaterial(Context context, List<String> list) {
        final DBOpenHelper myHelper = new DBOpenHelper(context);
        try {
            myHelper.createEmptyDatabase();
        } catch (IOException e) {
            throw new Error("Unable to Create Database");
        }

        SQLiteDatabase db = myHelper.getWritableDatabase();
        String[] nameColumn = {"number", "name", "price", "expendable", "place1", "place2"};

        try {
            ContentValues nameCV = new ContentValues();
            for (int i = 0; i < 6; i++) {
                nameCV.put(nameColumn[i], list.get(i));
            }

            try {
                db.beginTransaction();
                db.insert("Name", null, nameCV);
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }

            ContentValues inNumberCV = new ContentValues();
            inNumberCV.put("number", list.get(0));

            String[] inNumberColumn = {"InCase", "InBag", "InSubstance"};

            for (int i = 6; i < 9; i++) {
                int j = i - 6;
                inNumberCV.put(inNumberColumn[j], list.get(i));
            }

            try {
                db.beginTransaction();
                db.insert("InNumber", null, inNumberCV);
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }

            ContentValues usedNumberCV = new ContentValues();
            usedNumberCV.put("number", list.get(0));
            usedNumberCV.put("average", "0.0");
            usedNumberCV.put("month", String.valueOf(month));

            String[] usedNumberColumns = {"this", "last", "theMonthBeforeLast"};
            for (int i = 9; i < 12; i++) {
                int j = i - 9;
                usedNumberCV.put(usedNumberColumns[j], list.get(i));
            }

            try {
                db.beginTransaction();
                db.insert("UsedNumber", null, usedNumberCV);
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }

            ContentValues orderCV = new ContentValues();
            orderCV.put("number", list.get(0));


            try {
                db.beginTransaction();
                db.insert("Ordering", null, orderCV);
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }

            ContentValues numberCV = new ContentValues();
            numberCV.put("number", list.get(0));
            numberCV.put("month", String.valueOf(month));
            numberCV.put("lastMonth", "0.0");
            numberCV.put("date", "0");
            numberCV.put("thisMonth", list.get(12));

            try {
                db.beginTransaction();
                db.insert("Number", null, numberCV);
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }
        } finally {
            db.close();
            myHelper.close();
        }
    }

    /**
     * materialContent indexと内容
     * 0.商品番号
     * 1.商品名
     * 2.単価
     * 3.消耗品の種類
     * 4.場所1
     * 5.場所2
     * 6.内包量:ケース
     * 7.内包量:中包
     * 8.内包量:個数
     * 9.使用数:今月
     * 10.使用数:先月
     * 11.使用数:先々月
     * 12.在庫
     */

    public void updateMaterialContain(Context context, List<String> materialContain) {
        final DBOpenHelper myHelper = new DBOpenHelper(context);
        try {
            myHelper.createEmptyDatabase();
        } catch (IOException e) {
            throw new Error("Unable to Create Database");
        }

        SQLiteDatabase db = myHelper.getWritableDatabase();



        try {
            List<String> nameList = new ArrayList<>(6);
            nameList.add("number");
            nameList.add("name");
            nameList.add("price");
            nameList.add("expendable");
            nameList.add("place1");
            nameList.add("place2");

            ContentValues cv = new ContentValues();

            for(int i = 0; i < 6; i++) {
                cv.put(nameList.get(i), materialContain.get(i));
            }

            try {
                db.beginTransaction();
                db.update("Name", cv, "number = ?", new String[] {this.materialNumber});
                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }

            List<String> InNumberList = new ArrayList<>(3);
            InNumberList.add("InCase");
            InNumberList.add("InBag");
            InNumberList.add("InSubstance");

            ContentValues cv2 = new ContentValues();

            for (int i = 6; i < 9; i++) {
                int j = i - 6;
                cv2.put(InNumberList.get(j), materialContain.get(i));
            }

            try {
                db.beginTransaction();
                db.update("InNumber", cv2, "number = ?", new String[] {this.materialNumber});
                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }
        } finally {
            db.close();
            myHelper.close();
        }



    }

    private String makeUpdateSQL(String[] columns, List<String> columnParam, String from) {
        StringBuffer sb = new StringBuffer("UPDATE ");
        sb.append("\"");
        sb.append(from);
        sb.append("\"");
        sb.append(" SET ");
        for (int i = 0; i < columns.length; i++) {
            sb.append("\"");
            sb.append(columnParam.get(i));
            sb.append("\"");
            sb.append(" = ");
            sb.append(columns[i]);
            if (i == columns.length - 1) {
                sb.append(" ");
            } else {
                sb.append(", ");
            }
        }
        sb.append("WHERE \"number\" = ");
        sb.append(this.materialNumber);
        sb.append(";");

        return sb.toString();
    }


    //発注した資材の合計金額と個数のList
    public static List<List<String>> getOrderList(Context context) {
        Map<String, List<Integer>> orderList = Money.getOrderList(context);
        List<List<String>> allList = new ArrayList<>();

        for (String name : orderList.keySet()) {
            List<String> list = new ArrayList<>(3);

            list.add(name);
            list.add(orderSum(orderList.get(name)).get(0));
            list.add(orderSum(orderList.get(name)).get(1));

            allList.add(list);
        }
        return allList;
    }

    private static List<String> orderSum(List<Integer> orderList) {
        List<Integer> list = orderList;
        List<String> preserveList = new ArrayList<>(2);
        int price = list.get(0);
        int orderNumber = orderList.get(1) + orderList.get(2) + orderList.get(3) + orderList.get(4);
        int remainMoney = price * orderNumber;

        preserveList.add(String.valueOf(orderNumber));
        preserveList.add(String.valueOf(remainMoney));

        return preserveList;

    }

    //発注数と資材名、資材番号をえる
    public static List<List<String>> getOrder(Context context) {
        List<List<String>> allList = new ArrayList<>();

        List<List<String>> weekOfMonth = getWeekOfMonthList();
        Map<String, List<String>> expendableList = getExpendableList();

        String[] weekOfMonthString = {"first", "second", "third", "forth"};

        String[] expendableString = {"A", "B"};

        for (int i = 0; i < 4; i++) {
            allList.add(weekOfMonth.get(i));

            for (String expendable : expendableString) {
                allList.add(expendableList.get(expendable));
                for (List<String> list : getSubList(context, weekOfMonthString[i], expendable)) {
                    allList.add(list);
                }
            }


        }

        return allList;
    }

    private static Map<String , List<String>> getExpendableList() {
        Map<String, List<String>> allMap = new HashMap<>(2);

        String[] expendableString = {"A", "B"};

        for (String expendable : expendableString) {
            List<String> list = new ArrayList<>(1);
            list.add(expendable);
            allMap.put(expendable, list);
        }

        return allMap;
    }

    private static List<List<String>> getWeekOfMonthList() {
        List<List<String>> allList = new ArrayList<>(4);

        for (int i = 0; i < 4; i++) {
            List<String> list = new ArrayList<>(1);
            list.add(String.valueOf(i + 1));
            allList.add(list);
        }
        return allList;
    }

    private static List<List<String>> getSubList(Context context, String monthOfWeek, String expendable) {
        final DBOpenHelper myHelper = new DBOpenHelper(context);
        try {
            myHelper.createEmptyDatabase();
        } catch (IOException e) {
            throw new Error("Unable to Create Database");
        }

        List<List<String>> allList = new ArrayList<>();

        String name = null;
        String number = null;
        String order = null;

        SQLiteDatabase db = myHelper.getReadableDatabase();
        String sql = makeOrderingSQL(monthOfWeek, expendable);
        Cursor cursor = db.rawQuery(sql, null);
        try {
            while(cursor.moveToNext()) {
                List<String> list = new ArrayList<>(3);

                list.add(cursor.getString(cursor.getColumnIndex("name")));
                list.add(cursor.getString(cursor.getColumnIndex("number")));
                list.add(String.valueOf(cursor.getString(cursor.getColumnIndex(monthOfWeek))));

                allList.add(list);
            }
        } finally {
            db.close();
        }

        return allList;
    }

    private static String makeOrderingSQL(String monthOfWeek, String expendable) {
        StringBuilder sb = new StringBuilder("SELECT ");
        sb.append("Ordering.number, Ordering.");
        sb.append(monthOfWeek);
        sb.append(", Name.name ");
        sb.append("FROM Ordering ");
        sb.append("INNER JOIN Name ON Ordering.number = Name.number ");
        sb.append("WHERE Ordering.");
        sb.append(monthOfWeek);
        sb.append(" > 0 AND Name.expendable = ");
        sb.append("\"");
        sb.append(expendable);
        sb.append("\" AND Ordering.month = ");
        sb.append(String.valueOf(month));
        sb.append(";");

        return sb.toString();
    }

}
