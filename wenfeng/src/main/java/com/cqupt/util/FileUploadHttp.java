
package com.cqupt.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class FileUploadHttp {
	
	private static final String PREFIX = "--";
	private static final String BOUNDARY = java.util.UUID.randomUUID().toString();
	private static final String LINE_END = "\r\n";
	private static final String CHARSET = "utf-8";
	 
   /**
	* update the files based on http
	*@param url the url of server
	*@param files the files to upload
	*/
	public static boolean upload(String url,Map<String,File> files) throws IOException{
		
		URL uri = new URL(url);
		HttpURLConnection con = (HttpURLConnection)uri.openConnection();
		con.setReadTimeout(5 * 1000);    
        con.setDoInput(true);//  allow input
        con.setDoOutput(true);// allow output
        con.setUseCaches(false);    
        con.setRequestMethod("POST");
        con.setRequestProperty("connection", "keep-alive");    
        con.setRequestProperty("Charsert", CHARSET);    
        con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
        // uoload multi-files
        DataOutputStream  outStream = new DataOutputStream(con.getOutputStream());
        if(files != null){
        	for(Map.Entry<String, File> entry : files.entrySet()){
                StringBuilder sb = new StringBuilder();
                sb.append(PREFIX+BOUNDARY+LINE_END);
                sb.append("Content-Disposition: form-data; name=\"file\"; filename=\""    
                        + entry.getKey() + "\"" + LINE_END);    
                sb.append("Content-Type: multipart/form-data; charset="    
                        + CHARSET + LINE_END);    
                sb.append(LINE_END);    
                outStream.write(sb.toString().getBytes());    
                InputStream inStream = new FileInputStream(entry.getValue());    
                byte[] buffer = new byte[1024];    
                int len = 0;    
                while ((len = inStream.read(buffer)) != -1) {    
                    outStream.write(buffer, 0, len);    
                }    
                inStream.close();    
                outStream.write(LINE_END.getBytes());    
        	}
        }
        // the end of request
        outStream.write((PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes());    
        outStream.flush(); 
        // get the answer code
        boolean success = con.getResponseCode()==200;
        outStream.close();    
        con.disconnect();    
        return success;
	}
}
