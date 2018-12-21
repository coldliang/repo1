package com.cqupt.thread;

import java.util.HashMap;

import com.cqupt.net.WebService;

public class MakeFileReadThread {
	
	public static void start(final String fileName){
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				WebService web = new WebService();
				HashMap<String, String> p = new HashMap<String, String>();
				p.put("fileName", fileName);
				
				web.CallWebService("makeFileRead", p);
				
			}
		}).start();
		
	}

}
