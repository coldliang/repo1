package com.cqupt.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class DensityUtil {

    public static int dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }  
  

    public static int px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (pxValue / scale + 0.5f);  
    }  


    public static int getWindowWidth(Context context){
    	
    	DisplayMetrics metric = new DisplayMetrics();
    	WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
    	manager.getDefaultDisplay().getMetrics(metric);
        return metric.widthPixels;  // ��Ļ��ȣ����أ�
   
    }
    

    public static int getWindowheight(Context context){
    	
    	DisplayMetrics metric = new DisplayMetrics();
    	WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
    	manager.getDefaultDisplay().getMetrics(metric);
        return metric.heightPixels; 
   
    }

}
