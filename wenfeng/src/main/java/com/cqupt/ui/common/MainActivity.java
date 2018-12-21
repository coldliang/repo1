package com.cqupt.ui.common;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;

import com.cqupt.R;
import com.cqupt.util.CheckAppUpdateThread;
import com.cqupt.util.CheckNetwork;
import com.cqupt.util.LanguageSetting;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity { 

	private LanguageSetting ls;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		ls = LanguageSetting.getInstance();
		ls.setCurrentLanguage(this, ls.getCurrentLanguage(this));
		
		if(CheckNetwork.isConnectingToInternet(this)){
			SharedPreferences sp = getSharedPreferences("my_prefer",Activity.MODE_PRIVATE);
			
			new CheckAppUpdateThread(sp,MainActivity.this).start();

		}
		
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
		   @Override
		   public void run() {
			  Intent it = new Intent(MainActivity.this,ChooseMainActivity.class);
			  startActivity(it);
			  finish();
		   }
		  };
		  
		timer.schedule(task, 1000 * 2);
	}
}
