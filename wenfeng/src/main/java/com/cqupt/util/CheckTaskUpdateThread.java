package com.cqupt.util;

import java.util.ArrayList;
import java.util.HashMap;

import org.ksoap2.serialization.SoapObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;
import com.cqupt.net.WebService;

public class CheckTaskUpdateThread extends Thread{
	
	private MySQLiteOpenHelper helper;
	private DBManager db;
	private WebService web;
	
	private Context context;
	private String userID;
	
	public CheckTaskUpdateThread(Context context,String userID){
		
		helper = MySQLiteOpenHelper.getInstance(context);
		db = new DBManager(helper.getConnection());
		web = new WebService();
		
		this.context = context;
		this.userID = userID;
		
	}
	
	public void run(){

		ArrayList<Integer> taskIDs = db.getTaskListIDs(userID,3);
		String id = taskIDs.toString().replace("[","").replace("]","")
				.replace(" ","").trim();
		
		HashMap<String,String> p = new HashMap<String, String>();
		p.put("userID",userID);
		p.put("taskIDs",id);
		
		SoapObject result = web.CallWebService("checkTaskUpdate",p);
		
		if(result != null){
			int newTaskCount = XMLParser.parseInt(result);
			
			SharedPreferences sp = context.getSharedPreferences("my_prefer"
					,Activity.MODE_PRIVATE);
				
			SharedPreferences.Editor editor = sp.edit();
			editor.putInt("newTaskCount",newTaskCount);
			editor.commit();
		}		
	}
}
