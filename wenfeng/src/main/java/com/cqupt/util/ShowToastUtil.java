package com.cqupt.util;

import android.content.Context;
import android.widget.Toast;

import com.cqupt.R;


public class ShowToastUtil {
	
	public static void showNetworkUnavailableToast(Context context){
		Toast.makeText(context, R.string.tip_network_unavailable, Toast.LENGTH_SHORT).show();
	}
	
	public static void showConnectionTimeOutToast(Context context){
		Toast.makeText(context, R.string.tip_connection_timeout, Toast.LENGTH_SHORT).show();
	}
	
	public static void showEmptyInputToast(Context context){
		Toast.makeText(context, R.string.empty_input_error, Toast.LENGTH_SHORT).show();
	}
	
	public static void showDownloadStartToast(Context context){
		Toast.makeText(context, R.string.tip_start_download, Toast.LENGTH_SHORT).show();
	}
	
	public static void showDownloadSuccessToast(Context context){
		Toast.makeText(context,R.string.tip_attachment_download_success,Toast.LENGTH_SHORT).show();
	}
	
	public static void showSDCardNotExistToast(Context context){
		Toast.makeText(context, R.string.tip_sdcard_not_exist, Toast.LENGTH_SHORT).show();
	}
	
	public static void ShowVideoCannotPlayToast(Context context){
		Toast.makeText(context, R.string.tip_video_can_not_play, Toast.LENGTH_SHORT).show();
	}
	
	public static void ShowSaveSuccessToast(Context context){
		Toast.makeText(context, R.string.save_success, Toast.LENGTH_SHORT).show();
	}

}
