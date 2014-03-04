package com.messenger.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MessageDataSource {
	  private SQLiteDatabase database;
	  private MySQLiteHelper dbHelper;
	  private String[] allColumns = { 	MySQLiteHelper.COLUMN_ID,
			  							MySQLiteHelper.COLUMN_FROM,
			  							MySQLiteHelper.COLUMN_TO,
			  							MySQLiteHelper.COLUMN_TIME,
			  							MySQLiteHelper.COLUMN_MESSAGE};

	  public MessageDataSource(Context context) {
	    dbHelper = new MySQLiteHelper(context);
	  }

	  public void open() throws SQLException {
		  try{
			  database = dbHelper.getWritableDatabase();
		  }
		  catch(Exception e){
			  e.printStackTrace();
		  }
	  }

	  public void close() {
	    dbHelper.close();
	  }
	  
	  public long insertRows(ContentValues values, String table)
		{	
			long val=database.insert(table, null, values);
			return val;
		}

	  public Message createMessage(String message) {
	    ContentValues values = new ContentValues();
	    values.put(MySQLiteHelper.COLUMN_MESSAGE, message);
	    long insertId = database.insert(MySQLiteHelper.TABLE_MESSAGES, null,
	        values);
	    Cursor cursor = database.query(MySQLiteHelper.TABLE_MESSAGES,
	        allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
	        null, null, null);
	    cursor.moveToFirst();
	    Message newMessage = cursorToMessage(cursor);
	    cursor.close();
	    return newMessage;
	  }

	  public void deleteMessage(Message message) {
	    long id = message.getId();
	    System.out.println("Message deleted with id: " + id);
	    database.delete(MySQLiteHelper.TABLE_MESSAGES, MySQLiteHelper.COLUMN_ID
	        + " = " + id, null);
	  }
	  
	  public void deleteMessageItem(String message,String from,String to) {
		  String where = "message_from = ?"
				  + " AND message_to = ?"
				  + " AND message = ?";
		  String[] whereArgs = {from,to,message};

		    database.delete(MySQLiteHelper.TABLE_MESSAGES, where, whereArgs);
		    
		    Log.i("delete message query", "Executing the delete query: "+message);
		  }
	  
	  public Cursor getAllValues(String table)
		{
			Cursor myResult;
			myResult=database.query(table, null, null, null, null, null, null, null);
			
			return myResult;
		}

	  public List<Message> getAllMessages() {
	    List<Message> messages = new ArrayList<Message>();

	    Cursor cursor = database.query(MySQLiteHelper.TABLE_MESSAGES,
	        allColumns, null, null, null, null, null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      Message message = cursorToMessage(cursor);
	      messages.add(message);
	      cursor.moveToNext();
	    }
	    // Make sure to close the cursor
	    cursor.close();
	    return messages;
	  }

	  private Message cursorToMessage(Cursor cursor) {
	    Message message = new Message();
	    message.setId(cursor.getLong(0));
	    message.setMessage(cursor.getString(1));
	    return message;
	  }
	} 