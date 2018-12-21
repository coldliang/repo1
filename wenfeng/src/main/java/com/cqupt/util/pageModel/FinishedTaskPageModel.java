package com.cqupt.util.pageModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ksoap2.serialization.SoapObject;

import com.cqupt.model.Task;
import com.cqupt.util.XMLParser;

public class FinishedTaskPageModel extends PageModel<Task>{
	
	private String userID;

	
	public FinishedTaskPageModel(String userID,int currentPage,int lineSize){
		
		this.userID = userID;
		this.currentPage = currentPage;
		this.lineSize = lineSize;
		
	}
	
	// default page number and start page
	public FinishedTaskPageModel(String userID){
		
		this.userID = userID;

	}

	@Override
	public List<Task> getDataList() {
		
		List<Task> list = new ArrayList<Task>();
		
		HashMap<String,String> p = new HashMap<String, String>();
		p.put("userID", userID);
		p.put("lineSize", String.valueOf(lineSize));
		p.put("currentPage",String.valueOf(currentPage));
		
		SoapObject result = web.CallWebService("getStudentFinishedTaskList", p);
		
		if(result == null){
			return list;
		}
		
		return XMLParser.parseFinishedTaskList(result);
	}

	@Override
	protected int getDataCount() {
		
		HashMap<String,String> p = new HashMap<String, String>();
		p.put("userID", userID);
		SoapObject result = web.CallWebService("getFinishedTaskCount", p);
		
		if(result == null){
			return CONNECTION_FAIL;
		}
		
		
		return XMLParser.parseInt(result);
	}


}
