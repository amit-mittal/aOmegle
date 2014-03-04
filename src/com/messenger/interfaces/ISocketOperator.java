package com.messenger.interfaces;


public interface ISocketOperator {
	
	public String sendHttpRequest(String params);
	public boolean sendMessage(String message, String ip, int port);
	public boolean sendStrangerMessage(String message, String ip, int port);
	public int startListening(int port);
	public void stopListening();
	public void exit();
	public int getListeningPort();

}
