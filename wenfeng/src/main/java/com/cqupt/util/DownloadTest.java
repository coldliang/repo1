package com.cqupt.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ksoap2.serialization.SoapObject;

import com.cqupt.db.DBManager;
import com.cqupt.model.Attachment;
import com.cqupt.model.Test;
import com.cqupt.model.TestItem;
import com.cqupt.model.TestItemOption;
import com.cqupt.net.WebService;

public class DownloadTest {
	
	public final static int FAIL = -1;
	public final static int SUCCESS = 0;
	public final static int IMPORT_TEST = 1;
	public final static int DOWNLOAD_TEST_ITEM = 2;
	public final static int IMPORT_TEST_ITEM = 3;
	public final static int DOWNLOAD_OPTION = 4;
	public final static int IMPORT_OPTION = 5;
	
	public interface OnProcessChangeListener{
		public void onProcessChange(int process);
	}
	
	public interface OnAddArrangeRelation{
		public void addArrangeRelation(int testID);
	}
	
	private WebService mWeb = new WebService();
	private OnProcessChangeListener mListener;
	private OnAddArrangeRelation mRelation;
	private DBManager mDb;
	
	public DownloadTest(DBManager db,OnAddArrangeRelation a){
		mDb = db;
		mRelation = a;
	}
	
	public DownloadTest(DBManager db){
		mDb = db;
	}
	
	public void setListener(OnProcessChangeListener listener){
		mListener = listener;
	}
	
	public int downloadTest(final List<Test> testList){
		
		if (testList != null) {
			
			// the img downloaded in background
			new Thread(new Runnable() {
				public void run() {
					DownloadMultiMedia.downloadInnerTextImage(testList);
				}
			}).start();

			// import the title
			if(mListener != null){
				mListener.onProcessChange(IMPORT_TEST);
			}
			
			mDb.addTestLibrary(testList);
			
			// update the source type
			DownloadResourceType.download(mDb);
			
			for (Test test : testList) {
				
				if(downloadAttachment(test.getTestID()) == FAIL){
					return FAIL;
				}

				if(mRelation != null){
					// add the tables related to title
					mRelation.addArrangeRelation(test.getTestID());
				}	
				
				// update the content
				if (downloadItem(test.getTestID()) == FAIL) {
					return FAIL;
				}
			}
		}
		
		return SUCCESS;
	 }
	
	private int downloadAttachment(int testID){
		// get the files list
		HashMap<String, String>map = new HashMap<String, String>();
		map.put("id", String.valueOf(testID));
		map.put("type", String.valueOf(1));
		map.put("userID", "");
		SoapObject result = mWeb.CallWebService("getAttachment", map);

		if (result == null) {
			return FAIL;
		}

		ArrayList<Attachment> attachmentList = XMLParser.parseAttachment(result);
		mDb.addAttachment(attachmentList);
		
		return SUCCESS;
	}
	
	private int downloadItem(int testID){
		// get the detail of item
		if(mListener != null){
			mListener.onProcessChange(DOWNLOAD_TEST_ITEM);
		}
		
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("testID", String.valueOf(testID));
		SoapObject result = mWeb.CallWebService("getTestItem", map);
		
		if (result == null) {
			return FAIL;
		}
		
		ArrayList<TestItem> testItemList = XMLParser.parseTestItem(result);

		if (testItemList != null) {
			
			if(mListener != null){
				mListener.onProcessChange(IMPORT_TEST_ITEM);
			}
			
			mDb.addTestItem(testItemList);// add item
			for (TestItem items : testItemList) {
				
				if (downloadOption(items.getTestItemID()) == FAIL) {
					return FAIL;
				}
				
			}
		}
		
		return SUCCESS;
	}
	
	private int downloadOption(int itemID){
		
		if(mListener != null){
			mListener.onProcessChange(DOWNLOAD_OPTION);
		}
		
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("testItemID", String.valueOf(itemID));
		SoapObject result = mWeb.CallWebService("getTestItemOption", map);
		
		if (result == null) {
			return FAIL;
		}
		
		ArrayList<TestItemOption> testItemOptionList = XMLParser.parseTestItemOption(result);
		
		if (testItemOptionList != null) {
			
			if(mListener != null){
				mListener.onProcessChange(IMPORT_OPTION);
			}
			
			mDb.addTestOption(testItemOptionList);
		}
		
		return SUCCESS;
	}
}


