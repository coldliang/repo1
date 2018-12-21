package com.cqupt.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.cqupt.model.AppInfo;
import com.cqupt.net.WebService;

import org.ksoap2.serialization.SoapObject;

public class AppInfoUtil {
	
	public final static int CONNETION_FAIL = -1;
	public final static int GET_LOCAL_VERSION_FAIL = 0;
	public final static String GET_LOCAL_VERSION_NAME_FAIL = "error";

	public static int getWebVersionCode(WebService web){
		
		SoapObject result = web.CallWebService("getVersionCode", null);
		if(result == null){
			return CONNETION_FAIL;
		}else{
			return XMLParser.parseInt(result);
		}
	}
	
	/**
	 * ��ñ�����appVersionCode
	 */
	public static int getLocalVersionCode(Context context){
		
		try {
			PackageInfo pi = context.getPackageManager()
					         .getPackageInfo(context.getPackageName(), 0);
			return pi.versionCode;
		} catch (NameNotFoundException e) {
			
			e.printStackTrace();
			return GET_LOCAL_VERSION_FAIL;
		} 
		
	}
	
	
	/**
	 * ��ñ�����appVersionName
	 */
	public static String getLocalVersionName(Context context){
		
		try {
			PackageInfo pi = context.getPackageManager()
					         .getPackageInfo(context.getPackageName(), 0);
			return pi.versionName;
		} catch (NameNotFoundException e) {
			
			e.printStackTrace();
			return GET_LOCAL_VERSION_NAME_FAIL;
		} 
		
	}
	
	/**
	 * ��÷������˵�app��Ϣ
	 */
	public static AppInfo getAppInfo(WebService web){
		
		SoapObject result = web.CallWebService("getAppInfo", null);
		
		//���ӷ�������ʱ
		if(result == null){
			return null;
		}
		
	   return XMLParser.parseAppInfo(result);
	}

}
