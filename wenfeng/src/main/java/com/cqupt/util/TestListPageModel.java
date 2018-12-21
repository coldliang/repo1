package com.cqupt.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ksoap2.serialization.SoapObject;

import com.cqupt.model.TestDetailForArrange;
import com.cqupt.util.pageModel.PageModel;

public class TestListPageModel extends PageModel<TestDetailForArrange> {
	
	private String userID;
	private String title;
	private String testType;
	private String myList;
	private String exerciseID;
	
	public TestListPageModel(String userID,String title,String testType
			,String myList,String exerciseID){
		this.userID = userID;
		this.title = title;
		this.testType = testType;
		this.myList = myList;
		this.exerciseID = exerciseID;
	}
	
	public TestListPageModel(String userID,String title,String testType,String myList,
			int currentPage,int pageSize,String exerciseID){
		this(userID, title, testType, myList,exerciseID);
		this.currentPage = currentPage;
		this.lineSize = pageSize;
	}

	@Override
	public List<TestDetailForArrange> getDataList() {
		List<TestDetailForArrange> list = new ArrayList<TestDetailForArrange>();
		HashMap<String, String> p = new HashMap<String, String>();
		p.put("title", title);
		p.put("testType", testType);
		p.put("myList", myList);
		p.put("userID", userID);
		p.put("lineSize", String.valueOf(lineSize));
		p.put("currentPage", String.valueOf(currentPage));
		p.put("exerciseID", exerciseID);
		
		SoapObject result = web.CallWebService("getTestListForArrange", p);
		
		if(result != null){
			list = XMLParser.parseTestDetailForArrange(result);
		}
		
		return list;
	}

	@Override
	protected int getDataCount() {
		
		HashMap<String, String> p = new HashMap<String, String>();
		p.put("title", title);
		p.put("testType", testType);
		p.put("myList", myList);
		p.put("userID", userID);
		p.put("exerciseID", exerciseID);
		
		SoapObject result = web.CallWebService("getTestCountForArrange", p);
		
		if(result != null){
			sumCount = XMLParser.parseInt(result);
		}
		
		return sumCount;
	}

}
