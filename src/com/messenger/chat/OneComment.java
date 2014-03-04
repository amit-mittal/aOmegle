package com.messenger.chat;

public class OneComment {
	public boolean left;
	public String comment;
	
	public long id;
	public String message;
	public String from;
	public String to;
	public long time;

	public OneComment(boolean left, String comment) {
		super();
		this.left = left;
		this.comment = comment;
	}

}