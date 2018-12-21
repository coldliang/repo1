package com.cqupt.util.pageModel;

import java.util.ArrayList;
import java.util.HashMap;

import org.ksoap2.serialization.SoapObject;

import com.cqupt.model.Comment;
import com.cqupt.net.WebService;
import com.cqupt.util.XMLParser;

public class CommentListPageModel {
	
	public static final int CONNECTION_FAIL = -1;

	private int id;//job of title id
	private int currentPage = 1;
	private int lineSize = 10;
	private int type = 0;// 0:the list of title comment, 1:the list of assignment comment
    private int lastItemIndex = 0;// the index of last item
	private WebService web = new WebService();
	public CommentListPageModel(int type ,int id,int currentPage,int lineSize){
		
		this.type = type;
		this.id = id;
		this.currentPage = currentPage;
		this.lineSize = lineSize;
	}
	public int getSumPages(){
		
		if(getCommentCount() == CONNECTION_FAIL){
			return CONNECTION_FAIL;
		}else{
			return (getCommentCount()+lineSize-1)/lineSize;
		}
		
	}
	public int getCommentCount(){
		
		int count = 0;
		HashMap<String,String> p = new HashMap<String, String>();
		p.put("id", String.valueOf(id));
		SoapObject result;
		if(type == 0){
			result = web.CallWebService("getTestCommentCount", p);
		}else{
			result = web.CallWebService("getTaskCommentCount", p);
		}
		if(result == null){
			return CONNECTION_FAIL;
		}
		count = XMLParser.parseCommentCount(result);
		return count;
	}
	public ArrayList<Comment> getCommentList(){
		ArrayList<Comment> list = new ArrayList<Comment>();
		HashMap<String,String> p = new HashMap<String, String>();
		p.put("id", String.valueOf(id));
		p.put("currentPage", String.valueOf(currentPage));
		p.put("lineSize", String.valueOf(lineSize));
		SoapObject result;
		if(type == 0){
			result = web.CallWebService("getTestCommentList", p);
		}else{
			result = web.CallWebService("getTaskCommentList", p);
		}
		if(result == null){
			return list;
		}
		list = XMLParser.parseCommentList(result);
		return list;
	}
	public ArrayList<Comment> nextPage(){
		currentPage++;
		return getCommentList();
	}
	public int getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	public int getLastItemIndex() {
		return lastItemIndex;
	}
	public void setLastItemIndex(int lastItemIndex) {
		this.lastItemIndex = lastItemIndex;
	}
	
}
