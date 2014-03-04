package com.messenger.chat;

public class MessageBubble {
	public String message;
	public String message_from;
	public String message_to;
	public String time;
	public boolean left;
	
	public MessageBubble(boolean left, String message, String message_from,
			String message_to,String time) {
		super();
		this.left=left;
		this.message = message;
		this.message_from = message_from;
		this.message_to = message_to;
		this.time = time;
	}
}