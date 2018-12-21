package com.cqupt.util.pageModel;

import java.util.HashMap;
import java.util.List;

import org.ksoap2.serialization.SoapObject;

import com.cqupt.model.Task;
import com.cqupt.util.XMLParser;

public class TeacherTaskListPageModel extends PageModel<Task>{
	
	private String userID;
	
	/**
	 * 1:going jobs, 2:finished jobs
	 */
	private int type = 1;
	
	/**
	 * 0 take a part 1 take the graded one 2 take the ungraded one
	 */
	private int selection = 0;
	
	/**
	 * use the default start page and the number of items show in every page
	 */
	public TeacherTaskListPageModel(String userID){
		this.userID = userID;
	}
	
	/**
	 * use the default start page and the number of items show in every page
	 */
	public TeacherTaskListPageModel(String userID,int currentPage,int pageSize){
		this.userID = userID;
		this.currentPage = currentPage;
		this.lineSize = pageSize;
	}
	

	@Override
	public List<Task> getDataList() {
		  	
    	HashMap<String,String> p = new HashMap<String, String>();
    	p.put("userID",userID);
    	p.put("type", String.valueOf(type));
    	p.put("currentPage",String.valueOf(currentPage));
    	p.put("lineSize", String.valueOf(lineSize));
    	p.put("selection", String.valueOf(selection));
    	
    	SoapObject result = web.CallWebService("getTeacherExerciseList", p);
    	
    	if(result == null){
    		
    		return null;
    	}
    	
    	return XMLParser.parseTeacherTaskList(result);
    	 	
	}

	@Override
	protected int getDataCount() {
		 	
    	HashMap<String,String> p = new HashMap<String, String>();
    	p.put("userID", userID);
    	p.put("type",String.valueOf(type));
    	p.put("selection", String.valueOf(selection));
    	
    	SoapObject result = web.CallWebService("getTeacherTaskCount",p);
    	
    	if(result == null){
    		return CONNECTION_FAIL;
    	}
    	
    	return XMLParser.parseInt(result);
    	
	}
	
	public void setType(int type){
		this.type = type;
	}
	
	public void setSelection(int selection){
		this.selection = selection;
	}

}
