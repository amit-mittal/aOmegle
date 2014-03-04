/* 
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.messenger.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.messenger.Login;
import com.messenger.Messaging;
import com.messenger.StrangerMessaging;
import com.messenger.R;
import com.messenger.communication.SocketOperator;
import com.messenger.interfaces.IAppManager;
import com.messenger.interfaces.ISocketOperator;
import com.messenger.interfaces.IUpdateData;
import com.messenger.interfaces.IUpdateStranger;
import com.messenger.tools.FriendController;
import com.messenger.tools.StrangerController;
import com.messenger.tools.XMLHandler;
import com.messenger.tools.XMLStranger;
import com.messenger.types.FriendInfo;
import com.messenger.types.StrangerInfo;

/**
 * This is an example of implementing an application service that runs locally
 * in the same process as the application.  The {@link LocalServiceController}
 * and {@link LocalServiceBinding} classes show how to interact with the
 * service.
 *
 * <p>Notice the use of the {@link NotificationManager} when interesting things
 * happen in the service.  This is generally how background services should
 * interact with the user, rather than doing something more disruptive such as
 * calling startActivity().
 */
@SuppressWarnings("unused")
public class IMService extends Service implements IAppManager, IUpdateData, IUpdateStranger {
//	private NotificationManager mNM;
	
	public static final String TAKE_MESSAGE = "Take_Message";
	public static final String FRIEND_LIST_UPDATED = "Take Friend List";
	public static final String STRANGER_LIST_UPDATED = "Take Stranger List";
	public ConnectivityManager conManager = null; 
	private final int UPDATE_TIME_PERIOD = 15000;
//	private static final int LISTENING_PORT_NO = 8956;
	private String rawFriendList = new String();
	private String rawStrangerList = new String();


	ISocketOperator socketOperator = new SocketOperator(this);

	private final IBinder mBinder = new IMBinder();
	private String username;
	private String password;
	private String displayName;
	private String userKey;
	private boolean authenticatedUser = false;
	 // timer to take the updated data from server
	private Timer timer;
	
	private NotificationManager mNM;
	
	
	public class IMBinder extends Binder {
		public IAppManager getService() {
			return IMService.this;
		}
		
	}
	   
    @Override
    public void onCreate() 
    {   	
         mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
     //   showNotification();
    	conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    	
    	
    	// Timer is used to take the friendList info every UPDATE_TIME_PERIOD;
		timer = new Timer();   
		
		Thread thread = new Thread()
		{
			@Override
			public void run() {			
				
				//socketOperator.startListening(LISTENING_PORT_NO);
				Random random = new Random();
				int tryCount = 0;
				while (socketOperator.startListening(10000 + random.nextInt(20000))  == 0 )
				{		
					tryCount++; 
					if (tryCount > 10)
					{
						// if it can't listen a port after trying 10 times, give up...
						break;
					}
					
				}
			}
		};		
		thread.start();
    
    }

/*
    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(R.string.local_service_started);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
    }
*/	

	@Override
	public IBinder onBind(Intent intent) 
	{
		return mBinder;
	}




	/**
	 * Show a notification while this service is running.
	 * @param msg 
	 **/
    private void showNotification(String username, String msg) 
	{       
        // Set the icon, scrolling text and timestamp
    	String title = username + ": " + 
     				((msg.length() < 5) ? msg : msg.substring(0, 5)+ "...");
        Notification notification = new Notification(R.drawable.stat_sample, 
        					title,
                System.currentTimeMillis());

        Intent i = new Intent(this, Messaging.class);
        i.putExtra(FriendInfo.USERNAME, username);
        i.putExtra(FriendInfo.MESSAGE, msg);	
        
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                i, 0);

        // Set the info for the views that show in the notification panel.
        // msg.length()>15 ? msg : msg.substring(0, 15);
        notification.setLatestEventInfo(this, "New message from " + username,
                       						msg, 
                       						contentIntent);
        
        //TODO: it can be improved, for instance message coming from same user may be concatenated 
        // next version
        
        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNM.notify((username+msg).hashCode(), notification);
    }
    
    private void showStrangerNotification(String username, String msg) 
	{       
        // Set the icon, scrolling text and timestamp
    	String title = username + ": " + 
     				((msg.length() < 5) ? msg : msg.substring(0, 5)+ "...");
        Notification notification = new Notification(R.drawable.stat_sample, 
        					title,
                System.currentTimeMillis());

        Intent i = new Intent(this, StrangerMessaging.class);
        i.putExtra(StrangerInfo.USERNAME, username);
        i.putExtra(StrangerInfo.MESSAGE, msg);	
        
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                i, 0);

        // Set the info for the views that show in the notification panel.
        // msg.length()>15 ? msg : msg.substring(0, 15);
        notification.setLatestEventInfo(this, "New message from " + username,
                       						msg, 
                       						contentIntent);
        
        //TODO: it can be improved, for instance message coming from same user may be concatenated 
        // next version
        
        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNM.notify((username+msg).hashCode(), notification);
    }
	 

	public String getUsername() {		
		return username;
	}

	public boolean sendMessage(String  username, String message) {
		FriendInfo friendInfo = FriendController.getFriendInfo(username);
		String IP = friendInfo.ip;
		IP = "10.0.2.2";
		int port = Integer.parseInt(friendInfo.port);
		
		String msg = FriendInfo.USERNAME +"=" + URLEncoder.encode(this.username) +
		 "&" + FriendInfo.USER_KEY + "=" + URLEncoder.encode(userKey) +
		 "&" + FriendInfo.MESSAGE + "=" + URLEncoder.encode(message) +
		 "&";
		
		Log.i("inIMSERVICE_send", "in imservice, sending message from friend");
		
		return socketOperator.sendMessage(msg, IP,  port);
	}
	
	public boolean sendStrangerMessage(String  username, String message) {
		StrangerInfo strangerInfo = StrangerController.getStrangerInfo(username);
		String IP = strangerInfo.ip;
		IP = "10.0.2.2";
		int port = Integer.parseInt(strangerInfo.port);
		
		String msg = StrangerInfo.USERNAME +"=" + URLEncoder.encode(this.username) +
		 "&" + StrangerInfo.USER_KEY + "=" + URLEncoder.encode(userKey) +
		 "&" + StrangerInfo.MESSAGE + "=" + URLEncoder.encode(message) +
		 "&";
		
		Log.i("inIMSERVICE_send", "in imservice, sending message from stranger");
		
		return socketOperator.sendMessage(msg, IP,  port);
	}

	
	private String getFriendList() 	{		
		// after authentication, server replies with friendList xml
		
		 rawFriendList = socketOperator.sendHttpRequest(getAuthenticateUserParams(username, password));
		 if (rawFriendList != null) {
			 this.parseFriendInfo(rawFriendList);
		 }
		 return rawFriendList;
	}
	
	private String getStrangerList() 	{		
		// after authentication, server replies with friendList xml
		
		 rawStrangerList = socketOperator.sendHttpRequest(getStrangerAuthenticateUserParams(username, password, displayName));
		 if (rawStrangerList != null) {
			 this.parseStrangerInfo(rawStrangerList);
		 }
		 return rawStrangerList;
	}

	/**
	 * authenticateUser: it authenticates the user and if succesful
	 * it returns the friend list or if authentication is failed 
	 * it returns the "0" in string type
	 * */
	public String authenticateUser(String usernameText, String passwordText) 
	{
		this.username = usernameText;
		this.password = passwordText;	
		
		this.authenticatedUser = false;
		
		String result = this.getFriendList(); //socketOperator.sendHttpRequest(getAuthenticateUserParams(username, password));
		if (result != null && !result.equals(Login.AUTHENTICATION_FAILED)) 
		{			
			// if user is authenticated then return string from server is not equal to AUTHENTICATION_FAILED
			this.authenticatedUser = true;
			rawFriendList = result;
			
			Intent i = new Intent(FRIEND_LIST_UPDATED);					
			i.putExtra(FriendInfo.FRIEND_LIST, rawFriendList);
			sendBroadcast(i);
			
		/*	timer.schedule(new TimerTask()
			{			
				public void run() 
				{
					try {					
						//rawFriendList = IMService.this.getFriendList();
						// sending friend list 
						Intent i = new Intent(FRIEND_LIST_UPDATED);
						String tmp = IMService.this.getFriendList();
						if (tmp != null) {
							i.putExtra(FriendInfo.FRIEND_LIST, tmp);
							sendBroadcast(i);	
							Log.i("friend list broadcast sent ", "");
						}
						else {
							Log.i("friend list returned null", "");
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}					
				}			
			}, UPDATE_TIME_PERIOD, UPDATE_TIME_PERIOD);
		*/}
		
		return result;		
	}
	
	public void refreshFriendList(){
		try {					
			//rawFriendList = IMService.this.getFriendList();
			// sending friend list 
			Intent i = new Intent(FRIEND_LIST_UPDATED);
			String tmp = IMService.this.getFriendList();
			if (tmp != null) {
				i.putExtra(FriendInfo.FRIEND_LIST, tmp);
				sendBroadcast(i);	
				Log.i("friend list broadcast sent ", "");
			}
			else {
				Log.i("friend list returned null", "");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String authenticateStrangerUser(String usernameText, String passwordText, String displayText) 
	{
		this.username = usernameText;
		this.password = passwordText;
		this.displayName=displayText;
		
		this.authenticatedUser = false;
		
		String result = this.getStrangerList(); //socketOperator.sendHttpRequest(getAuthenticateUserParams(username, password));
		if (result != null && !result.equals(Login.AUTHENTICATION_FAILED)) 
		{			
			// if user is authenticated then return string from server is not equal to AUTHENTICATION_FAILED
			this.authenticatedUser = true;
			rawStrangerList = result;
			
			Intent i = new Intent(STRANGER_LIST_UPDATED);					
			i.putExtra(StrangerInfo.STRANGER_LIST, rawStrangerList);
			sendBroadcast(i);
			
		/*	timer.schedule(new TimerTask()
			{			
				public void run() 
				{
					try {					
						//rawStrangerList = IMService.this.getStrangerList();
						// sending stranger list 
						Intent i = new Intent(STRANGER_LIST_UPDATED);
						String tmp = IMService.this.getStrangerList();
						if (tmp != null) {
							i.putExtra(StrangerInfo.STRANGER_LIST, tmp);
							sendBroadcast(i);	
							Log.i("stranger list broadcast sent ", "");
						}
						else {
							Log.i("stranger list returned null", "");
						}
					}
					catch (Exception e) {
				//		e.printStackTrace();
					}					
				}			
			}, UPDATE_TIME_PERIOD, UPDATE_TIME_PERIOD);
*/		}
		
		return result;		
	}
	
	public void refreshStrangerList(){
		try {					
			//rawStrangerList = IMService.this.getStrangerList();
			// sending stranger list 
			Intent i = new Intent(STRANGER_LIST_UPDATED);
			String tmp = IMService.this.getStrangerList();
			if (tmp != null) {
				i.putExtra(StrangerInfo.STRANGER_LIST, tmp);
				sendBroadcast(i);	
				Log.i("stranger list broadcast sent ", "");
			}
			else {
				Log.i("stranger list returned null", "");
			}
		}
		catch (Exception e) {
	//		e.printStackTrace();
		}
	}

	public void messageReceived(String message) 
	{				
		String[] params = message.split("&");
		String username= new String();
		String userKey = new String();
		String msg = new String();
		for (int i = 0; i < params.length; i++) {
			String[] localpar = params[i].split("=");
			if (localpar[0].equals(FriendInfo.USERNAME)||localpar[0].equals(StrangerInfo.USERNAME)) {
				username = URLDecoder.decode(localpar[1]);
			}
			else if (localpar[0].equals(FriendInfo.USER_KEY)||localpar[0].equals(StrangerInfo.USER_KEY)) {
				userKey = URLDecoder.decode(localpar[1]);
			}
			else if (localpar[0].equals(FriendInfo.MESSAGE)||localpar[0].equals(StrangerInfo.MESSAGE)) {
				msg = URLDecoder.decode(localpar[1]);
			}			
		}
		Log.i("Message received in Friend service", message);
		
		FriendInfo friend = FriendController.checkFriend(username, userKey);
		if ( friend != null)
		{			
			Intent i = new Intent(TAKE_MESSAGE);
		
			i.putExtra(FriendInfo.USERNAME, friend.userName);			
			i.putExtra(FriendInfo.MESSAGE, msg);			
			sendBroadcast(i);
			String activeFriend = FriendController.getActiveFriend();
			if (activeFriend == null || activeFriend.equals(username) == false) 
			{
				showNotification(username, msg);
			}
			Log.i("TAKE_MESSAGE broadcast sent by im service", "");
		}	
		
	}
	
	public void messageStrangerReceived(String message) 
	{				
		String[] params = message.split("&");
		String username= new String();
		String userKey = new String();
		String msg = new String();
		
		for (int i = 0; i < params.length; i++) {
			String[] localpar = params[i].split("=");
			if (localpar[0].equals(StrangerInfo.USERNAME)) {
				username = URLDecoder.decode(localpar[1]);
			}
			else if (localpar[0].equals(StrangerInfo.USER_KEY)) {
				userKey = URLDecoder.decode(localpar[1]);
			}
			else if (localpar[0].equals(StrangerInfo.MESSAGE)) {
				msg = URLDecoder.decode(localpar[1]);
			}			
		}
		Log.i("Message received in Stranger service", message);
		
		StrangerInfo stranger = StrangerController.checkStranger(username, userKey);
		if ( stranger != null)
		{			
			Intent i = new Intent(TAKE_MESSAGE);
			
			i.putExtra(StrangerInfo.USERNAME, stranger.userName);			
			i.putExtra(StrangerInfo.MESSAGE, msg);			
			sendBroadcast(i);
			String activeStranger = StrangerController.getActiveStranger();
			if (activeStranger == null || activeStranger.equals(username) == false) 
			{
				showStrangerNotification(username, msg);
			}
			Log.i("TAKE_MESSAGE broadcast sent by im service", "");
		}	
		
	}
	
	private String getAuthenticateUserParams(String usernameText, String passwordText) 
	{			
		String params = "username=" + URLEncoder.encode(usernameText) +
						"&password="+ URLEncoder.encode(passwordText) +
						"&action="  + URLEncoder.encode("authenticateUser")+
						"&port="    + URLEncoder.encode(Integer.toString(socketOperator.getListeningPort())) +
						"&";		
		
		return params;		
	}
	
	private String getStrangerAuthenticateUserParams(String usernameText, String passwordText, String displayText) 
	{			
		String params = "username=" + URLEncoder.encode(usernameText) +
						"&password="+ URLEncoder.encode(passwordText) +
						"&action="  + URLEncoder.encode("strangerauthenticateUser")+
						"&port="    + URLEncoder.encode(Integer.toString(socketOperator.getListeningPort())) +
						"&displayname=" + URLEncoder.encode(displayText) +
						"&";		
		
		return params;		
	}

	public void setUserKey(String value) 
	{
		this.userKey = value;		
	}

	public boolean isNetworkConnected() {
		return conManager.getActiveNetworkInfo().isConnected();
	}
	
	public boolean isUserAuthenticated(){
		return authenticatedUser;
	}
	
	public String getLastRawFriendList() {		
		return this.rawFriendList;
	}
	
	public String getLastRawStrangerList() {		
		return this.rawStrangerList;
	}
	
	@Override
	public void onDestroy() {
		Log.i("IMService is being destroyed", "...");
		super.onDestroy();
	}
	
	public void exit() 
	{
		String params = "username=" + username +
				"&password=" + password +
				"&action=" + "LogoutUser"+
				"&";
		
		socketOperator.sendHttpRequest(params);	
		
		timer.cancel();
		socketOperator.exit(); 
		socketOperator = null;
		this.stopSelf();
	}
	
	public String signUpUser(String usernameText, String passwordText,
			String emailText,String strangerName) 
	{
		TelephonyManager tMgr =(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		String mPhoneNumber = tMgr.getLine1Number();
		
		String params = "username=" + usernameText +
						"&password=" + passwordText +
						"&action=" + "signUpUser"+
						"&email=" + emailText+
						"&stranger=" + strangerName+
						"&phone=" + mPhoneNumber +
						"&";
		
		String result = socketOperator.sendHttpRequest(params);		
		
		return result;
	}

	public String addNewFriendRequest(String friendUsername) 
	{
		String params = "username=" + this.username +
		"&password=" + this.password +
		"&action=" + "addNewFriend" +
		"&friendUserName=" + friendUsername +
		"&";

		String result = socketOperator.sendHttpRequest(params);
		
		return result;
	}
	
	public String deleteFriendRequest(String friendUsername) 
	{
		String params = "username=" + this.username +
		"&password=" + this.password +
		"&action=" + "deleteFriend" +
		"&friendUserName=" + friendUsername +
		"&";

		String result = socketOperator.sendHttpRequest(params);
		
		return result;
	}

	public String sendFriendsReqsResponse(String approvedFriendNames,
			String discardedFriendNames) 
	{
		String params = "username=" + this.username +
		"&password=" + this.password +
		"&action=" + "responseOfFriendReqs"+
		"&approvedFriends=" + approvedFriendNames +
		"&discardedFriends=" +discardedFriendNames +
		"&";

		String result = socketOperator.sendHttpRequest(params);		
		
		return result;
		
	} 
	
	private void parseFriendInfo(String xml)
	{			
		try 
		{
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			sp.parse(new ByteArrayInputStream(xml.getBytes()), new XMLHandler(IMService.this));		
		} 
		catch (ParserConfigurationException e) {			
			e.printStackTrace();
		}
		catch (SAXException e) {			
			e.printStackTrace();
		} 
		catch (IOException e) {			
			e.printStackTrace();
		}	
	}
	
	private void parseStrangerInfo(String xml)
	{			
		try 
		{
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			sp.parse(new ByteArrayInputStream(xml.getBytes()), new XMLStranger(IMService.this));		
		} 
		catch (ParserConfigurationException e) {			
			e.printStackTrace();
		}
		catch (SAXException e) {			
			e.printStackTrace();
		} 
		catch (IOException e) {			
			e.printStackTrace();
		}	
	}

	public void updateData(FriendInfo[] friends,
			FriendInfo[] unApprovedFriends, String userKey) 
	{
		this.setUserKey(userKey);
		//FriendController.		
		FriendController.setFriendsInfo(friends);
		FriendController.setUnapprovedFriendsInfo(unApprovedFriends);
		
	}
	
	public String addGuest(){
		String params ="action=" + "addGuest"+
						"&port="+URLEncoder.encode(Integer.toString(socketOperator.getListeningPort()))+
						"&";
		
		String result=socketOperator.sendHttpRequest(params);
		
		this.username=result;
		
		return result;
	}
	
	public void removeGuest(String guest_id){		
		String params ="&id=" + guest_id+
				"&action=" + "removeGuest";
		
		Log.i("removeGuest", "guest id to be deleted is: "+guest_id);
		
		socketOperator.sendHttpRequest(params);
	}
	
	public String[] getStranger(String guest_id){
		String params1 ="action=" + "getStranger"+
				"&id="+guest_id+
				"&type="+"IP"+
				"&";
		String strangerIP = socketOperator.sendHttpRequest(params1);
		
		String params2 ="action=" + "getStranger"+
				"&id="+guest_id+
				"&type="+"id"+
				"&";
		
		String strangerid=socketOperator.sendHttpRequest(params2);
		
		String params3 ="action=" + "getStranger"+
				"&id="+guest_id+
				"&type="+"port"+
				"&";
		
		String strangerport=socketOperator.sendHttpRequest(params3);
		
		String result[]={strangerid,strangerIP,strangerport};
		
		return result;
	}

	@Override
	public void updateData(StrangerInfo[] strangers,
			StrangerInfo[] unApprovedStrangers, String userKey) {
		this.setUserKey(userKey);
		//StrangerController.		
		StrangerController.setStrangersInfo(strangers);
		StrangerController.setUnapprovedStrangersInfo(unApprovedStrangers);
		
	}
}