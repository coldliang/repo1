package com.cqupt.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.cqupt.net.WebService;


public class CheckAppUpdateThread extends Thread {
	
	private WebService web; 
	private SharedPreferences sp;
	private Context context;
	
	public CheckAppUpdateThread(SharedPreferences sp,Context context){
		
		this.sp = sp;
		web = new WebService();
		this.context = context;
	}
	
	public void run(){
		
		int webVersionCode = AppInfoUtil.getWebVersionCode(web);
		int localVersionCode = AppInfoUtil.getLocalVersionCode(context);
		
		//�����°汾
		if(webVersionCode != AppInfoUtil.CONNETION_FAIL && 
		   localVersionCode != AppInfoUtil.GET_LOCAL_VERSION_FAIL &&
		   localVersionCode < webVersionCode
		  )
		{
			SharedPreferences.Editor editor = sp.edit();
			editor.putBoolean("appUpdate",true);
			editor.commit();
		}else if(webVersionCode != AppInfoUtil.CONNETION_FAIL && 
				   localVersionCode != AppInfoUtil.GET_LOCAL_VERSION_FAIL &&
				   localVersionCode >= webVersionCode)
		{
			SharedPreferences.Editor editor = sp.edit();
			editor.putBoolean("appUpdate", false);
			editor.commit();
		}
	}

}
