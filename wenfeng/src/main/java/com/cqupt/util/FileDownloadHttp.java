package com.cqupt.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FileDownloadHttp {
	
	public static void download(String uri,String fileName,String fileSavePath){
		
		InputStream in = null;
		OutputStream out = null;
		
		try {
			URL url = new URL(uri);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			
			con.setConnectTimeout(3000);
			con.setRequestMethod("GET");
			con.connect();
			
			if(con.getResponseCode() == 200){
				
				File file = new File(fileSavePath + "/" + fileName);
				
				if(!file.exists()){
					
					if(!file.getParentFile().exists()){
						file.getParentFile().mkdirs();
					}
					
					file.createNewFile();
				}
				
				in = con.getInputStream();
				out = new FileOutputStream(file);
				
				byte[] buffer = new byte[1024];
				int size = -1;
				
				while((size = in.read(buffer)) != -1){
					out.write(buffer,0,size);
				}
			}
			con.disconnect();
		}  catch (MalformedURLException e) {
			 e.printStackTrace();
		}  catch (IOException e) {
			 e.printStackTrace();
		}finally{
			
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			}
			
			if(out != null){
				try {
					out.close();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			}
			
		}
		
	}

}
