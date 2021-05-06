package com.github.jitnegii.swc.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.github.jitnegii.swc.models.User;

public class AccessDatabase extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "appdatabase.db";
    private static final int DATABASE_VERSION = 1;

    public static final String USER_TABLE = "userTable";
    private static final String USER_NAME = "userName";
    private static final String USER_ID = "userId";
    private static final String EMAIL = "email";
    private static final String STATE = "state";
    private static final String TYPE = "type";
    private static final String CITY = "city";


    public static final String USER_REPORTS_TABLE = "userReportsTable";
    private static final String TIME = "time";
    private static final String TEXT = "text";
    private static final String IMAGE = "image";


    private final SQLiteDatabase db;
    Context context;

    public AccessDatabase(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        this.db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

       // SQLQuery.createTables(db, getDatabaseTables());

        db.execSQL("CREATE TABLE IF NOT EXISTS " + USER_TABLE + " ( " +
                USER_ID + " VARCHAR, " +
                USER_NAME + " VARCHAR, " +
                EMAIL + " VARCHAR, " +
                TYPE + " VARCHAR, " +
                STATE + " VARCHAR, " +
                CITY + " VARCHAR " +

                ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    @Override
    public void onOpen(SQLiteDatabase db) {
        onCreate(db);
    }



    public User getUserDetail(String table, String id) {
        Cursor cursor = null;
        User user = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + table + " WHERE " +
                    USER_ID + " like ?", new String[] { "%" + id + "%" }, null);

            if (cursor != null && cursor.moveToFirst()) {
                user = new User();
                user.setUsername(cursor.getString(cursor.getColumnIndex(USER_NAME)));
                user.setId(cursor.getString(cursor.getColumnIndex(USER_ID)));
                user.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                user.setEmail(cursor.getString(cursor.getColumnIndex(EMAIL)));
                user.setState(cursor.getString(cursor.getColumnIndex(STATE)));
                user.setCity(cursor.getString(cursor.getColumnIndex(CITY)));

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return user;
    }


    public void setUserDetails(User user, String table) {
        try {
            ContentValues values = new ContentValues();
            values.put(USER_ID, user.getId());
            values.put(USER_NAME, user.getUsername());
            values.put(EMAIL, user.getEmail());
            values.put(TYPE, user.getType());
            values.put(STATE, user.getState());
            values.put(CITY, user.getCity());

            db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
