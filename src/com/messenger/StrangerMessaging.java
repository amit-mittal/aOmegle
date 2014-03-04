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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

import com.messenger.chat.*;
import com.messenger.database.Message;
import com.messenger.database.MessageDataSource;
import com.messenger.interfaces.IAppManager;
import com.messenger.services.IMService;
import com.messenger.tools.StrangerController;
import com.messenger.types.StrangerInfo;

public class StrangerMessaging extends Activity {

	private static final int MESSAGE_CANNOT_BE_SENT = 0;
	private static final String DATABASE="messages_database";
//	private EditText messageText;
//	private EditText messageHistoryText;
	private Button sendMessageButton;
	private Button getChatButton;
	private IAppManager imService;
	private StrangerInfo stranger = new StrangerInfo();
	
	private DiscussArrayAdapter adapter;
	private ListView lv;
	private EditText editText1;
	
	private ServiceConnection mConnection = new ServiceConnection() {
      
		public void onServiceConnected(ComponentName className, IBinder service) {          
            imService = ((IMService.IMBinder)service).getService();          
        }
        public void onServiceDisconnected(ComponentName className) {          
        	imService = null;
            Toast.makeText(StrangerMessaging.this, R.string.local_service_stopped,
                    Toast.LENGTH_SHORT).show();
        }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);	   
		
		setContentView(R.layout.messaging_screen); //messaging_screen);
		
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
									if (!imService.sendStrangerMessage(stranger.userName, message.toString()))
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
					
					
	//				adapter.add(new OneComment(false, editText1.getText().toString()));
					String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
					adapter.add(new MessageBubble(false, editText1.getText().toString(), imService.getUsername(), stranger.displayName,currentDateTimeString));
					editText1.setText("");
					
					String message_database=message.toString();
					MessageDataSource messagedatasource;
					messagedatasource = new MessageDataSource(StrangerMessaging.this);
				    messagedatasource.open();
				    
				    
					ContentValues c=new ContentValues();
					c.put("message_from", imService.getUsername());
					c.put("message_to", stranger.displayName);
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
			    AlertDialog.Builder builder2=new AlertDialog.Builder(StrangerMessaging.this);
			    builder2.setMessage(R.string.delete_message);
			    builder2.setPositiveButton("OK",new DialogInterface.OnClickListener() {
			    	public void onClick(DialogInterface dialog, int which) {
			    		MessageBubble bubble=adapter.getItem(position);
			               
						MessageDataSource messagedatasource;
						messagedatasource = new MessageDataSource(StrangerMessaging.this);
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
		messageText.requestFocus();*/
		sendMessageButton = (Button) findViewById(R.id.sendMessageButton);
		getChatButton=(Button) findViewById(R.id.getchat);
		
		Bundle extras = this.getIntent().getExtras();
		
		stranger.userName = extras.getString(StrangerInfo.USERNAME);
		stranger.ip = extras.getString(StrangerInfo.IP);
		stranger.port = extras.getString(StrangerInfo.PORT);
		String msg = extras.getString(StrangerInfo.MESSAGE);
		
		
		setTitle("Messaging with " + stranger.userName);
	
		
	//	EditText strangerUserName = (EditText) findViewById(R.id.strangerUserName);
	//	strangerUserName.setText(stranger.userName);
		
		if (msg != null) 
		{
//			adapter.add(new OneComment(true, msg));
			String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
			adapter.add(new MessageBubble(true, msg, stranger.userName, imService.getUsername(),currentDateTimeString));
			editText1.setText("");
		//	this.appendToMessageHistory(stranger.userName , msg);
			((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel((stranger.userName+msg).hashCode());
		}
		
		sendMessageButton.setOnClickListener(new OnClickListener(){
			CharSequence message;
			Handler handler = new Handler();
			public void onClick(View arg0) {
				message = editText1.getText();
				if (message.length()>0) 
				{
					String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
					adapter.add(new MessageBubble(false, editText1.getText().toString(), imService.getUsername(), stranger.displayName,currentDateTimeString));
	//				adapter.add(new OneComment(false, editText1.getText().toString()));
					editText1.setText("");
					
					String message_database=message.toString();
					MessageDataSource messagedatasource;
					messagedatasource = new MessageDataSource(StrangerMessaging.this);
				    messagedatasource.open();
				    
				    
					ContentValues c=new ContentValues();
					c.put("message_from", imService.getUsername());
					c.put("message_to", stranger.displayName);
					c.put("message", message_database);
					c.put("time", currentDateTimeString);
					
					messagedatasource.insertRows(c, DATABASE);
					messagedatasource.close();
					
					Thread thread = new Thread(){					
						public void run() {
							if (!imService.sendStrangerMessage(stranger.userName, message.toString()))
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
		
		getChatButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ArrayList<Message> messageList = new ArrayList<Message>();
				MessageDataSource db=new MessageDataSource(StrangerMessaging.this);
				
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
					if(messageList.get(x).getMessage_from().contentEquals(stranger.userName))
						adapter.add(new MessageBubble(true, messageList.get(x).getMessage(), stranger.displayName, imService.getUsername(),messageList.get(x).getTime()));
					else if(messageList.get(x).getMessage_to().contentEquals(stranger.userName))
						adapter.add(new MessageBubble(false, messageList.get(x).getMessage(), imService.getUsername(), stranger.displayName,messageList.get(x).getTime()));
				}
			}
		});
		
/*		sendMessageButton.setOnClickListener(new OnClickListener(){
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
							if (!imService.sendStrangerMessage(stranger.userName, message.toString()))
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
			return new AlertDialog.Builder(StrangerMessaging.this)       
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
		
		StrangerController.setActiveStranger(null);
		
	}

	@Override
	protected void onResume() 
	{		
		super.onResume();
		bindService(new Intent(StrangerMessaging.this, IMService.class), mConnection , Context.BIND_AUTO_CREATE);
				
		IntentFilter i = new IntentFilter();
		i.addAction(IMService.TAKE_MESSAGE);
		
		registerReceiver(messageReceiver, i);
		
		StrangerController.setActiveStranger(stranger.userName);		
	}
	
	public class  MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) 
		{		
			Bundle extra = intent.getExtras();
			String username = extra.getString(StrangerInfo.USERNAME);			
			String message = extra.getString(StrangerInfo.MESSAGE);
			
			if (username != null && message != null)
			{
				if (stranger.userName.equals(username)) {
					String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
					adapter.add(new MessageBubble(true, message, stranger.displayName, imService.getUsername(),currentDateTimeString));
				//	appendToMessageHistory(username, message);
//					adapter.add(new OneComment(true, message));
					editText1.setText("");
				}
				else {
					if (message.length() > 15) {
						message = message.substring(0, 15);
					}
					Toast.makeText(StrangerMessaging.this,  username + " says '"+
													message + "'",
													Toast.LENGTH_SHORT).show();		
				}
			}			
		}
		
	};
	
	private MessageReceiver messageReceiver = new MessageReceiver();
	
/*	private void appendToMessageHistory(String username, String message) {
		if (username != null && message != null) {
			messageHistoryText.append(username + ":\n");								
			messageHistoryText.append(message + "\n");	
		}
	}*/
}
