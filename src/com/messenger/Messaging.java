package com.messenger;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.messenger.chat.*;
import com.messenger.interfaces.IAppManager;
import com.messenger.services.IMService;
import com.messenger.tools.FriendController;
import com.messenger.types.FriendInfo;
import com.messenger.database.*;

@SuppressWarnings("unused")
public class Messaging extends Activity {
	
	private static final int MESSAGE_CANNOT_BE_SENT = 0;
	private static final int DELETE_MESSAGE = 2;
	private static final String DATABASE="messages_database";
//	private EditText messageText;
//	private EditText messageHistoryText;
	private Button sendMessageButton;
	private Button getChatButton;
	private IAppManager imService;
	private FriendInfo friend = new FriendInfo();
	
	private DiscussArrayAdapter adapter;
	private ListView lv;
	private EditText editText1;
	
	private ServiceConnection mConnection = new ServiceConnection() {
      
		public void onServiceConnected(ComponentName className, IBinder service) {          
            imService = ((IMService.IMBinder)service).getService();          
        }
        public void onServiceDisconnected(ComponentName className) {          
        	imService = null;
            Toast.makeText(Messaging.this, R.string.local_service_stopped,
                    Toast.LENGTH_SHORT).show();
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);	   
		
		setContentView(R.layout.messaging_screen); //messaging_screen);
		
		//start of code to set messages stored in mysqlite
/*		ArrayList<Message> messageList = new ArrayList<Message>();
				
		MessageDataSource db=new MessageDataSource(Messaging.this);
		
		db.open();
		Cursor c=db.getAllValues(DATABASE);
		
		if(c!=null)
			c.moveToFirst();
		
		for(int i=0;i<c.getCount();i++)
		{
			Message mod=new Message();
			mod.setId(c.getInt(0));
			mod.setMessage_from(c.getString(1));
			mod.setMessage_to(c.getString(2));
			mod.setTime(c.getString(3));
			mod.setMessage(c.getString(4));
			messageList.add(mod);
			c.moveToNext();
		}
		
		c.close();
		db.close();
		
		for(int x=0;x<messageList.size();++x)
		{	
			if(messageList.get(x).getMessage_from().contentEquals(friend.userName))
				adapter.add(new MessageBubble(true, messageList.get(x).getMessage(), friend.userName, imService.getUsername(),messageList.get(x).getTime()));
			else if(messageList.get(x).getMessage_to().contentEquals(friend.userName))
				adapter.add(new MessageBubble(false, messageList.get(x).getMessage(), imService.getUsername(), friend.userName,messageList.get(x).getTime()));
		}
		*/
		

		//end to set messages stored in mysqlite
		
		lv = (ListView) findViewById(R.id.listView1);

		adapter = new DiscussArrayAdapter(getApplicationContext(), R.layout.listitem_discuss);

		lv.setAdapter(adapter);

		editText1 = (EditText) findViewById(R.id.editText1);
		editText1.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				final CharSequence message;
				final Handler handler = new Handler();
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
						message = editText1.getText();
						
						if (message.length()>0) 
						{		
							Thread thread = new Thread(){					
								public void run() {
									if (!imService.sendMessage(friend.userName, message.toString()))
									{
										handler.post(new Runnable(){	
											public void run() {
												showDialog(MESSAGE_CANNOT_BE_SENT);										
											}		
										});
									}
								}						
							};
							thread.start();					
						}
					
					String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
			//		adapter.add(new OneComment(false, editText1.getText().toString()));
					adapter.add(new MessageBubble(false, editText1.getText().toString(), imService.getUsername(), friend.userName,currentDateTimeString));
					editText1.setText("");
					
					String message_database=message.toString();
					MessageDataSource messagedatasource;
					messagedatasource = new MessageDataSource(Messaging.this);
				    messagedatasource.open();
				    
				    
					ContentValues c=new ContentValues();
					c.put("message_from", imService.getUsername());
					c.put("message_to", friend.userName);
					c.put("message", message_database);
					c.put("time", currentDateTimeString);
					
					messagedatasource.insertRows(c, DATABASE);
					messagedatasource.close();
					
					return true;
				}
				return false;
			}	
		});
		
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View view,final int position, long id) {
			    AlertDialog.Builder builder2=new AlertDialog.Builder(Messaging.this);
			    builder2.setMessage(R.string.delete_message);
			    builder2.setPositiveButton("OK",new DialogInterface.OnClickListener() {
			    	public void onClick(DialogInterface dialog, int which) {
			    		MessageBubble bubble=adapter.getItem(position);
			               
						MessageDataSource messagedatasource;
						messagedatasource = new MessageDataSource(Messaging.this);
					    messagedatasource.open();
					    
			    		messagedatasource.deleteMessageItem(bubble.message,bubble.message_from,bubble.message_to);
					    
					    messagedatasource.close();
			    	}
			    });
			    
			    builder2.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
			    	public void onClick(DialogInterface dialog, int which) {
			    		dialog.cancel();
			    }
			    });
			    
			    builder2.show();
			    
				return false;
			}
			
		});
		
/*		messageHistoryText = (EditText) findViewById(R.id.messageHistory);		
		messageText = (EditText) findViewById(R.id.message);
		messageText.requestFocus();			
*/		sendMessageButton = (Button) findViewById(R.id.sendMessageButton);
		getChatButton=(Button) findViewById(R.id.getchat);
		
		Bundle extras = this.getIntent().getExtras();
		
		friend.userName = extras.getString(FriendInfo.USERNAME);
		friend.ip = extras.getString(FriendInfo.IP);
		friend.port = extras.getString(FriendInfo.PORT);
		String msg = extras.getString(FriendInfo.MESSAGE);
		
		setTitle("Messaging with " + friend.userName);
	
		
	//	EditText friendUserName = (EditText) findViewById(R.id.friendUserName);
	//	friendUserName.setText(friend.userName);
		
		if (msg != null) 
		{
		//	this.appendToMessageHistory(friend.userName , msg);
		//	adapter.add(new OneComment(true, msg));
			String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
			adapter.add(new MessageBubble(true, msg, friend.userName, "",currentDateTimeString));
			editText1.setText("");
			((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel((friend.userName+msg).hashCode());
		}
		
		sendMessageButton.setOnClickListener(new OnClickListener(){
			CharSequence message;
			Handler handler = new Handler();
			public void onClick(View arg0) {
				message = editText1.getText();
				if (message.length()>0) 
				{
		//			adapter.add(new OneComment(false, editText1.getText().toString()));
					String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
					adapter.add(new MessageBubble(false, editText1.getText().toString(), imService.getUsername(), friend.userName,currentDateTimeString));
					editText1.setText("");
					
					//SQL code start
					String message_database=message.toString();
					MessageDataSource messagedatasource;
					messagedatasource = new MessageDataSource(Messaging.this);
				    messagedatasource.open();
				    
					ContentValues c=new ContentValues();
					c.put("message_from", imService.getUsername());
					c.put("message_to", friend.userName);
					c.put("message", message_database);
					c.put("time", currentDateTimeString);
					
					messagedatasource.insertRows(c, DATABASE);
					messagedatasource.close();
					//SQL code ends

					Thread thread = new Thread(){					
						public void run() {
							if (!imService.sendMessage(friend.userName, message.toString()))
							{
								handler.post(new Runnable(){	

									public void run() {
										showDialog(MESSAGE_CANNOT_BE_SENT);										
									}		
								});
							}
						}						
					};
					thread.start();
				}
			}});
	
		//new code start
		
		
		getChatButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ArrayList<Message> messageList = new ArrayList<Message>();
				MessageDataSource db=new MessageDataSource(Messaging.this);
				
				db.open();
				Cursor c=db.getAllValues(DATABASE);
				
				if(c!=null)
					c.moveToFirst();
				
				for(int i=0;i<c.getCount();i++)
				{
					Message mod=new Message();
					mod.setId(c.getInt(0));
					mod.setMessage_from(c.getString(1));
					mod.setMessage_to(c.getString(2));
					mod.setTime(c.getString(3));
					mod.setMessage(c.getString(4));
					messageList.add(mod);
					c.moveToNext();
				}
				
				c.close();
				db.close();
				
				for(int x=0;x<messageList.size();++x)
				{
			/*		if(messageList.get(x).getFrom().contentEquals(friend.userName))
						adapter.add(new OneComment(true, messageList.get(x).getMessage()));
					else if(messageList.get(x).getTo().contentEquals(friend.userName))
						adapter.add(new OneComment(false, messageList.get(x).getMessage()));
			*/	
					if(messageList.get(x).getMessage_from().contentEquals(friend.userName))
						adapter.add(new MessageBubble(true, messageList.get(x).getMessage(), friend.userName, imService.getUsername(),messageList.get(x).getTime()));
					else if(messageList.get(x).getMessage_from().contentEquals(imService.getUsername()))
						adapter.add(new MessageBubble(false, messageList.get(x).getMessage(), imService.getUsername(), friend.userName,messageList.get(x).getTime()));
				}
			}
		});		
		//new code end
		
		//DO NOT EDIT IT
		
		
/*		
		sendMessageButton.setOnClickListener(new OnClickListener(){
			CharSequence message;
			Handler handler = new Handler();
			public void onClick(View arg0) {
				message = messageText.getText();
				if (message.length()>0) 
				{		
					appendToMessageHistory(imService.getUsername(), message.toString());
								
					messageText.setText("");
					Thread thread = new Thread(){					
						public void run() {
							if (!imService.sendMessage(friend.userName, message.toString()))
							{
								handler.post(new Runnable(){	

									public void run() {
										showDialog(MESSAGE_CANNOT_BE_SENT);										
									}		
								});
							}
						}						
					};
					thread.start();					
				}
			}});
		
		messageText.setOnKeyListener(new OnKeyListener(){
			public boolean onKey(View v, int keyCode, KeyEvent event) 
			{
				if (keyCode == 66){
					sendMessageButton.performClick();
					return true;
				}
				return false;
			}
		});*/
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		int message = -1;
		switch (id)
		{
		case MESSAGE_CANNOT_BE_SENT:
			message = R.string.message_cannot_be_sent;
			break;
		}
		
		if (message == -1)
		{
			return null;
		}
		else
		{
			return new AlertDialog.Builder(Messaging.this)       
			.setMessage(message)
			.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					/* User clicked OK so do some stuff */
				}
			})        
			.create();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(messageReceiver);
		unbindService(mConnection);
		
		FriendController.setActiveFriend(null);
		
	}

	@Override
	protected void onResume() 
	{		
		super.onResume();
		bindService(new Intent(Messaging.this, IMService.class), mConnection , Context.BIND_AUTO_CREATE);
				
		IntentFilter i = new IntentFilter();
		i.addAction(IMService.TAKE_MESSAGE);
		
		registerReceiver(messageReceiver, i);
		
		FriendController.setActiveFriend(friend.userName);		
	}
	
	public class  MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) 
		{		
			Bundle extra = intent.getExtras();
			String username = extra.getString(FriendInfo.USERNAME);			
			String message = extra.getString(FriendInfo.MESSAGE);
			
			if (username != null && message != null)
			{
				if (friend.userName.equals(username)) {
				//	appendToMessageHistory(username, message);
			//		adapter.add(new OneComment(true, message));
					String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
	//				adapter.add(new MessageBubble(true, message, username, imService.getUsername()));
					adapter.add(new MessageBubble(true, message, username, imService.getUsername(),currentDateTimeString));
					editText1.setText("");
					
					String message_database=message.toString();
					MessageDataSource messagedatasource;
					messagedatasource = new MessageDataSource(Messaging.this);
				    messagedatasource.open();
				    
					ContentValues c=new ContentValues();
					c.put("message_from", friend.userName);
					c.put("message_to", imService.getUsername());
					c.put("message", message_database);
					c.put("time", currentDateTimeString);
					
					messagedatasource.insertRows(c, DATABASE);
					messagedatasource.close();
				}
				else {
					if (message.length() > 15) {
						message = message.substring(0, 15);
					}
					Toast.makeText(Messaging.this,  username + " says '"+
													message + "'",
													Toast.LENGTH_SHORT).show();		
				}
			}			
		}
		
	};
	
	private MessageReceiver messageReceiver = new MessageReceiver();
	
/*	private void appendToMessageHistory(String username, String message) {
		if (username != null && message != null) {
		/*	messageHistoryText.setTextColor(Color.BLUE);
			messageHistoryText.append(username + ":\n");
			messageHistoryText.setTextColor(Color.RED);
			messageHistoryText.append(message + "\n");
			messageHistoryText.append(Html.fromHtml("<font color=red>"+username+":\n"+"</font>"));
			messageHistoryText.append(Html.fromHtml("<font color=blue>"+message+"\n"+"</font>"));
		}
	}*/
}
