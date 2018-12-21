package com.cqupt.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import org.ksoap2.serialization.SoapObject;

import android.graphics.Bitmap;
import android.util.Base64;

import com.cqupt.net.WebService;


public class FileUploadWebservice {
	
	public static boolean upload(int type,File file,int id,String userID,String fileNewName){
		
		WebService web = new WebService();
		HashMap<String,String> map = new HashMap<String, String>();
		FileInputStream in = null;
		boolean flag = true;
		try {
			in = new FileInputStream(file);
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int count = -1;
			while((count = in.read(buffer))!= -1){
				baos.write(buffer, 0, count);  
			}
			String fileInByte = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
			fileInByte.length();
			map.put("type", String.valueOf(type));
			map.put("fileName",file.getName());
			map.put("file",fileInByte);
			map.put("id",String.valueOf(id));
			map.put("userID",userID);
			map.put("fileNewName", fileNewName);
			
			SoapObject result = web.CallWebService("uploadFiles", map);
			// connect to server timeout
			if(result == null){
				return false;
			}
			// fail to upload files
			if(XMLParser.parseBoolean(result).equalsIgnoreCase("false")){
				return false;
			}
		} catch (FileNotFoundException e) {
			flag = false;
			e.printStackTrace();
		} catch (IOException e) {
			flag = false;
			e.printStackTrace();
		}finally{
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return flag;
	}
	
	public static boolean upload(int type,Bitmap bitmap,String fileName
			,int id,String userID,String fileNewName){
		
		WebService web = new WebService();
		HashMap<String,String> map = new HashMap<String, String>();
		ByteArrayOutputStream baos = BitmapUtil.compressImage(bitmap);
		String bitmapInByte = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
		
		map.put("type", String.valueOf(type));
		map.put("fileName",fileName);
		map.put("file",bitmapInByte);
		map.put("id",String.valueOf(id));
		map.put("userID",userID);
		map.put("fileNewName", fileNewName);
		
		SoapObject result = web.CallWebService("uploadFiles", map);
		// timeout
		if(result == null){
			return false;
		}
		// upload failed
		if(XMLParser.parseBoolean(result).equalsIgnoreCase("false")){
			return false;
		}
		return true;
	}

}
