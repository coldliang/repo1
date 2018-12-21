package com.cqupt.ui.teacher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ksoap2.serialization.SoapObject;

import android.os.Bundle;
import android.os.StrictMode;

import com.cqupt.model.Test;
import com.cqupt.model.TestItemOption;
import com.cqupt.net.WebService;
import com.cqupt.ui.common.IDoneTestActivity;
import com.cqupt.util.XMLParser;

public class TeacherDoneTestActivity extends IDoneTestActivity {
	
	private WebService web = new WebService();
	private String studentUserID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		.permitNetwork().build());
		
		studentUserID = getIntent().getStringExtra("studentUserID");
		super.onCreate(savedInstanceState);
		
	}

	@Override
	protected String getAnswer(int itemID) {
		HashMap<String, String> p = new HashMap<String, String>();
		p.put("testItemID", String.valueOf(itemID));
		p.put("taskID", String.valueOf(ID));
		p.put("userID",studentUserID);
		
		SoapObject result = web.CallWebService("downloadMyChoice", p);
		
		if(result == null){
			return "";
		}
		
		HashMap<String, String> answer = XMLParser.parseAnswers(result);
		
		return answer == null ? "" : answer.get("answer") ;
	}

	@Override
	protected ArrayList<Test> getTestList() {
		ArrayList<Test> testList = null;
		HashMap<String, String> p = new HashMap<String, String>();
		p.put("taskID", String.valueOf(ID));
		
		SoapObject result = web.CallWebService("getTestList", p);
		
		if(result != null){
			testList = XMLParser.parseTestList(result);
		}
		
		return testList == null ? new ArrayList<Test>() : testList;
	}

	@Override
	protected int getItemType(int itemID) {
		HashMap<String, String> p = new HashMap<String, String>();
		p.put("itemID", String.valueOf(itemID));
		
		SoapObject result = web.CallWebService("getTestItemType", p);
		
		if(result == null){
			return -1;
		}
		
		return XMLParser.parseInt(result);
	}

	@Override
	protected List<TestItemOption> getTestItemOptions(int itemID) {
		
		List<TestItemOption> list = new ArrayList<TestItemOption>();
		HashMap<String, String> p = new HashMap<String, String>();
		p.put("testItemID", String.valueOf(itemID));
		
		SoapObject result = web.CallWebService("getTestItemOption", p);
		
		if(result != null){
			list = XMLParser.parseTestItemOption(result);
		}
		
		return list;
	}
	
	@Override
	protected boolean isTestFinished(int id, int testID, String userID) {
		HashMap<String, String> p = new HashMap<String, String>();
		p.put("taskID", String.valueOf(ID));
		p.put("testID", String.valueOf(testID));
		p.put("userID", userID);
		
		SoapObject result = web.CallWebService("isTestFinished", p);
		
		if(result != null){
			return XMLParser.parseBoolean(result).equalsIgnoreCase("true") ? true : false;
		}
		
		return false;
	}

}
