package com.cqupt.control;

import java.util.ArrayList;

import android.os.Handler;

import com.cqupt.net.WebServiceThread;

public class Controller {
	private Handler handler;

	public Controller(Handler handler) {
		
		this.handler = handler;
	}

	public void loginCheck(String id, String password) {
		
		ArrayList<String> params = new ArrayList<String>();
		params.add("id");
		params.add("password");
		ArrayList<String> values = new ArrayList<String>();
		values.add(id);
		values.add(password);
		new WebServiceThread(handler, "loginCheck", params, values).start();
	}

}
