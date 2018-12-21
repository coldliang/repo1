package com.cqupt.util;

import java.io.File;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

public class FileDownloadAndroidManager {
	
	public static final long NETWORK_NOT_AVAILABLE = -1;
	public static final long SDCARD_NOT_EXIST = -2;
	
	/**
	 * 
	 * @param uri
	 * @param path
	 * @param fileName
	 * @return downloadID
	 */
	public static long download(Context context,Uri uri,String path,String fileName){
		
		if(CheckNetwork.isConnectingToInternet(context)){
						
			if(Environment.isExternalStorageEmulated()){
				
				ShowToastUtil.showDownloadStartToast(context);
				DownloadManager dm = (DownloadManager) context.getSystemService(Activity.DOWNLOAD_SERVICE);
				DownloadManager.Request req = new DownloadManager.Request(uri);
				
				File file = new File(path);
				
				if(!file.exists()){
					file.mkdirs();
				}
				
				req.setDestinationInExternalPublicDir(path , fileName);
				return dm.enqueue(req);
			}else{
				ShowToastUtil.showSDCardNotExistToast(context);
				return SDCARD_NOT_EXIST;
			}
			
		}else{
			ShowToastUtil.showNetworkUnavailableToast(context);
			return NETWORK_NOT_AVAILABLE;
		}
		
	}

}
