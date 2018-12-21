package com.cqupt.net;

import java.util.HashMap;
import java.util.List;

import org.ksoap2.serialization.SoapObject;

import com.cqupt.model.FeedBack;
import com.cqupt.model.ScoreStudentItem;
import com.cqupt.util.XMLParser;

public class WebServiceOperation {
	
	public static FeedBack getFeedBack(String userID,int exerciseID){
		
		WebService web = new WebService();
		HashMap<String, String> p = new HashMap<String, String>();
		
		p.put("taskID", String.valueOf(exerciseID));
		p.put("userID", userID);
		
		SoapObject result = web.CallWebService("getFeedback", p);
		
		if(result == null){
			return null;
		}
		
		return XMLParser.parseFeedBack(result); 
			
	}
	
	public static boolean makeFeedback(String fromUserID,String toUserID ,int exerciseID
			,String score ,String content){
		WebService web = new WebService();
		HashMap<String, String> p = new HashMap<String, String>();
		
		p.put("content", content);
		p.put("fromUserID", fromUserID);
		p.put("toUserID", toUserID);
		p.put("exerciseID", String.valueOf(exerciseID));
		
		SoapObject result = web.CallWebService("makeFeedback", p);
		
		if(result == null){
			return false;
		}else{
			return manualScore(toUserID, exerciseID, score);
		}
	}
	
	public static boolean manualScore(String userID,int exerciseID,String score){
		
		WebService web = new WebService();
		HashMap<String, String> p = new HashMap<String, String>();
		p.put("taskID", String.valueOf(exerciseID));
		p.put("userID", userID);
		p.put("score", score);
		
		SoapObject result = web.CallWebService("manualScore", p);
		
		if(result == null){
			return false;
		}
		
		String r =  XMLParser.parseBoolean(result);
		
		if(r == null){
			return false;
		}
		
		return r.equals("true") ? true : false;
		
	}
	
	public static List<ScoreStudentItem> getScoreStuItemByPage(int exerciseID,String scoreType,String fileType
			,int lineSize,int currentPage){
		
		WebService web = new WebService();
		HashMap<String,String> p = new HashMap<String, String>();
		p.put("exerciseID",String.valueOf(exerciseID));
		p.put("scoreType", scoreType);
		p.put("fileType", fileType);
		p.put("lineSize", String.valueOf(lineSize));
		p.put("currentPage", String.valueOf(currentPage));
		
		SoapObject result = web.CallWebService("getScoreStudentListByPage", p);
		
		if(result == null){
			return null;
		}
		
		return XMLParser.parseScoreStudentItem(result);
		
	}
	
	public static int getScoreStuItemsCount(int exerciseID,String fileType,String scoreType){
		
		WebService web = new WebService();
		HashMap<String, String> p = new HashMap<String, String>();
		p.put("exerciseID", String.valueOf(exerciseID));
		p.put("scoreType", scoreType);
		p.put("fileType", fileType);
		
		SoapObject result = web.CallWebService("getScoreStudentsCount", p);
		
		if(result == null){
			return -1;
		}
		
		return XMLParser.parseInt(result);
		
	}
	
	public static boolean checkTaskScored(int exerciseID) throws Exception{
		
		WebService web = new WebService();
		HashMap<String, String> p = new HashMap<String, String>();
		p.put("exerciseID", String.valueOf(exerciseID));
		
		SoapObject result = web.CallWebService("checkTaskScored", p);
		
		if(result == null){
			throw new Exception("Connection fail");
		}
		
		return XMLParser.parseBoolean(result).equalsIgnoreCase("true") ? true : false;
		
	}


}
