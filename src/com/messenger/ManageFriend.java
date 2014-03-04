package com.messenger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.messenger.interfaces.IAppManager;
import com.messenger.services.IMService;

public class ManageFriend extends Activity {
	
	protected static final int TYPE_FRIEND_USERNAME = 0;
	protected static final int DELETE_FRIEND_USERNAME = 1;
	private EditText friendUserNameText;
	private Button addFriendButton;
	private Button cancelButton;
	private EditText deleteUsernameText;
	private Button deleteFriendButton;
	private Button resetButton;
	private IAppManager imService;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.manage_friend);
		setTitle("Manage friend list");
		
		addFriendButton = (Button) findViewById(R.id.addFriend);
		cancelButton = (Button) findViewById(R.id.cancel1);
		friendUserNameText = (EditText) findViewById(R.id.newFriendUsername);
		deleteFriendButton= (Button)findViewById(R.id.deleteFriend);
		resetButton= (Button) findViewById(R.id.cancel2);
		deleteUsernameText= (EditText) findViewById(R.id.deleteFriendUsername);
		
		addFriendButton.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				if ( friendUserNameText.length() > 0 )
				{
					Thread thread = new Thread(){
							@Override
							public void run() {
								imService.addNewFriendRequest(friendUserNameText.getText().toString());
							}
						};
						thread.start();
						
						Toast.makeText(ManageFriend.this, R.string.request_sent, Toast.LENGTH_SHORT)
					       .show();
						finish();					
				}
				else{					
					showDialog(TYPE_FRIEND_USERNAME);					
				}
			}
			
		});
		
		deleteFriendButton.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				if ( deleteUsernameText.length() > 0 )
				{
					Thread thread = new Thread(){
							@Override
							public void run() {
								imService.deleteFriendRequest(deleteUsernameText.getText().toString());
							}
						};
						thread.start();
						
						Toast.makeText(ManageFriend.this, R.string.delete_request_sent, Toast.LENGTH_SHORT)
					       .show();
						finish();					
				}
				else{					
					showDialog(DELETE_FRIEND_USERNAME);					
				}
			}
			
		});
		
		cancelButton.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				friendUserNameText.setText("");
			}
			
		});
		
		resetButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				deleteUsernameText.setText("");
			}
		});
		
	
	}
	
	@Override
	protected void onResume() {
	
		super.onResume();
		bindService(new Intent(ManageFriend.this, IMService.class), mConnection , Context.BIND_AUTO_CREATE);

	}
	
	@Override
	protected void onPause() {		
		super.onPause();
		unbindService(mConnection);
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {
		
		public void onServiceConnected(ComponentName className, IBinder service) {          
			imService = ((IMService.IMBinder)service).getService();   		
		}
		public void onServiceDisconnected(ComponentName className) {          
			imService = null;
			Toast.makeText(ManageFriend.this, R.string.local_service_stopped,
					Toast.LENGTH_SHORT).show();
		}
	};
	
	
	
	 protected Dialog onCreateDialog(int id) {
	        switch (id) 
	        {
	        	case TYPE_FRIEND_USERNAME:
	        	{				 	                 
	        		 return new AlertDialog.Builder(ManageFriend.this)
	        			.setTitle(R.string.add_new_friend)
	        			.setMessage(R.string.type_friend_username)
	        			.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
	        				public void onClick(DialogInterface dialog, int whichButton) {
	        				}
	        			})	                
	        			.create();
	        	}
	        	case DELETE_FRIEND_USERNAME:
	        	{				 	                 
	        		 return new AlertDialog.Builder(ManageFriend.this)
	        			.setTitle(R.string.delete_friend)
	        			.setMessage(R.string.type_friend_username)
	        			.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
	        				public void onClick(DialogInterface dialog, int whichButton) {
	        				}
	        			})	                
	        			.create();
	        	}
	   
	        
	        	default:
	        			return null;
	        }
	 }


}
