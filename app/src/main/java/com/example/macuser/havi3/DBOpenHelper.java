package com.example.macuser.havi3;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by macuser on 2016/07/22.
 */
public class DBOpenHelper extends SQLiteOpenHelper {
    private static final String DB_FILE_NAME = "HAVI.db";
    private static final String DB_NAME = "HAVI.db.db.db.db.db.db.db.db.db.db.db.db.db.db.db.db.db.db.db.db.db.db";
    private static final int DB_VERSION = 1;

    private SQLiteDatabase mDatabase;
    private Context context;
    private File dbPath;

    public DBOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
        this.dbPath = this.context.getDatabasePath(DB_NAME);
    }

    public void createEmptyDatabase() throws IOException {
        boolean dbExist = checkDataBaseExists();

        if (dbExist) {
            // すでにデータベースは作成されている。
        } else {
            // このメソッドを呼ぶことで、空のデータベスパスがアプリのデフォルトシステムパスに作られる。

            getReadableDatabase();

            try {
                // assetsに格納したデータベースをコピー
                copyDataBaseFromAsset();

                String dbAbsolutePath = this.dbPath.getAbsolutePath();
                SQLiteDatabase checkDB = null;
                try {
                    checkDB = SQLiteDatabase.openDatabase(dbAbsolutePath, null, SQLiteDatabase.OPEN_READWRITE);
                } catch (SQLiteException e) {

                }

                if (checkDB != null) {
                    checkDB.setVersion(DB_VERSION);
                    checkDB.close();
                }
            } catch (IOException e) {
                throw new Error("Error Copying Database");
            }
        }
    }

    private boolean checkDataBaseExists() {
        String dbAbsolutePath = this.dbPath.getAbsolutePath();

        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(dbAbsolutePath, null, SQLiteDatabase.OPEN_READWRITE);
        } catch (SQLiteException e) {
            // まだデータベースは存在していない。
        }

        if (checkDB == null) {
            // まだデータベースは存在していない
            return false;
        }

        int oldVersion = checkDB.getVersion();
        int newVersion = DB_VERSION;

        if (oldVersion == newVersion) {
            // データベースは存在していて、最新
            checkDB.close();
            return true;
        }

        File f = new File(dbAbsolutePath);
        f.delete();
        return false;
    }

    private void copyDataBaseFromAsset() throws IOException {
        InputStream input = this.context.getAssets().open(DB_FILE_NAME);
        OutputStream output = new FileOutputStream(this.dbPath);

        byte[] buffer = new byte[1024];
        int size;
        while ((size = input.read(buffer)) > 0 ) {
            output.write(buffer, 0, size);
        }

        // close the stream
        output.flush();
        output.close();
        input.close();
    }

    public SQLiteDatabase openDataBase() throws SQLiteException {
        return getWritableDatabase();
    }



    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    @Override
    public synchronized void close() {
        if (mDatabase != null) {
            mDatabase.close();
        }
        super.close();
    }
}
