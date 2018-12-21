package com.cqupt.application;

import android.app.Application;

public class MyApplication extends Application {
	
	private String userID;

	@Override
	public void onCreate() {
		
		super.onCreate();
		
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}
	
	

}
