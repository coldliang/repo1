package com.cqupt.util.pageModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ksoap2.serialization.SoapObject;

import com.cqupt.model.UserInfo;
import com.cqupt.util.XMLParser;

public class StuListPageModel extends PageModel<UserInfo>{
	
	public static final int CONNECTION_FAIL = -1;

	private String keyword;
	private String dept;
	private String myList;
	private String userID;
	private String schoolName;	
    private int taskID;
	
	public StuListPageModel(String keyword,String dept,String myList,
			String userID,String schoolName,int currentPage,int lineSize,int taskID){
		
		this.keyword = keyword;
		this.dept = dept;
		this.myList = myList;
		this.userID = userID;
		this.schoolName = schoolName;
		
		this.currentPage = currentPage;
		this.lineSize = lineSize;
		this.taskID = taskID;
	}
	
	// default page number and start page
	public StuListPageModel(String keyword,String dept,String myList
			,String userID,String schoolName,int taskID){
		
		this.keyword = keyword;
		this.dept = dept;
		this.myList = myList;
		this.userID = userID;
		this.schoolName = schoolName;
		this.taskID = taskID;
	}

	@Override
	public List<UserInfo> getDataList() {
		ArrayList<UserInfo> list = new ArrayList<UserInfo>();
		HashMap<String,String> p = new HashMap<String, String>();
		p.put("keyword",keyword);
		p.put("dept",dept);
		p.put("myList",myList);
		p.put("userID", userID);
		p.put("schoolName", schoolName);		
		p.put("currentPage", String.valueOf(currentPage));
		p.put("lineSize", String.valueOf(lineSize));
		p.put("exerciseID", String.valueOf(taskID));
		SoapObject result = web.CallWebService("getStudentList", p);
		
		if(result == null){
			return list;
		}
		
		return XMLParser.ParseUserInfoList(result);
	}

	@Override
	protected int getDataCount() {
		HashMap<String,String> p = new HashMap<String, String>();
		p.put("keyword",keyword);
		p.put("dept",dept);
		p.put("myList",myList);
		p.put("userID", userID);
		p.put("schoolName", schoolName);
		p.put("exerciseID", String.valueOf(taskID));
		
		SoapObject result = web.CallWebService("getStudentCount",p);
		
		if(result == null){
			return CONNECTION_FAIL;
		}
		
		return sumCount = XMLParser.parseInt(result);
	}

	

}
