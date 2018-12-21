package com.cqupt.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class SoftInputManagerUtil {
	
	public static boolean isSoftInputShown(Context context){
		
		InputMethodManager manager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);

		if(manager == null){
			return false;
		}
		
		return manager.isActive();
	}
	
	public static void hideSoftInput(Activity activity){
		
		InputMethodManager manager = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);

		if(manager != null && manager.isActive()){
			View focusView = activity.getCurrentFocus();
			
			if(focusView != null){
				manager.hideSoftInputFromWindow(focusView.getWindowToken()
						, InputMethodManager.HIDE_NOT_ALWAYS);
			}
			
		}
		

	}

}
