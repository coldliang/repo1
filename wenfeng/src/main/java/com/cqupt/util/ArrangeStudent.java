package com.cqupt.util;

import com.cqupt.net.WebService;

import org.ksoap2.serialization.SoapObject;

import java.util.HashMap;
import java.util.List;

public class ArrangeStudent {
	
	public static final int SUCCESS = 1;
	public static final int FAIL = -1;
	
	public static int add(int taskID,List<String> studentList){
		
		WebService web = new WebService();
		HashMap<String,String> p = new HashMap<String, String>();
		p.put("taskID",String.valueOf(taskID));
		p.put("studentList",studentList.toString().replace("[","")
				.replace("]","").replace(" ",""));
		
		SoapObject result = web.CallWebService("arrangeStudent",p);
		
		if(result == null){
			return FAIL;
		}
		
		return XMLParser.parseBoolean(result).equalsIgnoreCase("true") ? SUCCESS : FAIL;
		
	}

}
