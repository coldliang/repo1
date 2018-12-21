
package com.cqupt.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class CheckNetwork {

	public static boolean isConnectingToInternet(Context context){
		ConnectivityManager manager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(manager != null){
			NetworkInfo[] info = manager.getAllNetworkInfo();
	        if(info != null){
	        	for(int i=0;i<info.length;i++){
	        		if(info[i].getState() == NetworkInfo.State.CONNECTED){  //�ж������Ƿ�������
	        	    return true;  //����������
	        	    }
	            }
	       }
	    }
		return false;
	}
	
	/**
	 * ����Ƿ����ӵ�wifi
	 */
	public static boolean isWifiAvailable(Context context){
		
		ConnectivityManager manager=(ConnectivityManager)context.
				getSystemService(Context.CONNECTIVITY_SERVICE); 
		NetworkInfo info = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		if(info.getState() == NetworkInfo.State.CONNECTED){
			return true;
		}
		
		return false;
	}
}
