package com.mycompany.hosted.exception_handler;

@SuppressWarnings("serial")
public class MvcNavigationException extends Exception {
	
	private String friendly;

	public MvcNavigationException(String message) {
		
		super(message);
	}

	public String getFriendly() {
		return friendly;
	}

	public void setFriendly(String friendly) {
		this.friendly = friendly;
	}
	
	
	
}
