package com.messenger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;

import com.messenger.services.IMService;
import com.messenger.interfaces.IAppManager;

public class Stranger extends Activity{
	
	private static final int MESSAGE_CANNOT_BE_SENT = 0;
	private EditText messageText;
	private EditText messageHistoryText;
	private Button sendMessageButton;
	private IAppManager imService=null;
	public Button exitButton;
    public static final int EXIT_STRANGER = Menu.FIRST;
    public String username=new String();
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            imService = ((IMService.IMBinder)service).getService();  
        }

        public void onServiceDisconnected(ComponentName className) {
        	imService = null;
            Toast.makeText(Stranger.this, R.string.local_service_stopped,Toast.LENGTH_SHORT).show();
        }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle extras = this.getIntent().getExtras();
		
		final String guest_id=extras.getString("guest_id");
		username=extras.getString("id");
	//	final String IP=extras.getString("IP");
	//	final String port=extras.getString("port");
		String msg = extras.getString("message");
		
		setContentView(R.layout.stranger);
		setTitle("Messaging with "+username);
		
		messageHistoryText = (EditText) findViewById(R.id.messageHistory_stranger);
		messageText = (EditText) findViewById(R.id.message_stranger);
		messageText.requestFocus();			
		sendMessageButton = (Button) findViewById(R.id.sendMessageButton_stranger);
		exitButton=(Button)findViewById(R.id.exit);
		
		exitButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				imService.removeGuest(guest_id);
		//		finish();
			}
		});
		
		if (msg != null) 
		{
			this.appendToMessageHistory(username , msg);
			((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel((username+msg).hashCode());
		}
		
		sendMessageButton.setOnClickListener(new OnClickListener(){
			CharSequence message;
			Handler handler = new Handler();
			public void onClick(View arg0) {
				message = messageText.getText();
				if (message.length()>0) 
				{		
					appendToMessageHistory(guest_id, message.toString());//or put imService.getUsername() in place of guest
								
					messageText.setText("");
					Thread thread = new Thread(){					
						public void run() {
//							if (!imService.sendStrangerMessage(username, IP, Integer.parseInt(port), message.toString()))
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
		});
	}
	
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
			return new AlertDialog.Builder(Stranger.this)       
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
	protected void onPause() 
	{
		unbindService(mConnection);
		unregisterReceiver(messageReceiver);
		super.onPause();
	}

	@Override
	protected void onResume() 
	{		
		super.onResume();
		bindService(new Intent(Stranger.this, IMService.class), mConnection , Context.BIND_AUTO_CREATE);
	    		
		IntentFilter i = new IntentFilter();
		i.addAction(IMService.TAKE_MESSAGE);
		
		registerReceiver(messageReceiver, i);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {		
		boolean result = super.onCreateOptionsMenu(menu);
		
		 menu.add(0, EXIT_STRANGER, 0, R.string.exit_stranger);

		return result;
	}
	
	public class  MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) 
		{		
			Bundle extra = intent.getExtras();
			String Username = extra.getString("id");;			
			String message = extra.getString("message");
			
			if (Username != null && message != null)
			{
				if (Username.equals(username)) {
					appendToMessageHistory(Username, message);					
				}
				else {
					if (message.length() > 15) {
						message = message.substring(0, 15);
					}
					Toast.makeText(Stranger.this,  Username + " says '"+
													message + "'",
													Toast.LENGTH_SHORT).show();		
				}
			}			
		}
		
	};
	
	private MessageReceiver messageReceiver = new MessageReceiver();
	
	private void appendToMessageHistory(String username, String message) {
		if (username != null && message != null) {
			messageHistoryText.append(username + ":\n");								
			messageHistoryText.append(message + "\n");	
		}
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
	    
		switch(item.getItemId()) 
	    {
	    	case EXIT_STRANGER:
	    		exitButton.performClick();
	    		return true;
	    }
	       
	    return super.onMenuItemSelected(featureId, item);
	}
}
