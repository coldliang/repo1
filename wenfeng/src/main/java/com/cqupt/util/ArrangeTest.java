package com.cqupt.util;

import java.util.HashMap;
import java.util.List;

import org.ksoap2.serialization.SoapObject;

import com.cqupt.net.WebService;

public class ArrangeTest {
	public static final int SUCCESS = 1;
	public static final int FAIL = -1;
	
	public static int add(int taskID,List<Integer> testList){
		
		WebService web = new WebService();
		HashMap<String,String> p = new HashMap<String, String>();
		p.put("exerciseID",String.valueOf(taskID));
		p.put("testIDs",testList.toString().replace("[","")
				.replace("]","").replace(" ",""));
		
		SoapObject result = web.CallWebService("arrangeTest",p);
		
		if(result == null){
			return FAIL;
		}
		
		return XMLParser.parseBoolean(result).equalsIgnoreCase("true") ? SUCCESS : FAIL;
		
	}

}
