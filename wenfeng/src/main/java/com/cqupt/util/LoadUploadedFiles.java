package com.cqupt.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.ksoap2.serialization.SoapObject;

import android.os.Environment;

import com.cqupt.db.DBManager;
import com.cqupt.model.Attachment;
import com.cqupt.model.UploadFiles;
import com.cqupt.net.WebService;
import com.cqupt.ui.common.FileExploreActivity.MyFile;

/**
* upload files
*/

public class LoadUploadedFiles {
	
	public static void loadFiles(ArrayList<File> files,DBManager db,int taskID
			,String userID,int userType){
		
		ArrayList<UploadFiles> uploadFiles = db.getUploadFile(userID,taskID);// the files upload from local
		
		// upload file from web
		ArrayList<Attachment> attachments = new ArrayList<Attachment>();
		
		// get files from database
		if(userType == 3){
			attachments = db.getAttachment(taskID,userID);
		}else{// teachers get files from web
			WebService web = new WebService();
			// get the list of assignments
			HashMap<String,String> map = new HashMap<String,String>();
			map.put("id", String.valueOf(taskID));
			map.put("type",String.valueOf(2));
			map.put("userID","");
			
			SoapObject result = web.CallWebService("getAttachment", map);
			
			if(result != null){
				attachments = XMLParser.parseAttachment(result);
			}

		}
		
		Iterator<Attachment> ait = attachments.iterator();
		Iterator<UploadFiles> fit = uploadFiles.iterator();
		
		// load the files from database first
		while(fit.hasNext()){
			String fileNewName = fit.next().getFileNewName();
			while(ait.hasNext()){
				String fileName = ait.next().getNewName();
				if(fileNewName.equals(fileName)){
					ait.remove();
					ait = attachments.iterator();
					break;
				}
			}
		}
		
		for(UploadFiles f : uploadFiles){
			MyFile mFile = new MyFile(f.getFilePath());
			mFile.setNewName(f.getFileNewName());
			files.add(mFile);	
		}
		
		for(Attachment a : attachments){
			MyFile mFile = new MyFile(Environment.getExternalStorageDirectory()
					.getPath() + "/wenfeng/" + userID+ "/download/" + a.getOriginName());
			mFile.setNewName(a.getNewName());
			files.add(mFile);
		}
		
	}

}
