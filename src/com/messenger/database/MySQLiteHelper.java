package com.messenger.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

  public static final String TABLE_MESSAGES = "messages_database";
  public static final String COLUMN_ID = "id";
  public static final String COLUMN_FROM = "message_from";
  public static final String COLUMN_TO = "message_to";
  public static final String COLUMN_TIME = "time";
  public static final String COLUMN_MESSAGE = "message";

  private static final String DATABASE_NAME = "messages.db";
  private static final int DATABASE_VERSION = 1;

  // Database creation sql statement
//  private static final String db="CREATE TABLE 'login' (name text);";
  
  private static final String DATABASE_CREATE = "create table "
      + TABLE_MESSAGES + "(" + COLUMN_ID
      + " integer primary key autoincrement, " + COLUMN_FROM
      + " text," + COLUMN_TO
      + " text," + COLUMN_TIME
      + " text," + COLUMN_MESSAGE
      + " text);";

  public MySQLiteHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase database) {
    database.execSQL(DATABASE_CREATE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.w(MySQLiteHelper.class.getName(),
        "Upgrading database from version " + oldVersion + " to "
            + newVersion + ", which will destroy all old data");
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
    onCreate(db);
  }
} 