package com.cqupt.net;

import android.os.Handler;
import android.os.Message;

import com.cqupt.util.XMLParser;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WebServiceThread extends Thread {
	
	public static final String CONNECTION_FAIL = "0";
	
	WebService webService = new WebService();
	private Handler handler;
	ArrayList<String> params;
	ArrayList<String> values;

	public WebServiceThread(Handler handler, String method,
			ArrayList<String> params, ArrayList<String> values) {
		
		this.handler = handler;
		this.params = params;
		this.values = values;
	}

	@Override
	public void run() {

		// ��װ����
		Map<String, String> map;
		if (params == null) {
			map = null;
		} else {
			map = new HashMap<String, String>();
			for (int i = 0; i < params.size(); i++) {
				map.put(params.get(i), values.get(i));
			}
		}
		//���webservice�ķ���ֵ
		SoapObject s = webService.CallWebService("loginCheck", map);
		String result;
		if(s == null){
			result = CONNECTION_FAIL;
		}else{
			result = XMLParser.parseBoolean(s);
		}
		Message message = new Message();
		message.obj = result;
		handler.sendMessage(message);
	}

}
