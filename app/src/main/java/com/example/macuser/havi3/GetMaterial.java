package com.example.macuser.havi3;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.CornerPathEffect;
import android.os.CountDownTimer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by macuser on 2016/10/06.
 */

public class GetMaterial {
    private String materialNumber;
    private String materialName;
    private int month;

    private GetMaterial(String number, String name) {
        materialNumber = number;
        materialName = name;
    }

    public static GetMaterial newInstance(String number, String name) {
        return new GetMaterial(number, name);
    }

    {
        Calendar calendar = Calendar.getInstance();
        month = calendar.get(Calendar.MONTH);
    }

    /**
     * 資材の内包量を得る
     *
     * @param context
     * @return List
     *
     * 1.InCase
     * 2.InBag
     * 3.InSubstance
     *
     *
     */
    public List<Integer> getInNumber(Context context) {
        final DBOpenHelper myHelper = new DBOpenHelper(context);
        try {
            myHelper.createEmptyDatabase();
        } catch (IOException e) {
            throw new Error ("Unable to Create Database");
        }

        List<Integer> list = new ArrayList<>(3);

        SQLiteDatabase db = myHelper.getReadableDatabase();
        String[] columns = {"InCase", "InBag", "InSubstance"};
        Cursor cursor = db.rawQuery(makeSelectSQL(columns, "InNumber"), null);

        try {
            while (cursor.moveToNext()) {
                for (String column : columns)
                    list.add(cursor.getInt(cursor.getColumnIndex(column)));
            }
        } finally {
            db.close();
        }

        return list;
    }

    /**
     *
     * 資材の基本情報を得る
     *
     * @param context
     * @return List
     *
     * 1.number
     * 2.name
     * 3.price
     * 4.expendable
     * 5.place1
     * 6.place2
     *
     */
    public List<String> getName(Context context) {
        final DBOpenHelper myHelper = new DBOpenHelper(context);
        try {
            myHelper.createEmptyDatabase();
        } catch (IOException e) {
            throw new Error ("Unable to Create Database");
        }

        List<String> list = new ArrayList<>(6);
        list.add(materialNumber);
        list.add(materialName);

        SQLiteDatabase db = myHelper.getReadableDatabase();
        String[] columns = {"price", "expendable", "place1", "place2"};
        Cursor cursor = db.rawQuery(makeSelectSQL(columns, "Name"), null);

        try {
            while (cursor.moveToNext()) {
                list.add(String.valueOf(cursor.getInt(cursor.getColumnIndex("price"))));
                for (int i = 1; i < 4; i++) {
                    list.add(cursor.getString(cursor.getColumnIndex(columns[i])));
                }
            }
        } finally {
            db.close();
        }

        return list;
    }

    public List<Integer> getOrdering(Context context) {
        final DBOpenHelper myHelper = new DBOpenHelper(context);
        try {
            myHelper.createEmptyDatabase();
        } catch (IOException e) {
            throw new Error ("Unable to Create Database");
        }

        List<Integer> list = new ArrayList<>(4);

        SQLiteDatabase db = myHelper.getReadableDatabase();

        String[] columns = {"first", "second", "third", "forth"};
        Cursor cursor = db.rawQuery(makeSelectSQL(columns, "Ordering"), null);

        try {
            while(cursor.moveToNext()) {
                for (String column : columns) {
                    list.add(cursor.getInt(cursor.getColumnIndex(column)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        return list;
    }

    public List<Double> getNumber(Context context) {
        final DBOpenHelper myHelper = new DBOpenHelper(context);
        try {
            myHelper.createEmptyDatabase();
        } catch (IOException e) {
            throw new Error ("Unable to Create Database");
        }

        List<Double> list = new ArrayList<>(2);
        String[] columns = {"thisMonth", "lastMonth"};

        SQLiteDatabase db = myHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(makeSelectSQL(columns, "Number"), null);

        try {
            while (cursor.moveToNext()) {
                for (String column : columns) {
                    list.add(cursor.getDouble(cursor.getColumnIndex(column)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        return list;
    }

    public List<Double> getUsedNumber(Context context) {
        final DBOpenHelper myHelper = new DBOpenHelper(context);
        try {
            myHelper.createEmptyDatabase();
        } catch (IOException e) {
            throw new Error ("Unable to Create Database");
        }

        List<Double> list = new ArrayList<>(4);
        String[] columns = {"this", "last", "theMonthBeforeLast", "average"};

        SQLiteDatabase db = myHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(makeSelectSQL(columns, "UsedNumber"), null);

        try {
            while(cursor.moveToNext()) {
                for (String column : columns) {
                    list.add(cursor.getDouble(cursor.getColumnIndex(column)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        return list;
    }

    public String makeSelectSQL(String[] columns, String from) {
        StringBuilder sb = new StringBuilder("SELECT ");
        for (int i = 0; i < columns.length; i++) {
            if (i == columns.length - 1) {
                sb.append("\"");
                sb.append(columns[i]);
                sb.append("\"");
                sb.append(" ");
            } else {
                sb.append("\"");
                sb.append(columns[i]);
                sb.append("\"");
                sb.append(", ");
            }
        }

        sb.append("FROM ");
        sb.append("\"");
        sb.append(from);
        sb.append("\"");
        sb.append(" WHERE number = ");
        sb.append("\"");
        sb.append(materialNumber);
        sb.append("\"");
        sb.append(";");

        return sb.toString();
    }

    public static List<String> getAllNumber(Context context) {
        final DBOpenHelper myHelper = new DBOpenHelper(context);
        try {
            myHelper.createEmptyDatabase();
        } catch (IOException e) {
            throw new Error ("Unable to Create Database");
        }

        List<String> list = new ArrayList<>();

        SQLiteDatabase db = myHelper.getReadableDatabase();
        String sql = "SELECT \"number\" FROM \"Name\";";
        Cursor cursor = db.rawQuery(sql, null);

        try {
            while (cursor.moveToNext()) {
                list.add(cursor.getString(cursor.getColumnIndex("number")));
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        return list;
    }


}
