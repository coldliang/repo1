package com.cqupt.util;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

/**
 * ch-english change
 */
public class LanguageSetting {
	
	private static LanguageSetting ls;
	
	private LanguageSetting(){};
	
	public static LanguageSetting  getInstance(){
		
		if(ls == null){
			ls = new LanguageSetting();
		}
		return ls;
	}
	
	public String getCurrentLanguage(Context context){
		
		SharedPreferences pre = context.getSharedPreferences("my_prefer",Activity.MODE_PRIVATE);
		String systemLanguage = Locale.getDefault().getLanguage();
		// default language
		return pre.getString("language", systemLanguage);
	}
	
	public void setCurrentLanguage(Context context,String language){
		// change configuration
		Locale mLocale = new Locale(language);
		Resources re = context.getResources();
		Configuration conf = re.getConfiguration();
		conf.locale = mLocale;
		re.updateConfiguration(conf, re.getDisplayMetrics());
		// record current language in SharedPreferences
		SharedPreferences pre = context.getSharedPreferences("my_prefer",Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pre.edit();
		editor.putString("language",language);
		editor.commit();
	}

}
