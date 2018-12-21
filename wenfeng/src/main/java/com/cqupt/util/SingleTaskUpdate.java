
package com.cqupt.util;

import java.util.ArrayList;
import java.util.HashMap;

import org.ksoap2.serialization.SoapObject;

import com.cqupt.db.DBManager;
import com.cqupt.model.Attachment;
import com.cqupt.model.FeedBack;
import com.cqupt.model.Task;
import com.cqupt.model.Test;
import com.cqupt.model.TestItem;
import com.cqupt.model.TestItemOption;
import com.cqupt.net.WebService;

/**
 * This class is primarily used by students to update a single task, that is, to synchronize web side tasks
 */
public class SingleTaskUpdate {
	/**
	 *Monitor the progress of background tasks
	 */
	 public static interface OnProcessChangeListener{
		 
		public void onProcessChange(int process);
	 }
	  
	 public static final int CONNECTION_FAIL = 0;//connection to server timed out
	 public static final int UPDATE_SUCCESS = 1;//update successfully
	 public static final int IMPORT_TASK = 2;//Import task
	 public static final int DOWNLOAD_TEST = 3;//Download homework
	 public static final int IMPORT_TEST = 4;//The import operation
	 public static final int DOWNLOAD_ITEM = 5;//Download the topic dry
	 public static final int IMPORT_ITEM = 6;//The import problem of dry
	 public static final int DOWNLOAD_OPTION = 7;//Download Options
	 public static final int IMPORT_OPTION = 8;//Import Options
	 
	 private OnProcessChangeListener listener;
	 private DBManager db;
	 private WebService webService;
	 private int taskID;
	 private String userID;
	 
	 private  ArrayList<Test> testList = null;//web port test list
	 private  ArrayList<Integer> testIDs = null;//local test list
	 private  ArrayList<Attachment> attachmentList = null;//file list in web port
	 private  ArrayList<Attachment> attachments = null;//local attachment list
	 private  ArrayList<TestItem> testItemList = null;//web item list
	 private  ArrayList<Integer> testItemIDs = null;//local item list
	 private  ArrayList<TestItemOption> testItemOptionList = null;//web option list
	 private  ArrayList<Integer> testItemOptionIDs = null;//local option list
	 
	 public SingleTaskUpdate(DBManager db,OnProcessChangeListener listener,String userID){
		 
		 this.listener = listener;
		 this.db = db;
		 webService = new WebService();
		 this.userID = userID;
	 }
	 
	 public int updateTask(Task task){
		 
		 listener.onProcessChange(IMPORT_TASK);
		 db.addTask(task);
		 taskID = task.getTaskID();
		 
		 // get the task file list
		 HashMap<String,String> map = new HashMap<String,String>();
		 map.put("id", String.valueOf(task.getTaskID()));
		 map.put("type",String.valueOf(2));
		 map.put("userID","");
		 SoapObject result = webService.CallWebService("getAttachment",map);
		 
		 if(result == null){
			 return CONNECTION_FAIL;
		 }
		 
		 attachmentList = XMLParser.parseAttachment(result);//web file list
		 //db.addAttachment(attachmentList);
		 attachments = db.getAttachment(taskID);//local file list
	     // delete different file between local and server
	     if(attachmentList.size() == 0){
	    	 db.deleteAttachment(taskID);
		 }else{
			 attachments.removeAll(attachmentList);
			 
			 for(Attachment a : attachments){
				 db.deleteAttachment(taskID, a.getNewName());
			 }
			 
			 db.addAttachment(attachmentList);
          }
	     
	     
	     
	     // get the file list uploaded
		 map = new HashMap<String,String>();
		 map.put("id", String.valueOf(task.getTaskID()));
		 map.put("type",String.valueOf(3));
		 map.put("userID", userID);
		 result = webService.CallWebService("getAttachment",map);
		 
		 if(result == null){
			 return CONNECTION_FAIL;
		 }
		 
		 attachmentList = XMLParser.parseAttachment(result);//web file list
		
		 
		 for(Attachment a : attachmentList){
			 a.setUserID(userID);
		 }
		 
		 if(attachmentList.size() == 0){
			 db.deleteAttachment(userID,taskID);
			 db.deleteUploadFiles(userID, taskID);
		 }else{
			 //db.addAttachment(attachmentList);
			 attachments = db.getAttachment(taskID,userID);// local file list
			 
		     //delete different file between local and server
			 attachments.removeAll(attachmentList);
			 
			 for(Attachment a : attachments){
				 db.deleteAttachment(taskID, userID,a.getNewName());
				 db.deleteUploadFiles(a.getNewName());
			 }
			 
			 db.addAttachment(attachmentList);
		 } 
		 
		 //Update feedback content
		 map = new HashMap<String, String>();
		 map.put("taskID", String.valueOf(taskID));
		 map.put("userID", userID);
		 
		 result = webService.CallWebService("getFeedback", map);
		 
		 if(result != null){
			 FeedBack feedback = XMLParser.parseFeedBack(result);
			 feedback.setTaskID(taskID);
			 db.addFeedBack(userID, feedback);
		 }

		 return updateTest(taskID);
	 }
	 
	 private int updateTest(int taskID){
		 //Get a list of topics
		 listener.onProcessChange(DOWNLOAD_TEST);
		 HashMap<String,String> map = new HashMap<String,String>();
		 map.put("taskID", String.valueOf(taskID));
		 SoapObject result = webService.CallWebService("getTestList", map);
		 if(result == null){
			 return CONNECTION_FAIL;
		 }
		 testList = XMLParser.parseTestList(result);//web port test list
		 testIDs = db.getTestListIDs(taskID);//local test list
		 
		 if(testList != null){
			 listener.onProcessChange(IMPORT_TEST);
			 
			 //Download the picture in the background
			 new Thread(new Runnable(){
				 public void run(){
					 DownloadMultiMedia.downloadInnerTextImage(testList);
				 }
			 }).start();
			 
			 db.addTestLibrary(testList);
			 for(Test test:testList){
				 
				 //Get a list of job attachments
				 map = new HashMap<String,String>();
				 map.put("id", String.valueOf(test.getTestID()));
				 map.put("type",String.valueOf(1));
				 map.put("userID", "");
				 result = webService.CallWebService("getAttachment",map);
				 
				 if(result == null){
					 return CONNECTION_FAIL;
				 }
				 
				 attachmentList = XMLParser.parseAttachment(result);//web file list
				 //db.addAttachment(attachmentList);
				 attachments = db.getAttachment(test.getTestID());//local file list
				 
			     //Remove local and server different job attachments
			     if(attachmentList.size() == 0){
			    	 db.deleteAttachment(test.getTestID());
				 }else{
					 attachments.removeAll(attachmentList);
					 
					 for(Attachment a : attachments){
						 db.deleteAttachment(test.getTestID(), a.getNewName());
					 }
					 
					 db.addAttachment(attachmentList);
		          }
			     
				 testIDs.remove((Object)test.getTestID());
				 
				 //Add the corresponding table of homework questions
				 db.addTaskTestArrange(taskID, test.getTestID());
				 
				 //Update the topic dry
				 if(updateItem(test.getTestID()) == CONNECTION_FAIL){
					 return CONNECTION_FAIL;
				 }
			 }
		 }
		 
		 //Delete the local and server different task job schedules and the corresponding answers for the jobs (always be consistent with the server)
		 for(int id : testIDs){
			 db.deleteTaskTestArrange(taskID,id);
			 ArrayList<Integer> itemIDs = db.getItemListIDs(id);
			 for(int itemID : itemIDs){
				 db.deleteMyChoice(userID,taskID, itemID);
			 }
		 }
		 return UPDATE_SUCCESS;
	 }
	 
	 private int updateItem(int testID){
		 //For more details
         listener.onProcessChange(DOWNLOAD_ITEM);
		 HashMap<String,String> map = new HashMap<String,String>();
		 map.put("testID", String.valueOf(testID));
		 SoapObject result = webService.CallWebService("getTestItem", map);
		 if(result == null){
			 return CONNECTION_FAIL;
		 }
		 testItemList = XMLParser.parseTestItem(result);
		 testItemIDs = db.getItemListIDs(testID);
		 
		 if(testItemList != null){
			 listener.onProcessChange(IMPORT_ITEM);
			 db.addTestItem(testItemList);//Add the topic dry
			 for(TestItem items:testItemList){
				 testItemIDs.remove((Object)items.getTestItemID());
				 if(updateOption(items.getTestItemID()) == CONNECTION_FAIL){
					 return CONNECTION_FAIL;
				 }
			 } 
		 }
		 //Remove local and server testItem(always match the server)
		 for(int id : testItemIDs){
			 db.deleteItem(testID,id);
			 db.deleteMyChoice(userID,taskID,id);
		 }
		 return UPDATE_SUCCESS;
	 }
	 
	 private int updateOption(int itemID){
		 listener.onProcessChange(DOWNLOAD_OPTION);
		 HashMap<String,String> map = new HashMap<String,String>() ;
		 map.put("testItemID", String.valueOf(itemID));
		 SoapObject result = webService.CallWebService("getTestItemOption", map);
		 if(result == null){
			 return CONNECTION_FAIL;
		 }
		 testItemOptionList = XMLParser.parseTestItemOption(result);
		 testItemOptionIDs = db.getOptionListIDs(itemID);
		 if(testItemOptionList != null){
			 listener.onProcessChange(IMPORT_OPTION);
			 db.addTestOption(testItemOptionList);
			 for(TestItemOption options : testItemOptionList){
				 testItemOptionIDs.remove((Object)options.getTestItemOptionID());
			 }
		 }
		 //
		 for(int id : testItemOptionIDs){
			 db.deleteOption(itemID,id);
		 }
		 return UPDATE_SUCCESS;
	 }
}
