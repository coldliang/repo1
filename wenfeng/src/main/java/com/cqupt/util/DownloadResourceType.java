package com.cqupt.util;

import org.ksoap2.serialization.SoapObject;

import com.cqupt.db.DBManager;
import com.cqupt.model.TestSubType;
import com.cqupt.model.TestType;
import com.cqupt.net.WebService;

/**
 * 
 * @author Administrator
 * download the TestTopTypt and TestType
 *
 */
public class DownloadResourceType {
	
	public static boolean download(DBManager db){
		
		WebService web = new WebService();
		
		SoapObject result = web.CallWebService("getTestType", null);
		
		if(result == null){
			return false;
		}
		
		// add testTopType
		for(TestType tp : XMLParser.parseTestType(result)){
			db.addTestTopType(tp);
		}
		
		// add testType
		result = web.CallWebService("getTestSubTypeForLocal", null);
		
		if(result == null){
			return false;
		}
		
		for(TestSubType tst : XMLParser.parseTestSubType(result)){
			db.addTestType(tst);
		} 
		
		return true;
		
	}

}
