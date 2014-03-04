package com.messenger.interfaces;


public interface IAppManager {
	
	public String getUsername();
	public boolean sendMessage(String username, String message);

	public String authenticateUser(String usernameText, String passwordText); 
	public void messageReceived(String message);
//	public void setUserKey(String value);
	public boolean isNetworkConnected();
	public boolean isUserAuthenticated();
	public String getLastRawFriendList();
	public void exit();
	public String signUpUser(String usernameText, String passwordText, String email,String strangerName);
	public String addNewFriendRequest(String friendUsername);
	public String sendFriendsReqsResponse(String approvedFriendNames,
			String discardedFriendNames);
	public String addGuest();
	public void removeGuest(String guest_id);
	public String[] getStranger(String guest_id);
	
	public boolean sendStrangerMessage(String id, String message);
	public void messageStrangerReceived(String message);
	public String authenticateStrangerUser(String usernameText, String passwordText, String displayText);
	public String deleteFriendRequest(String friendUsername);
	
	public void refreshStrangerList();
	public void refreshFriendList();
}
