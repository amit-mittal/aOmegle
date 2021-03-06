package com.messenger;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.messenger.interfaces.IAppManager;
import com.messenger.services.IMService;

public class Login extends Activity {	

    protected static final int NOT_CONNECTED_TO_SERVICE = 0;
	protected static final int FILL_BOTH_USERNAME_AND_PASSWORD = 1;
	protected static final int FILL_USERNAME_AND_PASSWORD_AND_STRANGER = 1;
	public static final String AUTHENTICATION_FAILED = "0";
	public static final String FRIEND_LIST = "FRIEND_LIST";
	protected static final int MAKE_SURE_USERNAME_AND_PASSWORD_CORRECT = 2 ;
	protected static final int NOT_CONNECTED_TO_NETWORK = 3;
	private EditText usernameText;
    private EditText passwordText;
    private Button cancelButton;
    private Button loginButton;
    private Button randomButton;
    private EditText dispayText; 
    private CheckBox rememberCheck;
    private TextView registerText;
    private IAppManager imService;
    public static final int SIGN_UP_ID = Menu.FIRST;
    public static final int EXIT_APP_ID = Menu.FIRST + 1;
    
    public static final String PREFS_NAME = "RememberMe";
	private static final String PREF_USERNAME = "username";
	private static final String PREF_PASSWORD = "password";
	private static final String PREF_DISPLAY_NAME = "displayname";

   
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            imService = ((IMService.IMBinder)service).getService();  
            
            if (imService.isUserAuthenticated() == true)
            {
            	Intent i = new Intent(Login.this, FriendList.class);																
				startActivity(i);
				Login.this.finish();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
        	imService = null;
            Toast.makeText(Login.this, R.string.local_service_stopped,
                    Toast.LENGTH_SHORT).show();
        }
    };
	
    
    
    /** Called when the activity is first created. */	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    

        /*
         * Start and bind the  imService 
         **/
    	startService(new Intent(Login.this,  IMService.class));			
	
               
        setContentView(R.layout.login_screen);
        setTitle("Login");
        
        
        
        loginButton = (Button) findViewById(R.id.login);
        randomButton=(Button)findViewById(R.id.random);
        cancelButton = (Button) findViewById(R.id.cancel_login);
        usernameText = (EditText) findViewById(R.id.userName);
        passwordText = (EditText) findViewById(R.id.password);
        rememberCheck = (CheckBox) findViewById(R.id.rem);
        registerText = (TextView) findViewById(R.id.register_me);
        dispayText = (EditText) findViewById(R.id.display_name);
        
        //if password and isername is remember me
        SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);   
        usernameText.setText(pref.getString(PREF_USERNAME, null));
        passwordText.setText(pref.getString(PREF_PASSWORD, null));
        dispayText.setText(pref.getString(PREF_DISPLAY_NAME, null));
        
        loginButton.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) 
			{					
				if (imService == null) {
					showDialog(NOT_CONNECTED_TO_SERVICE);
					return;
				}
				else if (imService.isNetworkConnected() == false)
				{
					showDialog(NOT_CONNECTED_TO_NETWORK);
					
				}
				else if (usernameText.length() > 0 && 
					passwordText.length() > 0 &&
					dispayText.length()>0)
				{
					
					Thread loginThread = new Thread(){
						private Handler handler = new Handler();
						@Override
						public void run() {
							String result = imService.authenticateUser(usernameText.getText().toString(), passwordText.getText().toString());
							if (result == null || result.equals(AUTHENTICATION_FAILED)) 
							{
								/*
								 * Authenticatin failed, inform the user
								 */
								handler.post(new Runnable(){
									public void run() {										
										showDialog(MAKE_SURE_USERNAME_AND_PASSWORD_CORRECT);
									}									
								});
														
							}
							else {
							
								/*
								 * if result not equal to authentication failed,
								 * result is equal to friend list of the user
								 */		
								handler.post(new Runnable(){
									public void run() {
										
										if(rememberCheck.isChecked())
										{
											String user=usernameText.getText().toString();
									    	String pass=passwordText.getText().toString();
									    	
									    	
									    	getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
									    	.edit()
									    	.putString(PREF_USERNAME, user)
									    	.putString(PREF_PASSWORD, pass)
									    	.commit();
										}
										else
										{
											getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
									    	.edit()
									    	.putString(PREF_USERNAME, "")
									    	.putString(PREF_PASSWORD, "")
									    	.commit();
										}
										
				/*						imService.authenticateStrangerUser(usernameText.getText().toString(), 
												passwordText.getText().toString(),
												dispayText.getText().toString());
					*/					
										Intent i = new Intent(Login.this, FriendList.class);												
										//i.putExtra(FRIEND_LIST, result);						
										startActivity(i);	
										Login.this.finish();
									}									
								});
								
							}
							
						}
					};
					loginThread.start();
					
				}
				else {
					/*
					 * Username or Password is not filled, alert the user
					 */
					showDialog(FILL_BOTH_USERNAME_AND_PASSWORD);
				}				
			}       	
        });
        
        randomButton.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) 
			{					
				if (imService == null) {
					showDialog(NOT_CONNECTED_TO_SERVICE);
					return;
				}
				else if (imService.isNetworkConnected() == false)
				{
					showDialog(NOT_CONNECTED_TO_NETWORK);
					
				}
				else if (usernameText.length() > 0 && 
					passwordText.length() > 0 &&
					dispayText.length()>0)
				{
					
					Thread loginThread = new Thread(){
						private Handler handler = new Handler();
						@Override
						public void run() {
							String result = imService.authenticateStrangerUser(usernameText.getText().toString(), 
									passwordText.getText().toString(),
									dispayText.getText().toString());
							if (result == null || result.equals(AUTHENTICATION_FAILED)) 
							{
								/*
								 * Authenticatin failed, inform the user
								 */
								handler.post(new Runnable(){
									public void run() {										
										showDialog(MAKE_SURE_USERNAME_AND_PASSWORD_CORRECT);
									}									
								});
														
							}
							else {
							
								/*
								 * if result not equal to authentication failed,
								 * result is equal to friend list of the user
								 */		
								handler.post(new Runnable(){
									public void run() {
										if(rememberCheck.isChecked())
										{
											String user=usernameText.getText().toString();
									    	String pass=passwordText.getText().toString();
									    	String disp=dispayText.getText().toString();
									    	
									    	getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
									    	.edit()
									    	.putString(PREF_USERNAME, user)
									    	.putString(PREF_PASSWORD, pass)
									    	.putString(PREF_DISPLAY_NAME, disp)
									    	.commit();
										}
										else
										{
											getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
									    	.edit()
									    	.putString(PREF_USERNAME, "")
									    	.putString(PREF_PASSWORD, "")
									    	.putString(PREF_DISPLAY_NAME, "")
									    	.commit();
										}
										
					/*					imService.authenticateUser(usernameText.getText().toString(), 
												passwordText.getText().toString());
						*/				
										Intent i = new Intent(Login.this,StrangerList.class);												
										//i.putExtra(FRIEND_LIST, result);						
										startActivity(i);	
										Login.this.finish();
									}									
								});
								
							}
							
						}
					};
					loginThread.start();
					
				}
				else {
					/*
					 * Username or Password is not filled, alert the user
					 */
					showDialog(FILL_BOTH_USERNAME_AND_PASSWORD);
				}				
			}       	
        });
        /*
        randomButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String id=imService.addGuest();
				String result[]=imService.getStranger(id);
				
				Intent in=new Intent(Login.this,Stranger.class);
				in.putExtra("guest_id", id);
				in.putExtra("id", result[0]);
				in.putExtra("IP", result[1]);
				in.putExtra("port", result[2]);
				startActivity(in);
			}
		});*/
        
        cancelButton.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) 
			{					
				usernameText.setText("");
				passwordText.setText("");
				dispayText.setText("");
				
				getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
		    	.edit()
		    	.putString(PREF_USERNAME, "")
		    	.putString(PREF_PASSWORD, "")
		    	.putString(PREF_DISPLAY_NAME, "")
		    	.commit();
				
			}
        	
        });
        
        registerText.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent in=new Intent(Login.this,SignUp.class);
				startActivity(in);
			}
		});
        
    }
    
    @Override
    protected Dialog onCreateDialog(int id) 
    {    	
    	int message = -1;    	
    	switch (id) 
    	{
    		case NOT_CONNECTED_TO_SERVICE:
    			message = R.string.not_connected_to_service;			
    			break;
    		case FILL_BOTH_USERNAME_AND_PASSWORD:
    			message = R.string.fill_both_username_and_password;
    			break;
    		case MAKE_SURE_USERNAME_AND_PASSWORD_CORRECT:
    			message = R.string.make_sure_username_and_password_correct;
    			break;
    		case NOT_CONNECTED_TO_NETWORK:
    			message = R.string.not_connected_to_network;
    			break;
    		default:
    			break;
    	}
    	
    	if (message == -1) 
    	{
    		return null;
    	}
    	else 
    	{
    		return new AlertDialog.Builder(Login.this)       
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
		super.onPause();
	}

	@Override
	protected void onResume() 
	{		
		bindService(new Intent(Login.this, IMService.class), mConnection , Context.BIND_AUTO_CREATE);
	    		
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		boolean result = super.onCreateOptionsMenu(menu);
		
		 menu.add(0, SIGN_UP_ID, 0, R.string.sign_up);
//		 menu.add(0, EXIT_APP_ID, 0, R.string.exit_application);

		return result;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
	    
		switch(item.getItemId()) 
	    {
	    	case SIGN_UP_ID:
	    		Intent i = new Intent(Login.this, SignUp.class);
	    		startActivity(i);
	    		return true;
	/*    	case EXIT_APP_ID:
	    		cancelButton.performClick();
	    		return true;
	  */  }
	       
	    return super.onMenuItemSelected(featureId, item);
	}
}