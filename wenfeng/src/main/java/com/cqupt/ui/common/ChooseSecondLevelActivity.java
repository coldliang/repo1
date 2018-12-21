package com.cqupt.ui.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;

import com.cqupt.R;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;

public class ChooseSecondLevelActivity extends Activity implements OnClickListener{
	
	private ImageButton mFreeStudyButton;
	private ImageButton mPlanButton;
	
	//----------------------��д����----------------------
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_choose_second_level);
		
		findView();
		setListener();
		
		
	}
	
	@Override
	public void onClick(View v) {
		
		Intent it = null;
		
		switch (v.getId()) {
		
		case R.id.free_study_button:
			it = new Intent(this,FreeStudyTopResourceSelectActivity.class);
			break;

		case R.id.plan_button:
			
			if(isLogin()){
				
				it = new Intent(this,PlanActivity.class);

			}else{
				
				it = new Intent(this,LoginActivity.class);
				
			}
			
			break;
		}
		
		startActivity(it);
		
	}
	
	//-----------------------�Զ��巽��------------------------
	
	private void findView(){
		
		mFreeStudyButton = (ImageButton) findViewById(R.id.free_study_button);
		mPlanButton = (ImageButton) findViewById(R.id.plan_button);
		
	}
	
	private void setListener(){
		
		mFreeStudyButton.setOnClickListener(this);
		mPlanButton.setOnClickListener(this);
		
	}
	
	private boolean isLogin(){
		
		MySQLiteOpenHelper helper = MySQLiteOpenHelper.getInstance(ChooseSecondLevelActivity.this);
		DBManager db = new DBManager(helper.getConnection());
		return db.login();
	}

}
