package com.messenger.database;

public class Message {
	  private long id;
	  private String message_from;
	  private String message_to;
	  private String message;
	  private String time;

	  	public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	
		public String getMessage_from() {
			return message_from;
		}

		public void setMessage_from(String message_from) {
			this.message_from = message_from;
		}

		public String getMessage_to() {
			return message_to;
		}

		public void setMessage_to(String message_to) {
			this.message_to = message_to;
		}

		public String getTime() {
			return time;
		}
	
		public void setTime(String time) {
			this.time = time;
		}
	
		public String toString() {
		   return message;
		}
}
